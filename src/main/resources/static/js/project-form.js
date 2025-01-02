const technologiesSelector = $('#technologiesSelector');
const technologiesPreviewDiv = $('#technologiesPreviewDiv');
const logoFilePreviewDiv = $('#logoFilePreviewDiv');
const logoFileInputDiv = $('#logoFileInputDiv');
const dockerFilePreviewDiv = $('#dockerFilePreviewDiv');
const dockerFileInputDiv = $('#dockerFileInputDiv');
const previewFilePreviewDiv = $('#previewFilePreviewDiv');
const previewFileInputDiv = $('#previewFileInputDiv');
const visibilityCheckbox = $('#visibilityCheckbox');
const visibilityCheckboxDesc = $('#visibilityCheckboxDesc');
const annotationInput = $('#annotationInput');
const characterCounter = $('#characterCounter');

$(technologiesSelector).on('changed.bs.select', (event, clickedIndex, isSelected, previousValue) => {
    let technologyId = technologiesSelector.prop('options')[clickedIndex].value;
    if (isSelected) {
        let technologyName = technologiesSelector.prop('options')[clickedIndex].dataset.name;
        let technologyFileLink = technologiesSelector.prop('options')[clickedIndex].dataset.filelink;
        let technologyUrl = technologiesSelector.prop('options')[clickedIndex].dataset.url;

        let techSpan = $('<span></span>').addClass('badge bg-body-tertiary me-2 mt-2').attr('id', `techSpan-${technologyId}`);
        let techBtnLink = $('<a></a>').addClass('link-underline link-underline-opacity-0 link-underline-opacity-75-hover link-body-emphasis')
            .attr('type', 'button').attr('href', technologyUrl).attr('target', '_blank')
            .html(`<img src="/${technologyFileLink}" width="32" height="32" class="align-bottom me-1" />${technologyName}`);
        techSpan.append(techBtnLink);

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

annotationInput.on('keyup', () => {
    characterCounter.text(`${128 - annotationInput.val().length} ${getMessage('info.characters-left')}`);
});

function deleteLogoFile() {
    logoFilePreviewDiv.attr('hidden', true);
    logoFileInputDiv.html('<div class="input-group custom-file-button">' +
        `<label class="input-group-text project-form-label">${getMessage('logo')}</label>` +
        '<input type="file" accept="image/*" id="logoFileInput" name="logo.inputtedFile" ' +
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
        `<label class="input-group-text d-inline-block text-truncate project-form-label">${getMessage('project.docker-compose-file')}</label>` +
        '<input type="file" accept=".yaml,.yml" id="dockerFileInput" name="dockerCompose.inputtedFile" ' +
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

function previewImage(imageInput) {
    let files = imageInput.prop('files');
    if (files.length) {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            imageInput.siblings('img').attr('src', event.target.result).attr('hidden', false).data('filename', files[0].name);
            imageInput.siblings('.change-img-btn').attr('hidden', false);
            imageInput.siblings('.empty-image-div').attr('style', 'display: none !important;');
        }
        fileReader.readAsDataURL(files[0]);
    } else {
        imageInput.siblings('.empty-image-div').removeClass('bg-danger-subtle border-2 border-danger')
            .attr('style', 'display: inline !important; height: 150px; width: 300px;');
        imageInput.siblings('img').attr('hidden', true);
        imageInput.siblings('.change-img-btn').attr('hidden', true);
        imageInput.attr('required', true);
    }
}

function moveElementUp(moveUpBtn) {
    let elementContainer = $(moveUpBtn).closest('.element-container');
    let elementContainerId = elementContainer.attr('id');
    let elementContainerIndex = +elementContainerId.replace('elementContainer-', '');
    if (elementContainerIndex !== 0) {
        let higherElementContainer = $(`#elementContainer-${elementContainerIndex - 1}`);
        let higherElementContainerId = higherElementContainer.attr('id');
        let higherElementContainerIndex = +higherElementContainerId.replace('elementContainer-', '');

        swapElements(elementContainerIndex, higherElementContainerIndex);
    }
}

function moveElementDown(moveDownBtn) {
    let elementContainer = $(moveDownBtn).closest('.element-container');
    let elementContainerId = elementContainer.attr('id');
    let elementContainerIndex = +elementContainerId.replace('elementContainer-', '');
    let lowerElementContainer = $(`#elementContainer-${elementContainerIndex + 1}`);
    if (lowerElementContainer.length) {
        let lowerElementContainerId = lowerElementContainer.attr('id');
        let lowerElementContainerIndex = +lowerElementContainerId.replace('elementContainer-', '');

        swapElements(elementContainerIndex, lowerElementContainerIndex);
    }
}

function swapElements(firstElemIndex, secondElemIndex) {
    let firstElementId = $(`#elementId-${firstElemIndex}`);
    let firstElementType = $(`#elementType-${firstElemIndex}`);
    let firstElementIndex = $(`#elementIndex-${firstElemIndex}`);
    let firstElementFileName = $(`#elementFileName-${firstElemIndex}`);
    let firstElementFileLink = $(`#elementFileLink-${firstElemIndex}`);
    let firstElementText = $(`#elementText-${firstElemIndex}`);
    let firstElementImage = $(`#elementImage-${firstElemIndex}`);
    let firstElementImageBytes = $(`#elementImageBytes-${firstElemIndex}`);

    let secondElementId = $(`#elementId-${secondElemIndex}`);
    let secondElementType = $(`#elementType-${secondElemIndex}`);
    let secondElementIndex = $(`#elementIndex-${secondElemIndex}`);
    let secondElementFileName = $(`#elementFileName-${secondElemIndex}`);
    let secondElementFileLink = $(`#elementFileLink-${secondElemIndex}`);
    let secondElementText = $(`#elementText-${secondElemIndex}`);
    let secondElementImage = $(`#elementImage-${secondElemIndex}`);
    let secondElementImageBytes = $(`#elementImageBytes-${secondElemIndex}`);

    firstElementId.attr('id', `elementId-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].id`);
    firstElementType.attr('id', `elementType-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].type`);
    firstElementIndex.attr('id', `elementIndex-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].index`);
    if (firstElementText.length) {
        firstElementText.attr('id', `elementText-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].text`);
    }
    if (firstElementFileName.length) {
        firstElementFileName.attr('id', `elementFileName-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].image.fileName`);
    }
    if (firstElementFileLink.length) {
        firstElementFileLink.attr('id', `elementFileLink-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].image.fileLink`);
    }
    if (firstElementImage.length) {
        firstElementImage.attr('id', `elementImage-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].image.inputtedFile`);
    }
    if (firstElementImageBytes.length) {
        firstElementImageBytes.attr('id', `elementImageBytes-${secondElemIndex}`).attr('name', `descriptionElementTos[${secondElemIndex}].image.inputtedFileBytes`);
    }

    secondElementId.attr('id', `elementId-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].id`);
    secondElementType.attr('id', `elementType-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].type`);
    secondElementIndex.attr('id', `elementIndex-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].index`);
    if (secondElementText.length) {
        secondElementText.attr('id', `elementText-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].text`);
    }
    if (secondElementFileName.length) {
        secondElementFileName.attr('id', `elementFileName-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].image.fileName`);
    }
    if (secondElementFileLink.length) {
        secondElementFileLink.attr('id', `elementFileLink-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].image.fileLink`);
    }
    if (secondElementImage.length) {
        secondElementImage.attr('id', `elementImage-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].image.inputtedFile`);
    }
    if (secondElementImageBytes.length) {
        secondElementImageBytes.attr('id', `elementImageBytes-${firstElemIndex}`).attr('name', `descriptionElementTos[${firstElemIndex}].image.inputtedFileBytes`);
    }

    let firstElementIndexValue = firstElementIndex.val();
    firstElementIndex.val(secondElementIndex.val());
    secondElementIndex.val(firstElementIndexValue);

    let firstElementContainer = $(`#elementContainer-${firstElemIndex}`);
    let secondElementContainer = $(`#elementContainer-${secondElemIndex}`);
    if (firstElemIndex > secondElemIndex) {
        firstElementContainer.insertBefore(secondElementContainer);
    } else {
        firstElementContainer.insertAfter(secondElementContainer);
    }
    firstElementContainer.attr('id', `elementContainer-${secondElemIndex}`);
    secondElementContainer.attr('id', `elementContainer-${firstElemIndex}`);
}

function deleteElement(deleteElementBtn) {
    let elementContainer = $(deleteElementBtn).closest('.element-container');
    let elementContainerId = elementContainer.attr('id');
    let elementContainerIndex = +elementContainerId.replace('elementContainer-', '');

    let elementIndexValue = $(`#elementIndex-${elementContainerIndex}`).val();
    let elementContainerAmount = $('.element-container').length;
    elementContainer.remove();
    for (let i = elementContainerIndex + 1; i < elementContainerAmount; i++) {
        let lowerElementContainer = $(`#elementContainer-${i}`);
        if (lowerElementContainer.length) {
            let lowerElementId = $(`#elementId-${i}`);
            let lowerElementType = $(`#elementType-${i}`);
            let lowerElementIndex = $(`#elementIndex-${i}`);
            let lowerElementFileName = $(`#elementFileName-${i}`);
            let lowerElementFileLink = $(`#elementFileLink-${i}`);
            let lowerElementText = $(`#elementText-${i}`);
            let lowerElementImage = $(`#elementImage-${i}`);
            let lowerElementImageBytes = $(`#elementImageBytes-${i}`);

            lowerElementId.attr('id', `elementId-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].id`);
            lowerElementType.attr('id', `elementType-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].type`);
            lowerElementIndex.attr('id', `elementIndex-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].index`);
            if (lowerElementText.length) {
                lowerElementText.attr('id', `elementText-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].text`);
            }
            if (lowerElementFileName.length) {
                lowerElementFileName.attr('id', `elementFileName-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].image.fileName`);
            }
            if (lowerElementFileLink.length) {
                lowerElementFileLink.attr('id', `elementFileLink-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].image.fileLink`);
            }
            if (lowerElementImage.length) {
                lowerElementImage.attr('id', `elementImage-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].image.inputtedFile`);
            }
            if (lowerElementImageBytes.length) {
                lowerElementImageBytes.attr('id', `elementImageBytes-${i - 1}`).attr('name', `descriptionElementTos[${i - 1}].image.inputtedFileBytes`);
            }

            let lowerElementIndexValue = lowerElementIndex.val();
            lowerElementIndex.val(elementIndexValue);
            elementIndexValue = lowerElementIndexValue;

            lowerElementContainer.attr('id', `elementContainer-${i - 1}`);
        }
    }
}

function addNewElement(type) {
    let newElementContainerIndex = $('.element-container').length;
    let newElementContainer = $('<div></div>').attr('id', `elementContainer-${newElementContainerIndex}`)
        .addClass('element-container mb-1');

    let newElementType = $('<input type="hidden"/>').attr('id', `elementType-${newElementContainerIndex}`)
        .attr('name', `descriptionElementTos[${newElementContainerIndex}].type`).val(type.toUpperCase());

    let newElementIndexValue = newElementContainerIndex === 0 ? 0 :
        (+($(`#elementIndex-${newElementContainerIndex - 1}`).val()) + 1);

    let newElementIndex = $('<input type="hidden"/>').attr('id', `elementIndex-${newElementContainerIndex}`)
        .attr('name', `descriptionElementTos[${newElementContainerIndex}].index`)
        .val(newElementIndexValue);

    let newElementInputContentDiv = $('<div></div>');

    if (type === 'Title' || type === 'Paragraph') {
        let formDiv = $('<div></div>').addClass('form-floating');
        let newElementText = $('<textarea></textarea>').attr('id', `elementText-${newElementContainerIndex}`)
            .attr('name', `descriptionElementTos[${newElementContainerIndex}].text`).attr('required', true)
            .attr('placeholder', `${type}`).addClass('form-control lh-base')
            .addClass(`${type === 'Title' ? 'fw-medium' : ''}`)
            .css('white-space', 'pre-wrap').css('height', `${type === 'Title' ? '65px' : '120px'}`);
        let label = $('<label></label>').addClass('text-muted').html(getMessage(`project.description-elements.${type.toLowerCase()}`));
        formDiv.append(getElementActionsBtnHtml(type));
        formDiv.append(newElementText);
        formDiv.append(label);
        newElementInputContentDiv.append(formDiv);

    } else if (type === 'Image') {
        newElementInputContentDiv.addClass('border rounded-2 ps-3 pe-2 pb-1');

        let label = $('<div></div>').addClass('text-start text-muted tiny').html(getMessage(`project.description-elements.${type.toLowerCase()}`));

        let flexDiv = $('<div></div>').addClass('d-flex align-items-start');

        let inputtedImage = $('<img />').addClass('element-image rounded-2 mt-2 mb-1 border')
            .attr('hidden', true).css('max-height', '150px').css('cursor', 'zoom-in').css('max-width', '95%')
            .click(function () {zoomImage($(this))});

        let imageInput = $('<input type="file"/>').attr('accept', 'image/*')
            .attr('id', `elementImage-${newElementContainerIndex}`).attr('required', true)
            .attr('name', `descriptionElementTos[${newElementContainerIndex}].image.inputtedFile`)
            .attr('hidden', true).addClass('element-image-input').change(function () {previewImage($(this))});

        let changeImageBtn = $('<button></button>').attr('type', 'button').attr('title', getMessage('change-image'))
            .attr('hidden', true)
            .addClass('change-img-btn btn btn-link opacity-75 link-underline-opacity-0 link-secondary pt-1')
            .html(`<i class="fa-solid fa-pencil"></i>`).click(function () {$(this).siblings('input').click()});

        let emptyImageDiv = $('<div></div>')
            .addClass('empty-image-div border rounded-2 mt-2 mb-1 align-content-center text-center')
            .css('height', '150px').css('width', '300px');

        let chooseImageBtn = $('<button></button>').attr('type', 'button')
            .addClass('btn btn-outline-secondary').html(getMessage('choose-image'))
            .click(function () {$(this).parent().siblings('input').click()});

        emptyImageDiv.append(chooseImageBtn);

        flexDiv.append(inputtedImage).append(imageInput).append(changeImageBtn).append(emptyImageDiv);

        newElementInputContentDiv.append(getElementActionsBtnHtml(type)).append(label).append(flexDiv);
    }

    newElementContainer.append(newElementType);
    newElementContainer.append(newElementIndex);
    newElementContainer.append(newElementInputContentDiv);

    $('#elementsBlock').append(newElementContainer);
}

function getElementActionsBtnHtml(type) {
    return `<button type="button"
                          class="${type !== 'Image' ? 'position-absolute' : ''}
                          float-end btn btn-link link-secondary opacity-75 link-underline-opacity-0 p-0 dropdown-toggle"
                          title="${getMessage('edit')}" data-bs-toggle="dropdown" aria-expanded="false" style="right: 10px;">
                          <i class="fa-solid fa-ellipsis-vertical"></i>
                        </button>
                        <ul class="dropdown-menu">
                          <li>
                            <button type="button" class="dropdown-item" onclick="moveElementUp(this)">
                               <i class="fa-solid fa-up-long text-warning"></i> <span>${getMessage('project.description-elements.move-up')}</span>
                            </button>
                          </li>
                          <li>
                             <button type="button" class="dropdown-item" onclick="moveElementDown(this)">
                                <i class="fa-solid fa-down-long text-warning"></i> <span>${getMessage('project.description-elements.move-down')}</span>
                             </button>
                          </li>
                          <li>
                             <button type="button" class="dropdown-item" onclick="deleteElement(this)">
                                <i class="fa-solid fa-xmark text-danger"></i> <span>${getMessage('delete')}</span>
                             </button>
                          </li>
                        </ul>`
}

function checkImageElementsNotEmpty() {
    let hasEmptyImageElements = false;
    $('.element-image-input').each(function () {
        if ($(this).attr('required') && !$(this).val().length) {
            $(this).siblings('.empty-image-div').addClass('bg-danger-subtle border-2 border-danger');
            hasEmptyImageElements = true;
        }
    });
    if (hasEmptyImageElements) {
        failToast(getMessage('info.empty-image-elements'));
    }
}
