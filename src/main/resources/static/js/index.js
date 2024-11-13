setupTooltips();

function setupTooltips() {
    $('.with-popover').on('shown.bs.popover', () => {
        $('.btn-close').on('click', () => $('.with-popover').popover('hide'));
    });

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
}

function showAddProjectPage(newBtn) {
    if (authUser != null) {
        window.location.href = '/projects/add';
    } else {
        $('.with-popover').popover('hide');
        newBtn.popover('toggle');
    }
}

function likeProject(likeBtn, id) {
    if (authUser != null) {
        let likeIcon = $(likeBtn).find('i');
        let likeCounter = $(likeBtn).find('span');
        let liked = likeIcon.attr('class').includes('fa-regular');
        $.ajax({
            url: `/projects/${id}/like`,
            type: "PATCH",
            data: { id: id, liked: liked },
        }).done(function() {
            likeIcon.removeClass(liked ? 'fa-regular' : 'fa-solid').addClass(liked ? 'fa-solid' : 'fa-regular');
            likeCounter.text(+(likeCounter.text()) + (liked ? 1 : -1));
        }).fail(function(data) {
            likeIcon.removeClass(liked ? 'fa-solid' : 'fa-regular').addClass(liked ? 'fa-regular' : 'fa-solid');
            handleError(data,  getMessage(liked ? 'project.failed-to-like' : 'project.failed-to-dislike'));
        });
    } else  {
        $('.with-popover').popover('hide');
        $(likeBtn).popover('toggle');
    }
}

let currentNav = null;
if (window.location.pathname.startsWith('/tags/')) {
    currentNav = 'tagNav';
    let navs = $('#navs');

    let navItem = $('<li></li>').addClass('nav-item');
    let navLink = $('<button></button>').addClass('nav-link secondary-nav-link pt-0').attr('type', 'button')
        .attr('id', 'tagNav')
        .text('#' + (window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1)));
    navItem.append(navLink);
    navs.append(navItem);
} else {
    let urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('popular')) {
        currentNav = 'popularNav';
    } else if (authUser != null && urlParams.has('by-author') && urlParams.get('by-author') ==  authUser.id) {
        currentNav = 'myNav';
    } else if (!urlParams.has('by-author')) {
        currentNav = 'freshNav';
    }
}


if (currentNav != null) {
    $(`#${currentNav}`).addClass('active');
}


function showPopular() {
    if (currentNav !== 'popularNav') {
        window.location.href = '/?popular';
    }
}

function showFresh() {
    if (currentNav !== 'freshNav') {
        window.location.href = '/';
    }
}

function showMy() {
    if (currentNav !== 'myNav') {
        window.location.href = `/?by-author=${authUser.id}`;
    }
}

let currentPage = 0;
let loading = false;
const projectsArea = $('#projectsArea');
const loadingDiv = $('#loading')[0];

const observer = new IntersectionObserver(async (entries) => {
    if (entries[0].isIntersecting && !loading) {
        loading = true;
        currentPage++;
        getProjects();
    }
}, { threshold: 0.25 });

if (loadingDiv) {
    observer.observe(loadingDiv);
}

function getProjects() {
    let pathVariable;
    if (currentNav != null) {
        pathVariable = currentNav === 'popularNav' ? 'popular' : (currentNav === 'tagNav' ? 'by-tag' : 'fresh');
    } else {
        pathVariable = 'fresh';
    }
    let params;
    if (pathVariable === 'by-tag') {
        params = { page: currentPage, size: 9, tag:  window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1) }
    } else {
        params = { page: currentPage, size: 9 }
    }

    $.ajax({
        url: `/projects/${pathVariable}`,
        data: params
    }).done(projectsPage => {
        if (projectsPage.content.length !== 0) {
            projectsPage.content.forEach(project => {
                let col = $('<div></div>').addClass('col mb-4');
                col.append(generateProjectCard(project));
                projectsArea.append(col);
            });
            $('.with-popover').on('shown.bs.popover', () => {
                $('.btn-close').on('click', () => $('.with-popover').popover('hide'));
            });
            const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
            const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
            const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
            const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
        } else {
            observer.disconnect();
            loadingDiv.remove();
        }
        loading = false;
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
        loading = false;
    });
}

function generateProjectCard(project) {
    let card = $('<div></div>').addClass('card h-100 project-card rounded-3');
    let previewDiv = $('<div></div>').addClass('ratio').css('--bs-aspect-ratio', '50%');
    let preview = $('<img>').addClass('card-img-top rounded-top-3').css('object-fit', 'cover')
        .attr('src', `/${project.preview.fileLink}`);
    previewDiv.append(preview);
    card.append(previewDiv);
    let architectureDiv = $('<div></div>').attr('title', getMessage('architecture'));
    let architectureImage = $('<img>').addClass('float-end bg-light-subtle border border-light-subtle rounded-circle p-1')
        .attr('src', `/${project.architecture.logo.fileLink}`)
        .attr('data-bs-toggle', 'tooltip').attr('title', project.architecture.name)
        .attr('width', '40').attr('height', '40').css('margin-top', '-20px').css('margin-right', '15px')
        .css('z-index', '2').css('position', 'relative')
        .css('box-shadow', '0 1px 2px 0 rgba(0, 0, 0, 0.2), 0 1px 2px 0 rgba(0, 0, 0, 0.19)')
        .on('mouseenter', function () {
            $(this).removeClass('p-1');
        })
        .on('mouseleave', function () {
            $(this).addClass('p-1');
        });
    architectureDiv.append(architectureImage);
    card.append(architectureDiv);

    let cardBody = $('<div></div>').addClass('card-body d-flex flex-column pb-0').css('margin-top', '-35px');
    let dFlexDiv = $('<div></div>').addClass('d-flex');
    let avatarDiv = $('<div></div>').addClass('pt-3 pb-2 ps-3').css('position', 'relative').css('z-index', '2')
        .css('margin-left', '-16px');
    let avatarLink = $('<a></a>').addClass('text-decoration-none link-body-emphasis')
        .attr('href', `/profile/${project.author.id}/view`).attr('title', project.author.name);
    let avatar = $('<img>').addClass('rounded-circle border')
        .attr('src', getAvatarLink(project.author.avatar))
        .attr('width', '40').attr('height', '40').css('object-fit', 'cover')
        .on('mouseenter', function () {
            $(this).addClass('opacity-75');
        })
        .on('mouseleave', function () {
            $(this).removeClass('opacity-75');
        });
    avatarLink.append(avatar);
    avatarDiv.append(avatarLink);
    dFlexDiv.append(avatarDiv);
    let authorNameAndCreatedDiv = $('<div></div>').addClass('pt-3 pb-2 px-3').css('position', 'relative')
        .css('z-index', '2').css('margin-left', '-8px');
    let authorName = $('<span></span>').addClass('h6').text(project.author.name);
    let authorNameLink = $('<a></a>').addClass('text-decoration-none link-body-emphasis')
        .attr('href', `/profile/${project.author.id}/view`);
    authorNameLink.append(authorName);
    authorNameAndCreatedDiv.append(authorNameLink);
    let createdDiv = $('<div></div>').addClass('tiny text-secondary-emphasis').css('margin-top', '-3px')
        .text(formatDateTime(project.created));
    authorNameAndCreatedDiv.append(createdDiv);
    dFlexDiv.append(authorNameAndCreatedDiv);
    cardBody.append(dFlexDiv);

    let name = $('<h5></h5>').addClass('card-title').text(project.name).attr('id', `${project.id}-name-elem`);
    cardBody.append(name);
    if (!project.visible) {
        let invisibleSymbol = $('<i></i>').addClass('fa-solid fa-eye-slash text-warning tiny float-end')
            .attr('title', getMessage('project.invisible-to-users')).css('position', 'relative')
            .css('z-index', '2');
        name.append(invisibleSymbol);
    }

    let annotation = $('<span></span>').addClass('card-text').text(project.annotation);
    cardBody.append(annotation);

    let likesCommentsShareViewsRow = $('<div></div>').addClass('row mt-auto pt-3 pb-1').css('position', 'relative')
        .css('z-index', '2');
    let likesCommentsShareCol = $('<div></div>').addClass('col-8');
    likesCommentsShareViewsRow.append(likesCommentsShareCol);
    let commentsBtn = $('<a></a>').addClass('btn-link text-decoration-none link-info').attr('type', 'button')
        .attr('title', getMessage('comment.comments')).attr('href', `/projects/${project.id}/view#comments`);
    let commentsSymbol = $('<i></i>').addClass('fa-regular fa-comments');
    let commentsCounter = $('<span></span>').addClass('ms-1 text-secondary-emphasis small').text(project.commentsCount);
    commentsBtn.append(commentsSymbol);
    commentsBtn.append(commentsCounter);
    likesCommentsShareCol.append(commentsBtn);
    let likeBtn = $('<a></a>').addClass('with-popover btn-link link-danger text-decoration-none ms-3')
        .attr('type', 'button').attr('data-bs-toggle', 'popover')
        .attr('data-bs-trigger', 'manual')
        .attr('data-bs-title', `"<a type='button' class='btn-close ms-2 float-end tiny'></a><div>${getMessage('info.only-for-auth-users')}</div>"`)
        .attr('data-bs-content', `"<div class='text-center'><a href='/login' type='button' class='btn btn-sm btn-warning px-3'>${getMessage('login')}</a></div>"`)
        .attr('data-bs-html', 'true')
        .on('click', function () {
            likeProject($(this), project.id);
        });
    let likeSymbol = $('<i></i>').addClass('fa-heart')
        .addClass(`${authUser === null || !project.likesUserIds.includes(authUser.id) ? 'fa-regular' : 'fa-solid'}`)
        .attr('title', getMessage('like'));
    let likeCounter = $('<span></span>').addClass('ms-1 text-secondary-emphasis small')
        .attr('title', getMessage('like')).text(project.likesUserIds.length);
    likeBtn.append(likeSymbol);
    likeBtn.append(likeCounter);
    likesCommentsShareCol.append(likeBtn);
    let shareBtn = $('<a></a>').attr('type', 'button').addClass('btn-link link-primary text-decoration-none ms-3')
        .attr('title', getMessage('project.share')).attr('data-bs-toggle', 'dropdown');
    let shareSymbol = $('<i></i>').addClass('fa-solid fa-share');
    shareBtn.append(shareSymbol);
    let shareDropDownMenu = $('<ul></ul>').addClass('dropdown-menu');
    let copyLinkItem = $('<li></li>');
    let copyLinkBtn = $('<button></button>').attr('type', 'button').addClass('dropdown-item')
        .html(`<i class="fa-solid fa-link fa-fw me-2"></i>${getMessage('project.copy-link')}`)
        .on('click', () => copyLink(project.id));
    copyLinkItem.append(copyLinkBtn);
    shareDropDownMenu.append(copyLinkItem);
    let shareOnVkItem = $('<li></li>');
    let shareOnVkBtn = $('<button></button>').attr('type', 'button').addClass('dropdown-item')
        .attr('data-id', project.id).attr('data-name', project.name)
        .html(`<i class="fa-brands fa-vk fa-fw me-2"></i>${getMessage('project.share-on-vk')}`)
        .on('click', function () {shareOnVk($(this))});
    shareOnVkItem.append(shareOnVkBtn);
    shareDropDownMenu.append(shareOnVkItem);
    let shareOnTelegramItem = $('<li></li>');
    let shareOnTelegramBtn = $('<button></button>').attr('type', 'button').addClass('dropdown-item')
        .attr('data-id', project.id).attr('data-name', project.name)
        .html(`<i class="fa-brands fa-telegram fa-fw me-2"></i>${getMessage('project.share-on-telegram')}`)
        .on('click', function () {shareOnTelegram($(this))});
    shareOnTelegramItem.append(shareOnTelegramBtn);
    shareDropDownMenu.append(shareOnTelegramItem);
    let shareOnWhatsAppItem = $('<li></li>');
    let shareOnWhatsAppBtn = $('<button></button>').attr('type', 'button').addClass('dropdown-item')
        .attr('data-id', project.id).attr('data-name', project.name)
        .html(`<i class="fa-brands fa-whatsapp fa-fw me-2"></i>${getMessage('project.share-on-whatsapp')}`)
        .on('click', function () {shareOnWhatsApp($(this))});
    shareOnWhatsAppItem.append(shareOnWhatsAppBtn);
    shareDropDownMenu.append(shareOnWhatsAppItem);
    likesCommentsShareCol.append(shareBtn);
    likesCommentsShareCol.append(shareDropDownMenu);

    let viewsCol = $('<div></div>').addClass('col-4 text-end');
    let viewsSymbol = $('<i></i>').addClass('fa-regular fa-eye').css('color', '#a1a0a0');
    let viewsCounter = $('<span></span>').addClass('ms-1 text-secondary-emphasis small').text(project.views);
    viewsCol.append(viewsSymbol);
    viewsCol.append(viewsCounter);
    likesCommentsShareViewsRow.append(viewsCol);
    cardBody.append(likesCommentsShareViewsRow);

    let projectLink = $('<a></a>').addClass('stretched-link').attr('href', `/projects/${project.id}/view`);
    cardBody.append(projectLink);

    card.append(cardBody);

    let footer = $('<div></div>').addClass('card-footer').css('position', 'relative').css('z-index', '1').css('min-height', '78px');
    let technologiesDiv = $('<div></div>');
    footer.append(technologiesDiv);
    project.technologies.forEach(technology => {
        let technologyLink = $('<a></a>').attr('type', 'button').attr('target', '_blank')
            .attr('title', technology.name).attr('href', technology.url).addClass('me-1 mb-1')
            .css('text-decoration', 'none');
        let technologyLogo = $('<img>').attr('src', `/${technology.logo.fileLink}`).attr('width', 24)
            .attr('height', 24)
            .on('mouseenter', function () {
                $(this).addClass('opacity-75');
            })
            .on('mouseleave', function () {
                $(this).removeClass('opacity-75');
            });
        technologyLink.append(technologyLogo);
        technologiesDiv.append(technologyLink);
    });

    card.append(footer);
    return card;
}

function getAvatarLink(avatar) {
    return avatar != null ? (avatar.fileLink.startsWith('https://') ? avatar.fileLink : `/${avatar.fileLink}`) : '/images/no-avatar.svg';
}

function formatDateTime(dateTime) {
    let dateAndTime = dateTime.split('T');
    let dateParts =  dateAndTime[0].split('-');
    let formattedDate = `${dateParts[2]}.${dateParts[1]}.${dateParts[0]}`;
    let formattedTime = dateAndTime[1].substring(0, dateAndTime[1].lastIndexOf(':'));
    return `${formattedDate} ${formattedTime}`;
}
