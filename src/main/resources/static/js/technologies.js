function deleteTechnology(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/management/reference/technologies/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, getMessage('technology.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('technology.failed-to-delete', [name]));
    });
}