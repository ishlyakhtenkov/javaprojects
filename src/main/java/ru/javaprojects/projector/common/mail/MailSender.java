package ru.javaprojects.projector.common.mail;

public interface MailSender {
    void sendEmail(String to, String subject, String text);
}
