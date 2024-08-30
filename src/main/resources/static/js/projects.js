function deleteProject(delButton, id) {
    let name = delButton.dataset.name;
    $.ajax({
        url: `projects/${id}`,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Project ${name} has been deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete project ${name}`);
    });
}

function enableProject(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: `projects/${id}`,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(`Project ${name} has been ${enabled ? 'enabled' : 'disabled'}`);
        $(checkbox).prop('title', `${enabled ? 'Disable' : 'Enable'} project`);
    }).fail(function(data) {
        $(checkbox).prop('checked', !enabled);
        handleError(data, `Failed to ${enabled ? 'enable' : 'disable'} project ${name}`);
    });
}
