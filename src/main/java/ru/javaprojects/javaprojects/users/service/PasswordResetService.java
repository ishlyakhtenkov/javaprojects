package ru.javaprojects.javaprojects.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.javaprojects.common.error.NotFoundException;
import ru.javaprojects.javaprojects.common.mail.MailSender;
import ru.javaprojects.javaprojects.users.error.UserDisabledException;
import ru.javaprojects.javaprojects.users.model.User;
import ru.javaprojects.javaprojects.users.model.token.PasswordResetToken;
import ru.javaprojects.javaprojects.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.javaprojects.users.repository.UserRepository;
import ru.javaprojects.javaprojects.users.to.PasswordResetTo;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MILLIS;
import static ru.javaprojects.javaprojects.app.config.SecurityConfig.PASSWORD_ENCODER;

@Service
public class PasswordResetService extends TokenService<PasswordResetToken> {

    public PasswordResetService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                                @Value("${password-reset.token-expiration-time}") long tokenExpirationTime,
                                @Value("${password-reset.confirm-url}") String confirmUrl,
                                PasswordResetTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "reset-password");
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        Assert.notNull(email, "email must not be null");
        PasswordResetToken passwordResetToken = ((PasswordResetTokenRepository) tokenRepository)
                .findByUserEmailIgnoreCase(email)
                .orElseGet(() -> new PasswordResetToken(null, UUID.randomUUID().toString(),
                        LocalDateTime.now().plus(tokenExpirationTime, MILLIS),
                        userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new NotFoundException("Not found user with email=" + email, "error.notfound.user", new Object[]{email}))));
        checkUserDisabled(passwordResetToken);
        if (!passwordResetToken.isNew()) {
            passwordResetToken.setToken(UUID.randomUUID().toString());
            passwordResetToken.setExpiryTimestamp(LocalDateTime.now().plus(tokenExpirationTime, MILLIS));
        }
        tokenRepository.saveAndFlush(passwordResetToken);
        sendEmail(email, passwordResetToken.getToken());
    }

    private void checkUserDisabled(PasswordResetToken passwordResetToken) {
        User user = passwordResetToken.getUser();
        if (!user.isEnabled()) {
            throw new UserDisabledException("User email=" + user.getEmail() + " is disabled", "user.disabled",
                    new Object[]{user.getEmail()});
        }
    }

    public PasswordResetToken checkToken(String token) {
        Assert.notNull(token, "token must not be null");
        PasswordResetToken passwordResetToken = getAndCheckToken(token);
        checkUserDisabled(passwordResetToken);
        return passwordResetToken;
    }

    @Transactional
    public void resetPassword(PasswordResetTo passwordResetTo) {
        Assert.notNull(passwordResetTo, "passwordResetTo must not be null");
        PasswordResetToken passwordResetToken = checkToken(passwordResetTo.getToken());
        passwordResetToken.getUser().setPassword(PASSWORD_ENCODER.encode(passwordResetTo.getPassword()));
        tokenRepository.delete(passwordResetToken);
    }
}