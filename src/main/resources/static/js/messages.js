const enMessages = new Map();
const ruMessages = new Map();

enMessages.set('passwords-not-match', 'passwords do not match');
ruMessages.set('passwords-not-match', 'пароли не совпадают');

function getMessage(messageCode) {
    if (locale === 'ru') {
        return ruMessages.get(messageCode);
    } else {
        return enMessages.get(messageCode);
    }
}