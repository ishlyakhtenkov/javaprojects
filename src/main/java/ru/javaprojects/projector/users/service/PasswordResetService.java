package ru.javaprojects.projector.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.mail.MailSender;
import ru.javaprojects.projector.users.error.UserDisabledException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.model.token.PasswordResetToken;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.users.to.PasswordResetTo;

import java.util.Date;
import java.util.UUID;

import static ru.javaprojects.projector.app.config.SecurityConfig.PASSWORD_ENCODER;

@Service
public class PasswordResetService extends TokenService<PasswordResetToken> {

    public PasswordResetService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                                @Value("${password-reset.token-expiration-time}") long tokenExpirationTime,
                                @Value("${password-reset.confirm-url}") String confirmUrl,
                                PasswordResetTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "password-reset");
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        Assert.notNull(email, "email must not be null");
        PasswordResetToken passwordResetToken = ((PasswordResetTokenRepository) tokenRepository)
                .findByUserEmailIgnoreCase(email)
                .orElseGet(() -> new PasswordResetToken(null, UUID.randomUUID().toString(),
                        new Date(System.currentTimeMillis() + tokenExpirationTime),
                        userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new NotFoundException("Not found user with email=" + email, "notfound.user", new Object[]{email}))));
        checkUserDisabled(passwordResetToken);
        if (!passwordResetToken.isNew()) {
            passwordResetToken.setToken(UUID.randomUUID().toString());
            passwordResetToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
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
