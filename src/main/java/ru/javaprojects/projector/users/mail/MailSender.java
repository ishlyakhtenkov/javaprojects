package ru.javaprojects.projector.users.mail;

public interface MailSender {
    void sendEmail(String to, String subject, String text);
}
