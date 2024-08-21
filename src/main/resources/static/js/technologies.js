function deleteTechnology(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/references/technologies/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Technology ${name} has been deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete technology ${name}`);
    });
}