package ru.javaprojects.projector.common.mail;

import ru.javaprojects.projector.common.error.LocalizedException;

public class MailException extends LocalizedException {
    public MailException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
