const descriptionInput = $('#descriptionInput');
const characterCounter = $('#characterCounter');

descriptionInput.on('keyup', () => {
    characterCounter.text(`${400 - descriptionInput.val().length} characters left`);
});
