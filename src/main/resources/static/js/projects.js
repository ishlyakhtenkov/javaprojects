function deleteProject(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `/management/projects/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, getMessage('project.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-delete', [name]));
    });
}

function enableProject(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: `/management/projects/${id}`,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(getMessage(enabled ? 'project.enabled' : 'project.disabled', [name]));
        $(checkbox).prop('title', getMessage(enabled ? 'project.disable' : 'project.enable'));
    }).fail(function(data) {
        $(checkbox).prop('checked', !enabled);
        handleError(data, getMessage(enabled ? 'project.failed-to-enable' : 'project.failed-to-disable', [name]));
    });
}
