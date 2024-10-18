const projectId = $('#projectId').text();
const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
const newCommentInput = $('#newCommentInput');

$('.with-popover').on('shown.bs.popover', () => {
    $('.btn-close').on('click', () => $('.with-popover').popover('hide'));
});

$('.delete-comment-btn').on('shown.bs.popover', (event) => {
    $('.del-com').on('click', () => deleteComment($(event.target).attr('id').replace('deleteCommentBtn-', '')));
});

$('.comment').each(function() {
    let counter = +$(this).css('margin-left').replace('px', '') / 16;
    if (counter > 0) {
        let outerDiv = $('<div></div>').addClass('ps-3 border-start').attr('id',`outer-${$(this).attr('id').replace('comment-', '')}`);
        outerDiv.data('indent', counter);
        counter--;
        let innerDiv = outerDiv;
        while (counter > 0) {
            let div = $('<div></div>').addClass('ps-3 border-start');
            innerDiv.append(div);
            innerDiv = div;
            counter--;
        }
        $(this).css('margin-left', '0px').removeClass('mt-3').addClass('pt-3');
        innerDiv.append($(this));
        $('#commentsArea').append(outerDiv);
    } else {
        $('#commentsArea').append($(this).removeClass('mt-3').addClass('pt-3'));
    }
})

newCommentInput.on('focus', () => {
    if (authUser === null) {
        newCommentInput.attr('readonly', 'true');
        $('.with-popover').popover('hide');
        newCommentInput.popover('toggle');
    } else {
        $('#newCommentDiv').removeClass('opacity-50');
        $('#replyCommentDiv').remove();
        $('#cancelEditBtn').click();
    }
})

$('.new-comment').on('input', (event) => {
    $(event.target).css('height', 'auto');
    $(event.target).css('height', $(event.target).prop('scrollHeight') + 2 + 'px');
    $('#sendNewBtn').attr('hidden', $(event.target).val().length === 0);
});


function reply(replyBtn) {
    if (authUser != null) {
        $('#newCommentDiv').addClass('opacity-50');
        newCommentInput.val('');
        $('#sendNewBtn').attr('hidden', true);

        let editCommentDiv = $('.editCommentDiv');
        if (editCommentDiv.length) {
            let commentId = editCommentDiv.attr('id').replace('editCommentDiv-', '');
            $(`#comment-${commentId}`).attr('hidden', false);
            editCommentDiv.remove();
        }

        $('#replyCommentDiv').remove();
        let div = $('<div></div>').addClass('col-12 col-md-6 mt-3 ms-3').attr('id', 'replyCommentDiv');
        let textArea = $('<textarea></textarea>').addClass('form-control bg-light-subtle reply-comment pb-4')
            .attr('id', 'replyComment').css('resize', 'none').attr('placeholder', getMessage('comment.leave-comment-here'))
            .attr('rows', '2');

        let sendReplyBtn = $('<a></a>').addClass('btn btn-link text-decoration-none float-end pe-2').css('margin-top', '-35px')
            .attr('type', 'button').attr('id', 'sendReplyBtn').attr('hidden', true).html(getMessage('comment.send'));
        let cancelReplyBtn = $('<a></a>').addClass('btn btn-link link-secondary text-decoration-none float-end').css('margin-top', '-35px')
            .attr('type', 'button').attr('id', 'cancelReplyBtn').html(getMessage('cancel'));
        div.append(textArea);
        div.append(sendReplyBtn);
        div.append(cancelReplyBtn);
        div.insertAfter(replyBtn.parent());

        $('#replyComment').on('input', (event) => {
            $(event.target).css('height', 'auto');
            $(event.target).css('height', $(event.target).prop('scrollHeight') + 2 + 'px');
            $('#sendReplyBtn').attr('hidden', $(event.target).val().length === 0);
        });

        $('#cancelReplyBtn').on('click', () => {
            $('#replyCommentDiv').remove();
            $('#newCommentDiv').removeClass('opacity-50');
        });

        $('#sendReplyBtn').on('click', () => {
            let text = $('#replyComment').val();
            let parentId = replyBtn.attr('id').replace('replyBtn-', '');
            postComment(parentId, text, postRepliedCommentSuccess);
        });
    } else {
        $('.with-popover').popover('hide');
        $(replyBtn).popover('toggle');
    }
}

function postRepliedCommentSuccess(comment) {
    $('.commentsCounter').each(function() {
        $(this).text(+$(this).text() + 1);
    });
    let commentDiv = generateCommentDiv(comment, true);
    let indent = $(`#outer-${comment.parentId}`).data('indent') != null ? +$(`#outer-${comment.parentId}`).data('indent') : 0;
    let counter = indent + 1;
    let outerDiv = $('<div></div>').addClass('ps-3 border-start').attr('id', `outer-${comment.id}`).data('indent', counter);
    counter--;
    let innerDiv = outerDiv;
    while (counter > 0) {
        let div = $('<div></div>').addClass('ps-3 border-start');
        innerDiv.append(div);
        innerDiv = div;
        counter--;
    }
    commentDiv.removeClass('mt-3').addClass('pt-3');
    innerDiv.append(commentDiv);
    if (indent !== 0) {
        outerDiv.insertAfter($(`#outer-${comment.parentId}`));
    } else {
        outerDiv.insertAfter($(`#comment-${comment.parentId}`));
    }

    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    $('#replyCommentDiv').remove();
    $('#newCommentDiv').removeClass('opacity-50');
}

function postComment(parentId, text, successCallBack) {
    if (text.length) {
        $.ajax({
            url: `/projects/${projectId}/comments`,
            type: 'POST',
            data: JSON.stringify(makeCommentObject(parentId, text)),
            contentType: 'application/json; charset=utf-8'
        }).done((comment) => {
            successCallBack(comment);
        }).fail(function(data) {
            handleError(data, getMessage('comment.failed-to-add'));
        });

    }
}

function makeCommentObject(parentId, text) {
    return {
        projectId: projectId,
        parentId: parentId,
        text: text
    };
}

function postNewCommentSuccess(comment) {
    $('.commentsCounter').each(function() {
        $(this).text(+$(this).text() + 1);
    });
    let commentDiv = generateCommentDiv(comment, false);
    $('#commentsArea').prepend(commentDiv);

    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    newCommentInput.val('');
    $('#sendNewBtn').attr('hidden', true);
}

function generateCommentDiv(comment, isReply) {
    let commentDiv = $('<div></div>').addClass('comment mt-3').attr('id', `comment-${comment.id}`);
    let dFlexDiv = $('<div></div>').addClass('d-flex');
    let avatar = $('<img>').addClass('rounded-circle border')
        .attr('src', `${(comment.author.avatar.fileLink).startsWith('https://') ? comment.author.avatar.fileLink : ('/' + comment.author.avatar.fileLink)}`)
        .attr('width', '40').attr('height', '40');
    let nameAndTimeDiv = $('<div></div>').addClass('ms-2');
    let nameSpan = $('<span></span>').addClass('h6').html(`${comment.author.name}`);
    let timeDiv = $('<div></div>').addClass('text-secondary-emphasis tiny').css('margin-top', '-3px')
        .html(formatDateTime(comment.created));
    nameAndTimeDiv.append(nameSpan);
    nameAndTimeDiv.append(timeDiv);
    if (isReply) {
        let borderSpan = $('<span></span>').addClass('my-auto border-bottom').css('margin-left', '-16px').css('width', '16px');
        dFlexDiv.append(borderSpan);
    }
    dFlexDiv.append(avatar);
    dFlexDiv.append(nameAndTimeDiv);
    let textDiv = $('<div></div>').addClass('comment-text').css('white-space', 'pre-wrap').html(comment.text);

    let buttonsDiv = $('<div></div>').addClass('comment-actions');
    let likeBtn = $('<a></a>').addClass('like-btn btn-link link-danger text-decoration-none with-popover')
        .attr('type', 'button').attr('title', getMessage('like'));
    let likeSymbol = $('<i></i>').addClass('fa-regular fa-heart');
    let likeCounter = $('<span></span>').addClass('text-secondary-emphasis small').text(` ${comment.likes.length}`);
    likeBtn.append(likeSymbol);
    likeBtn.append(likeCounter);
    likeBtn.on('click', () => {
        like(likeBtn, comment.id);
    });

    let replyBtn = $('<a></a>').addClass('reply-btn btn-link text-decoration-none ms-2 with-popover')
        .attr('type', 'button').attr('id', `replyBtn-${comment.id}`);
    let replySymbol = $('<i></i>').addClass('fa-solid fa-share fa-rotate-270 small');
    let replySpan = $('<span></span>').addClass('text-secondary-emphasis small').text(` ${getMessage('comment.reply')}`).css('margin-left', '-3px');
    replyBtn.on('click', () => {
        reply(replyBtn);
    })
    replyBtn.append(replySymbol);
    replyBtn.append(replySpan);

    let editBtn = $('<a></a>').addClass('btn-link text-decoration-none ms-2')
        .attr('type', 'button').attr('title', getMessage('edit')).attr('id', `editBtn-${comment.id}`);
    let editSymbol = $('<i></i>').addClass('fa-solid fa-pencil text-secondary small');
    editBtn.on('click', () => {
        edit(editBtn);
    })
    editBtn.append(editSymbol);

    let deleteCommentBtn = $('<a></a>').addClass('delete-comment-btn btn-link text-decoration-none ms-2')
        .attr('type', 'button').attr('id', `deleteCommentBtn-${comment.id}`)
        .attr('tabindex', '0').attr('data-bs-toggle', 'popover').attr('data-bs-trigger', 'focus')
        .attr('data-bs-title', `${getMessage('comment.delete')}?`)
        .attr('data-bs-content', `"<div class='text-center'><a type='button' class='btn btn-sm btn-secondary me-2'>${getMessage('cancel')}</a><a type='button' class='btn btn-sm btn-danger del-com'>${getMessage('delete')}</a></div>"`)
        .attr('data-bs-html', 'true')
    let deleteSymbol = $('<i></i>').addClass('fa-solid fa-trash-can text-danger small').attr('title', getMessage('delete'));
    deleteCommentBtn.append(deleteSymbol);
    deleteCommentBtn.on('shown.bs.popover', (event) => {
        $('.del-com').on('click', () => deleteComment($(event.target).attr('id').replace('deleteCommentBtn-', '')));
    });


    buttonsDiv.append(likeBtn);
    buttonsDiv.append(replyBtn);
    buttonsDiv.append(editBtn);
    buttonsDiv.append(deleteCommentBtn);
    commentDiv.append(dFlexDiv);
    commentDiv.append(textDiv);
    commentDiv.append(buttonsDiv);

    return commentDiv;
}

function formatDateTime(dateTime) {
    let dateAndTime = dateTime.split('T');
    let dateParts =  dateAndTime[0].split('-');
    let formattedDate = `${dateParts[2]}.${dateParts[1]}.${dateParts[0]}`;
    let formattedTime = dateAndTime[1].split('.')[0];
    return `${formattedDate} ${formattedTime}`;
}

function like(likeBtn, commentId) {
    if (authUser != null) {
        let likeIcon = $(likeBtn).find('i');
        let likeCounter = $(likeBtn).find('span');
        let liked = likeIcon.attr('class').includes('fa-regular');
        $.ajax({
            url: `/projects/${projectId}/comments/${commentId}/like`,
            type: "PATCH",
            data: "liked=" + liked
        }).done(function() {
            likeIcon.removeClass(liked ? 'fa-regular' : 'fa-solid').addClass(liked ? 'fa-solid' : 'fa-regular');
            likeCounter.text(+(likeCounter.text()) + (liked ? 1 : -1));
        }).fail(function(data) {
            likeIcon.removeClass(liked ? 'fa-solid' : 'fa-regular').addClass(liked ? 'fa-regular' : 'fa-solid');
            handleError(data, getMessage(liked ? 'comment.failed-to-like' : 'comment.failed-to-dislike'));
        });
    } else  {
        $('.with-popover').popover('hide');
        $(likeBtn).popover('toggle');
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
            handleError(data, getMessage(liked ? 'project.failed-to-like' : 'project.failed-to-dislike'));
        });
    } else  {
        $('.with-popover').popover('hide');
        $(likeBtn).popover('toggle');
    }
}



function deleteComment(id) {
    $.ajax({
        url: `/projects/${projectId}/comments/${id}`,
        type: "DELETE"
    }).done(function() {
        let commentDiv = $(`#comment-${id}`);
        commentDiv.find('.comment-text').addClass('text-secondary-emphasis').css('font-style', 'italic')
            .text(getMessage('comment.deleted'));
        commentDiv.find('.comment-actions').remove();
    }).fail(function(data) {
        handleError(data, getMessage('comment.failed-to-delete'));
    });
}

function edit(editBtn) {
    $('#newCommentDiv').addClass('opacity-50');
    newCommentInput.val('');
    $('#sendNewBtn').attr('hidden', true);

    $('#replyCommentDiv').remove();

    let editCommentDiv = $('.editCommentDiv');
    if (editCommentDiv.length) {
        let editCommentId = editCommentDiv.attr('id').replace('editCommentDiv-', '');
        $(`#comment-${editCommentId}`).attr('hidden', false);
        editCommentDiv.remove();
    }

    let commentDiv = editBtn.closest('.comment');
    let commentId = commentDiv.attr('id').replace('comment-', '');
    let text = commentDiv.find('.comment-text').text();
    commentDiv.attr('hidden', true);

    let div = $('<div></div>').addClass('col-12 col-md-6 mt-3 py-1 editCommentDiv')
        .css('margin-left', commentDiv.css('margin-left'))
        .attr('id', `editCommentDiv-${commentId}`);
    let textArea = $('<textarea></textarea>').addClass('form-control bg-light-subtle pb-4')
        .attr('id', 'editComment').css('resize', 'none').attr('placeholder', getMessage('comment.leave-comment-here'))
        .attr('rows', '2').text(text);

    let sendEditBtn = $('<a></a>').addClass('btn btn-link text-decoration-none float-end pe-2').css('margin-top', '-35px')
        .attr('type', 'button').attr('id', 'sendEditBtn').html(getMessage('comment.send'));
    let cancelEditBtn = $('<a></a>').addClass('btn btn-link link-secondary text-decoration-none float-end').css('margin-top', '-35px')
        .attr('type', 'button').attr('id', 'cancelEditBtn').html(getMessage('cancel'));
    div.append(textArea);
    div.append(sendEditBtn);
    div.append(cancelEditBtn);
    div.insertAfter(commentDiv);
    textArea.css('height', textArea.prop('scrollHeight') + 2 + 'px');

    $('#editComment').on('input', (event) => {
        $(event.target).css('height', 'auto');
        $(event.target).css('height', $(event.target).prop('scrollHeight') + 2 + 'px');
        $('#sendEditBtn').attr('hidden', $(event.target).val().length === 0);
    });

    $('#cancelEditBtn').on('click', () => {
        $(`#editCommentDiv-${commentId}`).remove();
        commentDiv.attr('hidden', false);
        $('#newCommentDiv').removeClass('opacity-50');
    });

    $('#sendEditBtn').on('click', () => {
        let text = $('#editComment').val();

        $.ajax({
            url: `/projects/${projectId}/comments/${commentId}`,
            type: "PUT",
            data: "text=" + text
        }).done(function() {
            div.remove();
            commentDiv.find('.comment-text').text(text).css('white-space', 'pre-wrap');
            commentDiv.attr('hidden', false);
            $('#newCommentDiv').removeClass('opacity-50');
        }).fail(function(data) {
            handleError(data, getMessage('comment.failed-to-update'));
        });
    });
}
