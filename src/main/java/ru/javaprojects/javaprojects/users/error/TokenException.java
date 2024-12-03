package ru.javaprojects.javaprojects.users.error;

import ru.javaprojects.javaprojects.common.error.LocalizedException;

public class TokenException extends LocalizedException {
    public TokenException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
