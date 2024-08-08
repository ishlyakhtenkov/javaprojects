package ru.javaprojects.projector.common.error;

import lombok.Getter;

@Getter
public class LocalizedException extends RuntimeException {
    private final String messageCode;
    private final Object[] messageArgs;

    public LocalizedException(String message, String messageCode, Object[] messageArgs) {
        super(message);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }
}
