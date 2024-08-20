function deleteFile() {
    $('#editFileInput').attr('hidden', true);
    $('#fileInputDiv').html('<div class="input-group custom-file-button">' +
        '<label class="input-group-text bg-light" for="fileInput">Logo</label>' +
        '<input type="file" accept=".svg,.png" id="fileInput" name="logoFile" ' +
        'class="form-control text-muted" required onchange="$(\'#fileInput\').removeClass(\'text-muted\')" /></div>');
}
