$('#urlInput').on('keyup', (event) => {
    let urlCheck = $('#urlCheck');
    urlCheck.attr('href', $(event.target).val());
    urlCheck.attr('hidden', $(event.target).val().length === 0);
});
