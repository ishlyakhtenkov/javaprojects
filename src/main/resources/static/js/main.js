setUpThemeSwitch(theme);

function setUpThemeSwitch(theme) {
    if (theme === 'dark') {
        $('#themeSwitcherIcon').removeClass('fa-solid fa-star text-secondary')
            .addClass('fa-regular fa-sun text-warning').parent().attr('title', getMessage('info.switch-to-light-theme'));
    } else {
        $('#themeSwitcherIcon').removeClass('fa-regular fa-sun text-warning')
            .addClass('fa-solid fa-star text-secondary').parent().attr('title', getMessage('info.switch-to-dark-theme'));
    }
}

function setTheme(themeSwitch) {
    let theme = themeSwitch.find('i').attr('class').includes('fa-sun') ? 'light' : 'dark';
    localStorage.setItem('bs-theme', theme);
    $('html').attr('data-bs-theme', theme);
    setUpThemeSwitch(theme);
}

function changeLocale(locale) {
    window.location.replace('?lang=' + locale);
}
