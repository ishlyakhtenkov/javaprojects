const technologiesSelector = $('#technologiesSelector');
const technologiesPreviewDiv = $('#technologiesPreviewDiv');
const logoFilePreviewDiv = $('#logoFilePreviewDiv');
const logoFileInputDiv = $('#logoFileInputDiv');
const dockerFilePreviewDiv = $('#dockerFilePreviewDiv');
const dockerFileInputDiv = $('#dockerFileInputDiv');
const cardImageFilePreviewDiv = $('#cardImageFilePreviewDiv');
const cardImageFileInputDiv = $('#cardImageFileInputDiv');
const visibilityCheckbox = $('#visibilityCheckbox');
const visibilityCheckboxDesc = $('#visibilityCheckboxDesc');
const shortDescriptionInput = $('#shortDescriptionInput');
const characterCounter = $('#characterCounter');

$(technologiesSelector).on('changed.bs.select', (event, clickedIndex, isSelected, previousValue) => {
    let technologyId = technologiesSelector.prop('options')[clickedIndex].value;
    if (isSelected) {
        let technologyName = technologiesSelector.prop('options')[clickedIndex].dataset.name;
        let technologyFileLink = technologiesSelector.prop('options')[clickedIndex].dataset.filelink;
        let techSpan = $('<span></span>').addClass('badge text-bg-light me-2 mt-2').attr('id', `techSpan-${technologyId}`)
            .html(`<img src="/${technologyFileLink}" width="32" height="32" class="align-bottom" /> ${technologyName}`);
        technologiesPreviewDiv.append(techSpan);
    } else {
        $(`#techSpan-${technologyId}`).remove();
    }
});

$('.url').on('keyup', (event) => {
    $(event.target).siblings('.url-check').attr('href', $(event.target).val());
    $(event.target).siblings('.url-check').attr('hidden', $(event.target).val().length === 0);
});

$('.date-input').on('change', (event) => {
    if ($(event.target).val().length) {
        $(event.target).removeClass('text-muted');
    } else {
        $(event.target).addClass('text-muted');
    }
});

visibilityCheckbox.on('click', () => {
    visibilityCheckboxDesc.html(`${visibilityCheckbox.prop('checked') === true ? 'Visible to users' : 'Not visible to users'}`);
});

shortDescriptionInput.on('keyup', () => {
    characterCounter.text(`${128 - shortDescriptionInput.val().length} characters left`);
});

function deleteLogoFile() {
    logoFilePreviewDiv.attr('hidden', true);
    logoFileInputDiv.html('<div class="input-group custom-file-button">' +
        '<label class="input-group-text bg-light project-card-label">Logo file</label>' +
        '<input type="file" accept="image/*" id="logoFileInput" name="logoFile" ' +
        'class="form-control text-muted" required onchange="previewLogoFile()" /></div>').attr('hidden', false);
}

function previewLogoFile() {
    let file = $('#logoFileInput').prop('files');
    if (file) {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            $('#logoFilePreview').attr('src', event.target.result);
        }
        fileReader.readAsDataURL(file[0]);
        $('#logoFileName').text(file[0].name);
    }
    logoFilePreviewDiv.attr('hidden', false);
    logoFileInputDiv.attr('hidden', true);
}


function deleteDockerFile() {
    dockerFilePreviewDiv.attr('hidden', true);
    dockerFileInputDiv.html('<div class="input-group custom-file-button">' +
        '<label class="input-group-text bg-light project-card-label">Docker file</label>' +
        '<input type="file" accept=".yaml,.yml" id="dockerFileInput" name="dockerComposeFile" ' +
        'class="form-control text-muted" onchange="previewDockerFile()" /></div>').attr('hidden', false);
}

function previewDockerFile() {
    let file = $('#dockerFileInput').prop('files');
    if (file) {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            $('#dockerComposeFileRef').attr('href', event.target.result).attr('download', file[0].name)
                .html(`<i class='text-primary me-1 fa-brands fa-docker'></i>${file[0].name}`);
        }
        fileReader.readAsDataURL(file[0]);
    }
    dockerFilePreviewDiv.attr('hidden', false);
    dockerFileInputDiv.attr('hidden', true);
}

function deleteCardImageFile() {
    cardImageFilePreviewDiv.attr('hidden', true);
    cardImageFileInputDiv.html('<div class="input-group custom-file-button">' +
        '<label class="input-group-text bg-light project-card-label">Card image file</label>' +
        '<input type="file" accept="image/*" id="cardImageFileInput" name="cardImageFile" ' +
        'class="form-control text-muted" required onchange="previewCardImageFile()" /></div>').attr('hidden', false);
    $('#cardImageFileLargePreview').attr('src', '');
}

function previewCardImageFile() {
    let file = $('#cardImageFileInput').prop('files');
    if (file) {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            $('#cardImageFilePreview').attr('src', event.target.result);
            $('#cardImageFileLargePreview').attr('src', event.target.result);
        }
        fileReader.readAsDataURL(file[0]);
        $('#cardImageFileName').text(file[0].name);
    }
    cardImageFilePreviewDiv.attr('hidden', false);
    cardImageFileInputDiv.attr('hidden', true);
}
