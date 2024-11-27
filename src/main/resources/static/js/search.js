setupToggles();

function showAddProjectPage(newBtn) {
    if (authUser != null) {
        window.location.href = '/projects/add';
    } else {
        $('.with-popover').popover('hide');
        newBtn.popover('toggle');
    }
}

let profilesCurrentPage = 0;
const profilesArea = $('#profilesArea');
const loadMoreProfilesBtn = $('#loadMoreProfilesBtn');

let projectsCurrentPage = 0;
const projectsArea = $('#projectsArea');
const loadMoreProjectsBtn = $('#loadMoreProjectsBtn');

let tagsCurrentPage = 0;
const tagsArea = $('#tagsArea');
const loadMoreTagsBtn = $('#loadMoreTagsBtn');

function loadMoreProfiles() {
    loadMoreProfilesBtn.attr('hidden', true);
    profilesCurrentPage++;
    let keyword = new URLSearchParams(window.location.search).get('keyword');

    $.ajax({
        url: '/profile/by-keyword',
        data: { keyword: keyword, page: profilesCurrentPage, size: 9 }
    }).done(profilesPage => {
        if (profilesPage.content.length !== 0) {
            profilesPage.content.forEach(profile => {
                profilesArea.append(generateProfileDiv(profile));
            });
            if (profilesPage.total !== $('.profile-div').length) {
                loadMoreProfilesBtn.attr('hidden', false);
            }
        }
    }).fail(function(data) {
        handleError(data, getMessage('user.failed-to-get-profiles'));
        profilesCurrentPage--;
        loadMoreProfilesBtn.attr('hidden', false);
    });
}

function generateProfileDiv(profile) {
    let dFlexDiv = $('<div></div>').addClass('d-flex profile-div mb-3');
    let avatarProfileLink = $('<a></a>').addClass('text-decoration-none link-body-emphasis')
        .attr('href', `/profile/${profile.id}/view`);
    let avatar = $('<img>').addClass('rounded-circle border')
        .attr('src', getAvatarLink(profile.avatar)).attr('width', '40').attr('height', '40')
        .attr('title', profile.name)
        .css('object-fit', 'cover')
        .on('mouseenter', function() {$(this).addClass('opacity-75')})
        .on('mouseleave', function() {$(this).removeClass('opacity-75')});
    avatarProfileLink.append(avatar);
    dFlexDiv.append(avatarProfileLink);
    let nameDiv = $('<div></div>').addClass('ms-2');
    let nameProfileLink = $('<a></a>').addClass('text-decoration-none link-body-emphasis')
        .attr('href', `/profile/${profile.id}/view`);
    let nameSpan = $('<span></span>').addClass('h6').html(`${profile.name}`);
    nameProfileLink.append(nameSpan);
    nameDiv.append(nameProfileLink);
    dFlexDiv.append(nameDiv);
    return dFlexDiv;
}

function loadMoreProjects() {
    loadMoreProjectsBtn.attr('hidden', true);
    projectsCurrentPage++;
    let keyword = new URLSearchParams(window.location.search).get('keyword');

    $.ajax({
        url: '/projects/by-keyword',
        data: { keyword: keyword, page: projectsCurrentPage, size: 9 }
    }).done(projectsPage => {
        if (projectsPage.content.length !== 0) {
            projectsPage.content.forEach(project => {
                projectsArea.append(generateProjectCard(project, 'my-3', '4', false));
            });
            setupToggles();

            if (projectsPage.total !== $('.project-card').length) {
                loadMoreProjectsBtn.attr('hidden', false);
            }
        }
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
        projectsCurrentPage--;
        loadMoreProjectsBtn.attr('hidden', false);
    });
}

function loadMoreTags() {
    loadMoreTagsBtn.attr('hidden', true);
    tagsCurrentPage++;
    let keyword = new URLSearchParams(window.location.search).get('keyword');

    $.ajax({
        url: '/search/tags',
        data: { keyword: keyword, page: tagsCurrentPage, size: 9 }
    }).done(tagsPage => {
        if (tagsPage.content.length !== 0) {
            tagsPage.content.forEach(tag => {
                tagsArea.append(generateTagDiv(tag));
            });
            if (tagsPage.total !== $('.tag-div').length) {
                loadMoreTagsBtn.attr('hidden', false);
            }
        }
    }).fail(function(data) {
        handleError(data, getMessage('info.failed-to-get-tags'));
        tagsCurrentPage--;
        loadMoreTagsBtn.attr('hidden', false);
    });
}

function generateTagDiv(tag) {
    let tagDiv = $('<div></div>').addClass('tag-div mb-3');
    let tagLink = $('<a></a>').addClass('text-decoration-none').attr('href', `/tags/${tag.name}`)
        .html(`#${tag.name}`);
    tagDiv.append(tagLink);
    return tagDiv;
}
