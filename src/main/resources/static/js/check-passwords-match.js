const password = $("#password");
const repeatPassword = $("#repeatPassword");
const passwordMatchError = $("#passwordMatchError");
const confirmButton = $("#confirmBtn");

repeatPassword.on('keyup', function(){
    checkPasswordsMatch();
});

password.on('keyup', function(){
    checkPasswordsMatch();
});

function checkPasswordsMatch() {
    let passwordValue = password.val();
    let repeatPasswordValue = repeatPassword.val();
    if (repeatPasswordValue.length && passwordValue !== repeatPasswordValue) {
        repeatPassword.addClass('is-invalid');
        passwordMatchError.html('<li>password does not match</li>');
        confirmButton.prop('disabled', true);
    }
    else {
        repeatPassword.removeClass('is-invalid');
        passwordMatchError.empty();
        confirmButton.prop('disabled', false);
    }
}
