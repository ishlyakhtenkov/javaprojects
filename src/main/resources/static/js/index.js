setupTooltips();

function setupTooltips() {
    $('.like-btn').on('shown.bs.popover', () => {
        $('.btn-close').on('click', () => $('.like-btn').popover('hide'));
    });

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
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
