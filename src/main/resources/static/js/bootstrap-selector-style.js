const selector = $('#selector');
let selectorFormControl = null;
let selectorTitle = null;

selector.on('loaded.bs.select', () => {
    selectorFormControl = $('.bootstrap-select.form-control');
    selectorTitle = $('.filter-option-inner-inner');

    $('.dropdown-toggle.btn-white').addClass('border-0 ').attr('style', 'outline: none !important')
    if (selector.val().length !== 0) {
        selectorTitle.removeClass('text-muted');
    } else {
        selectorTitle.addClass('text-muted');
    }
})

selector.on('shown.bs.select', () => {
    selectorFormControl.addClass('border-primary-subtle').attr('style', 'box-shadow: 0 0 0 0.25rem rgba(0, 110, 255, 0.25)');
})


selector.on('changed.bs.select', () => {
    selectorTitle.removeClass('text-muted');
})

selector.on('hide.bs.select',() => {
    selectorFormControl.removeClass('border-primary-subtle').attr('style', '');
})