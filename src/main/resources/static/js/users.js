const changePasswordModal = $('#changePasswordModal');

changePasswordModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#password').val('');
    $(e.currentTarget).find('#repeatPassword').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#passwordMatchError').text('');
    $(e.currentTarget).find('#confirmBtn').prop('disabled', false);
    $(e.currentTarget).find('#changePasswordModalUserId').val($(e.relatedTarget).data('id'));
    let userName = $(e.relatedTarget).data('name');
    $(e.currentTarget).find('#changePasswordModalUserName').val(userName);
    $(e.currentTarget).find('#changePasswordModalLabel').text(`Change password for ${userName}`);
});

function changePassword() {
    let id = changePasswordModal.find('#changePasswordModalUserId').val();
    let name = changePasswordModal.find('#changePasswordModalUserName').val();
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: `users/change-password/${id}`,
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(`Password for ${name} has been changed`);
        }).fail(function (data) {
            handleError(data, `Failed to change password for ${name}`);
        });
    }
}

function enableUser(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: `users/${id}`,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(`User ${name} has been ${enabled ? 'enabled' : 'disabled'}`);
        $(checkbox).prop('title', `${enabled ? 'Disable' : 'Enable'} user`);
        if (!enabled) {
            $(`#row-${id}`).find('.online-circle').removeClass('text-success').addClass('text-danger').prop('title', 'offline');
        }
    }).fail(function(data) {
        $(checkbox).prop('checked', !enabled);
        handleError(data, `Failed to ${enabled ? 'enable' : 'disable'} user ${name}`);
    });
}

function deleteUser(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `users/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `User ${name} has been deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete user ${name}`);
    });
}
