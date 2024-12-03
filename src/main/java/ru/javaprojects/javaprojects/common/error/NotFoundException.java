package ru.javaprojects.javaprojects.common.error;

public class NotFoundException extends LocalizedException {
    public NotFoundException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}