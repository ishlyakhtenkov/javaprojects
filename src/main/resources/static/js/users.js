const changePasswordModal = $('#changePasswordModal');

$(window).on('load', () => setupPopovers());

function setupPopovers() {
    let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
        new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
    document.querySelectorAll('[data-bs-toggle="popover"]')
        .forEach(e => e.setAttribute('title', e.getAttribute('data-bs-original-title')));
}

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
