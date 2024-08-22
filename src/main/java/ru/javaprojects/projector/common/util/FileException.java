package ru.javaprojects.projector.common.util;

import ru.javaprojects.projector.common.error.LocalizedException;

public class FileException extends LocalizedException {
    public FileException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
