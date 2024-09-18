function deleteArchitecture(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/management/reference/architectures/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Architecture ${name} has been deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete architecture ${name}`);
    });
}