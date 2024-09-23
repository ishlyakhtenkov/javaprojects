const changePasswordModal = $('#changePasswordModal');
const editProfileModal = $('#editProfileModal');
const userName = $('#userName');
const userNameFirstLetter = $('#userNameFirstLetter');
const profileButton = $('#profileButton');
const changeEmailModal = $('#changeEmailModal');

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

editProfileModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#nameInput').val(userName.text());
});

function updateProfile() {
    let name = editProfileModal.find('#nameInput').val();
    if (name.length) {
        $.ajax({
            url: "profile/update",
            type: "PATCH",
            data: "name=" + name
        }).done(function () {
            userName.text(name);
            userNameFirstLetter.text(name.substring(0, 1).toUpperCase());
            profileButton.attr('title', name).find('span').text(name.substring(0, 1).toUpperCase());
            editProfileModal.modal('toggle');
            successToast('Profile was updated');
        }).fail(function(data) {
            handleError(data, 'Failed to update profile');
        });
    }
}

changeEmailModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#email').val('');
});

function changeEmail() {
    let email = changeEmailModal.find('#email').val();
    if (email.length) {
        $.ajax({
            url: "profile/change-email",
            type: "POST",
            data: "email=" + email
        }).done(function () {
            changeEmailModal.modal('toggle');
            successToast('Use the message sent to confirm your new email');
        }).fail(function(data) {
            handleError(data, 'Failed to change email');
        });
    }
}
