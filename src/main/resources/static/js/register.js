const password = $("#password");
const repeatPassword = $("#repeatPassword");
const passwordMatchError = $("#passwordMatchError");
const registerBtn = $("#registerBtn");

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
        registerBtn.prop('disabled', true);
    }
    else {
        repeatPassword.removeClass('is-invalid');
        passwordMatchError.empty();
        registerBtn.prop('disabled', false);
    }
}