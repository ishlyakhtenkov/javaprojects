package ru.javaprojects.javaprojects.users.error;

import ru.javaprojects.javaprojects.common.error.LocalizedException;

public class UserDisabledException extends LocalizedException {
    public UserDisabledException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
