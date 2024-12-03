package ru.javaprojects.javaprojects.common.mail;

import ru.javaprojects.javaprojects.common.error.LocalizedException;

public class MailException extends LocalizedException {
    public MailException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
