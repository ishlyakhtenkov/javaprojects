$('.image-pop').on('click', (event) => {
    $('#imagePreview').attr('src', $(event.target).attr('src'));
    $('#imageModal').modal('show');
});
