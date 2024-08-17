let prevUrl = document.referrer ? document.referrer :
    window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
if (sessionStorage.getItem('prevUrl') == null) {
    sessionStorage.setItem('prevUrl', prevUrl);
}

function cancel() {
    let prevUrl = sessionStorage.getItem('prevUrl');
    if (prevUrl) {
        sessionStorage.removeItem('prevUrl');
    } else {
        prevUrl = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    }
    window.location.href = prevUrl;
}
