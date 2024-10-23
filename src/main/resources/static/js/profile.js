const changePasswordModal = $('#changePasswordModal');
const projectsTab = $('#projectsTab');
const qualificationTab = $('#qualificationTab');

let href = window.location.href;
if (href.includes('#qualification')) {
    $('#qualificationTabBtn').click();
} else if (href.includes('#projects')) {
    $('#projectsTabBtn').click();
}

changePasswordModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#currentPassword').val('');
    $(e.currentTarget).find('#password').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#repeatPassword').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#passwordMatchError').text('');
    $(e.currentTarget).find('#confirmBtn').prop('disabled', false);
});

function changePassword() {
    let currentPassword = changePasswordModal.find('#currentPassword').val();
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (currentPassword.length && password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: "profile/change-password",
            type: "PATCH",
            data: { currentPassword: currentPassword, newPassword: password },
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(getMessage('user.password-changed'));
        }).fail(function(data) {
            handleError(data, getMessage('user.failed-to-change-password'));
        });
    }
}

function showInformation() {
    window.history.replaceState(null, null, "#");
}

function showQualification() {
    window.history.replaceState(null, null, "#qualification");
    if (qualificationTab.children().length === 0) {
        getProjectsAndFillTabs();
    }
}

function showProjects() {
    window.history.replaceState(null, null, "#projects");
    if (projectsTab.children().length === 0) {
        getProjectsAndFillTabs();
    }
}

function getProjectsAndFillTabs() {
    $.ajax({
        url: '/projects/by-author',
        data: `userId=${$('#userId').text()}`
    }).done(projects => {
        if (projects.length !== 0) {
            fillProjectsTab(projects);
            fillQualificationTab(projects);
        }
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
    });
}

function fillProjectsTab(projects) {
    projectsTab.empty();
    let row = $('<div></div>').addClass('row justify-content-center');
    let column = $('<div></div>').addClass('col-md-8');
    row.append(column);
    projects.forEach(project => {
        column.append(generateProjectCard(project));
    });
    projectsTab.append(row);
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
}

function generateProjectCard(project) {
    let card = $('<div></div>').addClass('card project-card rounded-3 mt-3');
    let previewDiv = $('<div></div>').addClass('ratio').css('--bs-aspect-ratio', '50%');
    let preview = $('<img>').addClass('card-img-top rounded-top-3').css('object-fit', 'cover')
        .attr('src', `/${project.preview.fileLink}`);
    previewDiv.append(preview);
    card.append(previewDiv);
    let architectureDiv= $('<div></div>').attr('title', getMessage('architecture'));
    let architectureImage = $('<img>').addClass('float-end bg-light-subtle rounded-circle p-2')
        .attr('src', `/${project.architecture.logo.fileLink}`)
        .attr('data-bs-toggle', 'tooltip').attr('title', project.architecture.name)
        .attr('width', '48').attr('height', '48').css('margin-top', '-25px').css('margin-right', '15px')
        .css('z-index', '2').css('position', 'relative')
        .css('box-shadow', '0 1px 2px 0 rgba(0, 0, 0, 0.2), 0 1px 2px 0 rgba(0, 0, 0, 0.19)')
        .on('mouseenter', function () {
            $(this).removeClass('p-2').addClass('p-1');
        })
        .on('mouseleave', function () {
            $(this).removeClass('p-1').addClass('p-2');
        });
    architectureDiv.append(architectureImage);
    card.append(architectureDiv);

    let cardBody = $('<div></div>').addClass('card-body d-flex flex-column pb-0').css('margin-top', '-40px');
    let authorDiv = $('<div></div>').addClass('d-flex');
    let avatarDiv = $('<div></div>').addClass('pt-3 pb-2 ps-3').css('position', 'relative').css('z-index', '2')
        .css('margin-left', '-16px');
    let avatar = $('<img>').addClass('rounded-circle border')
        .attr('src', `${project.author.avatar != null ? project.author.avatar.fileLink : '/images/no-avatar.svg'}`)
        .attr('width', '40').attr('height', '40');
    avatarDiv.append(avatar);
    authorDiv.append(avatarDiv);
    let authorNameAndCreatedDiv = $('<div></div>').addClass('pt-3 pb-2 px-3').css('position', 'relative')
        .css('z-index', '2').css('margin-left', '-8px');
    let authorName = $('<span></span>').addClass('h6').text(project.author.name);
    authorNameAndCreatedDiv.append(authorName);
    let createdDiv = $('<div></div>').addClass('tiny text-secondary-emphasis').css('margin-top', '-3px')
        .text(formatDateTime(project.created));
    authorNameAndCreatedDiv.append(createdDiv);
    authorDiv.append(authorNameAndCreatedDiv);
    cardBody.append(authorDiv);

    let name = $('<h5></h5>').addClass('card-title').text(project.name);
    cardBody.append(name);

    let annotation = $('<span></span>').addClass('card-text').text(project.annotation);
    cardBody.append(annotation);

    let likesCommentsViewsRow = $('<div></div>').addClass('row mt-auto pt-3 pb-1').css('position', 'relative')
        .css('z-index', '2');
    let likesCommentsCol = $('<div></div>').addClass('col-8');
    likesCommentsViewsRow.append(likesCommentsCol);
    let commentsBtn = $('<a></a>').addClass('btn-link text-decoration-none link-info').attr('type', 'button')
        .attr('title', getMessage('comment.comments')).attr('href', `/projects/${project.id}/view#comments`);
    let commentsSymbol = $('<i></i>').addClass('fa-regular fa-comments');
    let commentsCounter = $('<span></span>').addClass('text-secondary-emphasis small').text(` ${project.commentsCount}`);
    commentsBtn.append(commentsSymbol);
    commentsBtn.append(commentsCounter);
    likesCommentsCol.append(commentsBtn);
    let likeBtn = $('<a></a>').addClass('like-btn btn-link link-danger text-decoration-none ms-3')
        .attr('type', 'button').attr('title', getMessage('like')).attr('data-bs-toggle', 'popover')
        .attr('data-bs-trigger', 'manual')
        .attr('data-bs-title', `"<a type='button' class='btn-close ms-2 float-end tiny'></a><div>${getMessage('info.only-for-auth-users')}</div>"`)
        .attr('data-bs-content', `"<div class='text-center'><a href='/login' type='button' class='btn btn-sm btn-warning px-3'>${getMessage('login')}</a></div>"`)
        .attr('data-bs-html', 'true')
        .on('click', function () {
            likeProject($(this), project.id);
        });
    let likeSymbol = $('<i></i>').addClass('fa-regular fa-heart');
    let likeCounter = $('<span></span>').addClass('text-secondary-emphasis small').text(` ${project.likesUserIds.length}`);
    likeBtn.append(likeSymbol);
    likeBtn.append(likeCounter);
    likesCommentsCol.append(likeBtn);
    let viewsCol = $('<div></div>').addClass('col-4 text-end');
    let viewsSymbol = $('<i></i>').addClass('fa-regular fa-eye').css('color', '#a1a0a0');
    let viewsCounter = $('<span></span>').addClass('text-secondary-emphasis small').text(` ${project.views}`);
    viewsCol.append(viewsSymbol);
    viewsCol.append(viewsCounter);
    likesCommentsViewsRow.append(viewsCol);
    cardBody.append(likesCommentsViewsRow);

    let projectLink = $('<a></a>').addClass('stretched-link').attr('href', `/projects/${project.id}/view`);
    cardBody.append(projectLink);

    card.append(cardBody);

    let footer = $('<div></div>').addClass('card-footer').css('position', 'relative').css('z-index', '2');
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
        $('.like-btn').popover('hide');
        $(likeBtn).popover('toggle');
    }
}

function formatDateTime(dateTime) {
    let dateAndTime = dateTime.split('T');
    let dateParts =  dateAndTime[0].split('-');
    let formattedDate = `${dateParts[2]}.${dateParts[1]}.${dateParts[0]}`;
    let formattedTime = dateAndTime[1].split('.')[0];
    return `${formattedDate} ${formattedTime}`;
}

function fillQualificationTab(projects) {
    qualificationTab.empty();
    let technologies = [];
    let alreadyAdded = [];
    projects.forEach(project => {
        project.technologies.forEach(technology => {
            if ($.inArray(technology.name, alreadyAdded) === -1) {
                technologies.push(technology);
                alreadyAdded.push(technology.name);
            }
        });
    });

    technologies.sort(technologiesComparator);

    technologies.forEach(technology => {
        let technologySpan = $('<span></span>').addClass('badge bg-body-tertiary me-2 mt-2');
        let technologyLink = $('<a></a>').attr('type', 'button').attr('target', '_blank').attr('href', technology.url)
            .addClass('link-underline link-underline-opacity-0 link-underline-opacity-75-hover link-body-emphasis');
        let technologyImage = $('<img>').addClass('align-bottom me-1').attr('src', `/${technology.logo.fileLink}`)
            .attr('width', '32').attr('height', '32');
        technologyLink.append(technologyImage).append(technology.name);
        technologySpan.append(technologyLink);
        qualificationTab.append(technologySpan);
    });
}

const priorities = new Map();
priorities.set('ULTRA', '0');
priorities.set('VERY_HIGH', '1');
priorities.set('HIGH', '2');
priorities.set('MEDIUM', '3');
priorities.set('LOW', '4');
priorities.set('VERY_LOW', '5');

function technologiesComparator(t1, t2) {
    if (t1.usage !== t2.usage) {
        return (t1.usage === 'BACKEND') ? -1 : 1;
    }
    if (t1.priority !== t2.priority) {
        return (priorities.get(t1.priority) < priorities.get(t2.priority)) ? -1 : 1
    }
    if (t1.name !== t2.name) {
        return (t1.name < t2.name) ? -1 : 1;
    }
    return 0;
}
