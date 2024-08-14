package ru.javaprojects.projector.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.TokenException;
import ru.javaprojects.projector.users.UserRepository;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.ChangeEmailToken;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeEmailService {
    public static final String CONFIRM_CHANGE_EMAIL_MESSAGE_LINK_TEMPLATE = "<a href='%s?token=%s'>%s</a>";

    private final ChangeEmailTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailSender mailSender;
    private final MessageSource messageSource;

    @Value("${change-email.token-expiration-time}")
    private long tokenExpirationTime;

    @Value("${change-email.confirm-url}")
    private String confirmChangeEmailUrl;

    @Transactional
    public void changeEmail(long userId, String newEmail) {
        Assert.notNull(newEmail, "newEmail must not be null");
        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(user -> {
            String exMessage = user.id() == userId ? "user with id=" + userId + " already has email=" :
                    "email=" + newEmail + " already in use";
            String exMessageCode = user.id() == userId ? "change-email.already-has-email" : "change-email.already-in-use";
            throw new IllegalRequestDataException(exMessage, exMessageCode, new Object[]{newEmail});
        });
        ChangeEmailToken changeEmailToken = tokenRepository.findByUser_Id(userId)
                .orElseGet(() -> new ChangeEmailToken(null, UUID.randomUUID().toString(),
                        new Date(System.currentTimeMillis() + tokenExpirationTime), newEmail, userRepository.getExisted(userId)));
        if (!changeEmailToken.isNew()) {
            changeEmailToken.setToken(UUID.randomUUID().toString());
            changeEmailToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
            changeEmailToken.setNewEmail(newEmail);
        }
        tokenRepository.save(changeEmailToken);
        sendEmail(newEmail, changeEmailToken.getToken());
    }

    private void sendEmail(String to, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.url-link-text", null, locale);
        String changeEmail = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRM_CHANGE_EMAIL_MESSAGE_LINK_TEMPLATE, confirmChangeEmailUrl, token, changeEmailUrlLinkText);
        String text = changeEmailMessageText + link;
        mailSender.sendEmail(to, changeEmail, text);
    }

    @Transactional
    public void confirmChangeEmail(String token, long userId) {
        Assert.notNull(token, "token must not be null");
        ChangeEmailToken changeEmailToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Not found change email token=" + token,
                        "change-email.token-not-found", null));
        if (changeEmailToken.getUser().id() != userId) {
            throw new TokenException("token " + token + " not belongs to user with id=" + userId,
                    "change-email.token-not-belongs", null);
        }
        if (new Date().after(changeEmailToken.getExpiryDate())) {
            throw new TokenException("Change email token=" + changeEmailToken.getToken() + " expired",
                    "change-email.token-expired", null);
        }
        changeEmailToken.getUser().setEmail(changeEmailToken.getNewEmail());
        tokenRepository.delete(changeEmailToken);
    }
}
