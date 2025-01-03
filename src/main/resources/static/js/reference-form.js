const filePreviewDiv = $('#filePreviewDiv');
const fileInputDiv = $('#fileInputDiv');

function previewFile() {
    let file = $('#fileInput').prop('files');
    if (file) {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            $(filePreviewDiv).find('img').attr('src', event.target.result);
        }
        fileReader.readAsDataURL(file[0]);
        $('#fileName').text(file[0].name);
    }
    filePreviewDiv.attr('hidden', false);
    fileInputDiv.attr('hidden', true);
}

function deleteFile() {
    filePreviewDiv.attr('hidden', true);
    fileInputDiv.html('<div class="input-group custom-file-button">' +
        `<label class="input-group-text text-start d-inline-block text-truncate w-25" for="fileInput">${getMessage('logo')}</label>` +
        '<input type="file" accept="image/*" id="fileInput" name="logo.inputtedFile" ' +
        'class="form-control text-muted" required onchange="previewFile()" /></div>').attr('hidden', false);
}
