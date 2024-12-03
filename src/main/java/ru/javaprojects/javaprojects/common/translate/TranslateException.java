package ru.javaprojects.javaprojects.common.translate;

import ru.javaprojects.javaprojects.common.error.LocalizedException;

public class TranslateException extends LocalizedException {
    public TranslateException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
