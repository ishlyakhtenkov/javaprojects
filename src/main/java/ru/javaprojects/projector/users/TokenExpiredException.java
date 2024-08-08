package ru.javaprojects.projector.users;

import ru.javaprojects.projector.common.error.LocalizedException;

public class TokenExpiredException extends LocalizedException {
    public TokenExpiredException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
