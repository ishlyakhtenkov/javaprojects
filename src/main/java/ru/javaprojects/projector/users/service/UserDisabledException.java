package ru.javaprojects.projector.users.service;

import ru.javaprojects.projector.common.error.LocalizedException;

public class UserDisabledException extends LocalizedException {
    public UserDisabledException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
