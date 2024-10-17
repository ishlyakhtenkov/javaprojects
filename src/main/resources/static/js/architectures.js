function deleteArchitecture(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/management/reference/architectures/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, getMessage('architecture.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('architecture.failed-to-delete', [name]));
    });
}