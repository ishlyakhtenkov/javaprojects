package ru.javaprojects.projector.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.mail.MailSender;
import ru.javaprojects.projector.users.error.TokenException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.model.token.ChangeEmailToken;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.users.to.UserTo;

import java.util.Date;
import java.util.UUID;

@Service
public class ChangeEmailService extends TokenService<ChangeEmailToken> {
    private UserService userService;

    public ChangeEmailService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                              @Value("${change-email.token-expiration-time}") long tokenExpirationTime,
                              @Value("${change-email.confirm-url}") String confirmUrl,
                              ChangeEmailTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "change-email");
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Transactional
    public void changeEmail(long userId, String newEmail) {
        Assert.notNull(newEmail, "newEmail must not be null");
        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(user -> {
            String exMessage = user.id() == userId ? ("user with id=" + userId + " already has email=" + newEmail) :
                    ("email=" + newEmail + " already in use");
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
        tokenRepository.saveAndFlush(changeEmailToken);
        sendEmail(newEmail, changeEmailToken.getToken());
    }

    @Transactional
    public void confirmChangeEmail(String token, long userId) {
        ChangeEmailToken changeEmailToken = getAndCheckToken(token);
        User user = changeEmailToken.getUser();
        if (user.id() != userId) {
            throw new TokenException("token " + token + " not belongs to user with id=" + userId,
                    "change-email.token-not-belongs", null);
        }
        userService.update(new UserTo(user.getId(), changeEmailToken.getNewEmail(), user.getName(), user.getRoles()));
        tokenRepository.delete(changeEmailToken);
    }
}
