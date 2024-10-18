setUpThemeSwitch(theme);

function setUpThemeSwitch(theme) {
    if (theme === 'dark') {
        $('#themeSwitcherIcon').removeClass('fa-regular fa-moon text-light')
            .addClass('fa-regular fa-sun text-warning').parent().attr('title', 'Switch to light theme');
    } else {
        $('#themeSwitcherIcon').removeClass('fa-regular fa-sun text-warning')
            .addClass('fa-regular fa-moon text-light').parent().attr('title', 'Switch to dark theme');
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
