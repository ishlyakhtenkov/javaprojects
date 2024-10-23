function deleteProject(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/projects/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, getMessage('project.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-delete', [name]));
    });
}

function revealProject(checkbox, id) {
    let visible = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: `/projects/${id}`,
        type: "PATCH",
        data: "visible=" + visible
    }).done(function() {
        successToast(getMessage(visible ? 'project.has-been-revealed' : 'project.has-been-hided', [name]));
        $(checkbox).prop('title', getMessage(visible ? 'project.hide' : 'project.reveal'));
    }).fail(function(data) {
        $(checkbox).prop('checked', !visible);
        handleError(data, getMessage(visible ? 'project.failed-to-reveal' : 'project.failed-to-hide', [name]));
    });
}
