package ru.javaprojects.javaprojects.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.javaprojects.common.mail.MailSender;
import ru.javaprojects.javaprojects.users.model.Role;
import ru.javaprojects.javaprojects.users.model.User;
import ru.javaprojects.javaprojects.users.model.token.RegisterToken;
import ru.javaprojects.javaprojects.users.repository.RegisterTokenRepository;
import ru.javaprojects.javaprojects.users.repository.UserRepository;
import ru.javaprojects.javaprojects.users.to.RegisterTo;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MILLIS;
import static ru.javaprojects.javaprojects.users.util.UserUtil.prepareToSave;

@Service
public class RegisterService extends TokenService<RegisterToken> {
    public RegisterService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                           @Value("${register.token-expiration-time}") long tokenExpirationTime,
                           @Value("${register.confirm-url}") String confirmUrl,
                           RegisterTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "register");
    }

    @Transactional
    public void register(RegisterTo registerTo) {
        Assert.notNull(registerTo, "registerTo must not be null");
        prepareToSave(registerTo);
        RegisterToken registerToken = ((RegisterTokenRepository) tokenRepository)
                .findByEmailIgnoreCase(registerTo.getEmail()).orElseGet(RegisterToken::new);
        registerToken.setToken(UUID.randomUUID().toString());
        registerToken.setExpiryTimestamp(LocalDateTime.now().plus(tokenExpirationTime, MILLIS));
        registerToken.setEmail(registerTo.getEmail());
        registerToken.setName(registerTo.getName());
        registerToken.setPassword(registerTo.getPassword());
        tokenRepository.saveAndFlush(registerToken);
        sendEmail(registerTo.getEmail(), registerToken.getToken());
    }

    @Transactional
    public void confirmRegister(String token) {
        Assert.notNull(token, "token must not be null");
        RegisterToken registerToken = getAndCheckToken(token);
        userRepository.save(new User(null, registerToken.getEmail(), registerToken.getName(), registerToken.getPassword(),
                true, Set.of(Role.USER)));
        tokenRepository.delete(registerToken);
    }
}
