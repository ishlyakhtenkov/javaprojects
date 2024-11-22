package ru.javaprojects.projector.common.translate;

import ru.javaprojects.projector.common.error.LocalizedException;

public class TranslateException extends LocalizedException {
    public TranslateException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
