const descriptionInput = $('#descriptionInput');
const characterCounter = $('#characterCounter');
const descriptionMaxSize = 400;

descriptionInput.on('keyup', () => {
    characterCounter.text(`${descriptionMaxSize - descriptionInput.val().length} ${getMessage('info.characters-left')}`);
});
