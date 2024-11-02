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
    let urlParams = new URLSearchParams(window.location.search);
    urlParams.set('lang', locale);
    window.location.replace('?' + urlParams.toString());
}

function copyAppLink() {
    navigator.clipboard.writeText(window.location.origin);
    successToast(getMessage('info.link-copied'))
}

function shareAppOnVk() {
    window.open(`https://vk.com/share.php?url=${window.location.origin}&title=${getMessage('info.app-description')}`);
}

function shareAppOnTelegram() {
    window.open(`https://t.me/share/url?url=${window.location.origin}&text=${getMessage('info.app-description')}`);
}

function shareAppOnWhatsApp() {
    window.open(`https://api.whatsapp.com/send?text=${getMessage('info.app-description')} ${window.location.origin}`);
}
