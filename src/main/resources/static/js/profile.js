const changePasswordModal = $('#changePasswordModal');

changePasswordModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#password').val('');
    $(e.currentTarget).find('#repeatPassword').val('').removeClass('is-invalid');
    $(e.currentTarget).find('#passwordMatchError').text('');
    $(e.currentTarget).find('#confirmBtn').prop('disabled', false);
});

function changePassword() {
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: "profile/change-password",
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast('Password has been changed');
        }).fail(function(data) {
            handleError(data, 'Failed to change password');
        });
    }
}
