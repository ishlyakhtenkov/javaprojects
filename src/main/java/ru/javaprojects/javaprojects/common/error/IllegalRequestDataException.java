package ru.javaprojects.javaprojects.common.error;

public class IllegalRequestDataException extends LocalizedException {
    public IllegalRequestDataException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}