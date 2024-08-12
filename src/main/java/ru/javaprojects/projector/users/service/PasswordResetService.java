package ru.javaprojects.projector.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.PasswordResetTo;
import ru.javaprojects.projector.users.TokenExpiredException;
import ru.javaprojects.projector.users.User;
import ru.javaprojects.projector.users.UserService;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.PasswordResetToken;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    public static final String PASSWORD_RESET_MESSAGE_LINK_TEMPLATE = "<a href='%s?token=%s'>%s</a>";

    private final PasswordResetTokenRepository repository;
    private final UserService userService;
    private final MailSender mailSender;
    private final MessageSource messageSource;

    @Value("${password-reset.token-expiration-time}")
    private long tokenExpirationTime;

    @Value("${password-reset.url}")
    private String passwordResetUrl;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        Assert.notNull(email, "email must not be null");
        PasswordResetToken passwordResetToken = repository.findByUserEmailIgnoreCase(email)
                .orElseGet(() -> new PasswordResetToken(null, UUID.randomUUID().toString(),
                        new Date(System.currentTimeMillis() + tokenExpirationTime), userService.getByEmail(email)));
        checkUserDisabled(passwordResetToken);
        if (!passwordResetToken.isNew()) {
            passwordResetToken.setToken(UUID.randomUUID().toString());
            passwordResetToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
        }
        repository.save(passwordResetToken);
        sendEmail(email, passwordResetToken.getToken());
    }

    private void checkUserDisabled(PasswordResetToken passwordResetToken) {
        User user = passwordResetToken.getUser();
        if (!user.isEnabled()) {
            throw new UserDisabledException("User email=" + user.getEmail() + " is disabled", "user.disabled",
                    new Object[]{user.getEmail()});
        }
    }

    private void sendEmail(String to, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String passwordResetUrlLinkText = messageSource.getMessage("password-reset.url-link-text", null, locale);
        String passwordResetMessageSubject = messageSource.getMessage("password-reset.message-subject", null, locale);
        String passwordResetMessageText = messageSource.getMessage("password-reset.message-text", null, locale);
        String link = String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, token, passwordResetUrlLinkText);
        String text = passwordResetMessageText + link;
        mailSender.sendEmail(to, passwordResetMessageSubject, text);
    }

    public PasswordResetToken checkToken(String token) {
        Assert.notNull(token, "token must not be null");
        PasswordResetToken passwordResetToken = repository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Not found password reset token=" + token,
                        "password-reset.token-not-found", null));
        checkUserDisabled(passwordResetToken);
        Date expiryDate = passwordResetToken.getExpiryDate();
        if (new Date().after(expiryDate)) {
            throw new TokenExpiredException("Password reset token=" + passwordResetToken.getToken() + " expired",
                    "password-reset.token-expired", null);
        }
        return passwordResetToken;
    }

    @Transactional
    public void resetPassword(PasswordResetTo passwordResetTo) {
        Assert.notNull(passwordResetTo, "passwordResetTo must not be null");
        PasswordResetToken passwordResetToken = checkToken(passwordResetTo.getToken());
        passwordResetToken.getUser().setPassword(PASSWORD_ENCODER.encode(passwordResetTo.getPassword()));
        repository.delete(passwordResetToken);
    }
}
