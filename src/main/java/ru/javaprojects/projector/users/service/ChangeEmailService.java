package ru.javaprojects.projector.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.users.error.TokenException;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.ChangeEmailToken;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.projector.users.repository.UserRepository;

import java.util.Date;
import java.util.UUID;

@Service
public class ChangeEmailService extends TokenService<ChangeEmailToken> {

    public ChangeEmailService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                              @Value("${change-email.token-expiration-time}") long tokenExpirationTime,
                              @Value("${change-email.confirm-url}") String confirmUrl,
                              ChangeEmailTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "change-email");
    }

    @Transactional
    public void changeEmail(long userId, String newEmail) {
        Assert.notNull(newEmail, "newEmail must not be null");
        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(user -> {
            String exMessage = user.id() == userId ? "user with id=" + userId + " already has email=" :
                    "email=" + newEmail + " already in use";
            String exMessageCode = user.id() == userId ? "change-email.already-has-email" : "change-email.already-in-use";
            throw new IllegalRequestDataException(exMessage, exMessageCode, new Object[]{newEmail});
        });
        ChangeEmailToken changeEmailToken = ((ChangeEmailTokenRepository) tokenRepository).findByUser_Id(userId)
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

    @Transactional
    public void confirmChangeEmail(String token, long userId) {
        ChangeEmailToken changeEmailToken = getAndCheckToken(token);
        if (changeEmailToken.getUser().id() != userId) {
            throw new TokenException("token " + token + " not belongs to user with id=" + userId,
                    "change-email.token-not-belongs", null);
        }
        changeEmailToken.getUser().setEmail(changeEmailToken.getNewEmail());
        tokenRepository.delete(changeEmailToken);
    }
}
