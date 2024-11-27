function hideProject(checkbox) {
    let id = checkbox.data('id');
    let name = checkbox.data('name');
    let visible = checkbox.prop('checked');
    $.ajax({
        url: `/projects/${id}`,
        type: "PATCH",
        data: "visible=" + visible
    }).done(function() {
        successToast(getMessage(visible ? 'project.has-been-revealed' : 'project.has-been-hided', [name]));
        checkbox.prop('title', getMessage(visible ? 'project.hide' : 'project.reveal'));
    }).fail(function(data) {
        checkbox.prop('checked', !visible);
        handleError(data, getMessage(visible ? 'project.failed-to-reveal' : 'project.failed-to-hide', [name]));
    });
}
