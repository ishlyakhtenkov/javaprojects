const changePasswordModal = $('#changePasswordModal');

changePasswordModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#password').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#repeatPassword').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#passwordMatchError').text('');
    $(e.currentTarget).find('#confirmBtn').prop('disabled', false);
    $(e.currentTarget).find('#changePasswordModalUserId').val($(e.relatedTarget).data('id'));
    let userName = $(e.relatedTarget).data('name');
    $(e.currentTarget).find('#changePasswordModalUserName').val(userName);
    $(e.currentTarget).find('#changePasswordModalLabel').text(getMessage('user.change-password-for', [userName]));
});

function changePassword() {
    let id = changePasswordModal.find('#changePasswordModalUserId').val();
    let name = changePasswordModal.find('#changePasswordModalUserName').val();
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: `/management/users/change-password/${id}`,
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(getMessage('user.password-changed-for', [name]));
        }).fail(function (data) {
            handleError(data, getMessage('user.failed-to-change-password-for', [name]));
        });
    }
}

function enableUser(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: `/management/users/${id}`,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(getMessage(enabled ? 'user.enabled' : 'user.disabled', [name]));
        $(checkbox).prop('title', getMessage(enabled ? 'user.disable' : 'user.enable'));
        if (!enabled) {
            $(`#row-${id}`).find('.online-circle').removeClass('text-success').addClass('text-danger').prop('title', 'offline');
        }
    }).fail(function(data) {
        $(checkbox).prop('checked', !enabled);
        handleError(data, getMessage(enabled ? 'user.failed-to-enable' : 'user.failed-to-disable', [name]));
    });
}

function deleteUser(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/management/users/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, getMessage('user.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('user.failed-to-delete', [name]));
    });
}
