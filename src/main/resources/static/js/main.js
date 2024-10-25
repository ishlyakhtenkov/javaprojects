setUpThemeSwitch(theme);

function setUpThemeSwitch(theme) {
    if (theme === 'dark') {
        $('#themeSwitcherIcon').removeClass('bi bi-moon-stars-fill text-secondary')
            .addClass('bi bi-sun-fill text-warning').parent().attr('title', getMessage('info.switch-to-light-theme'));
    } else {
        $('#themeSwitcherIcon').removeClass('bi bi-sun-fill text-warning')
            .addClass('bi bi-moon-stars-fill text-secondary').parent().attr('title', getMessage('info.switch-to-dark-theme'));
    }
}

function setTheme(themeSwitch) {
    let theme = themeSwitch.find('i').attr('class').includes('bi-sun-fill') ? 'light' : 'dark';
    localStorage.setItem('bs-theme', theme);
    $('html').attr('data-bs-theme', theme);
    setUpThemeSwitch(theme);
}

function changeLocale(locale) {
    window.location.replace('?lang=' + locale);
}
