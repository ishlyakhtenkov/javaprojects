package ru.javaprojects.projector.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.RegisterToken;
import ru.javaprojects.projector.users.model.Role;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.RegisterTokenRepository;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.users.to.UserTo;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static ru.javaprojects.projector.users.UserUtil.prepareToSave;

@Service
public class RegisterService extends TokenService<RegisterToken> {
    public RegisterService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                           @Value("${register.token-expiration-time}") long tokenExpirationTime,
                           @Value("${register.confirm-url}") String confirmUrl,
                           RegisterTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "register");
    }

    @Transactional
    public void register(UserTo userTo) {
        Assert.notNull(userTo, "userTo must not be null");
        prepareToSave(userTo);
        RegisterToken registerToken = ((RegisterTokenRepository) tokenRepository)
                .findByEmailIgnoreCase(userTo.getEmail()).orElseGet(RegisterToken::new);
        registerToken.setToken(UUID.randomUUID().toString());
        registerToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
        registerToken.setEmail(userTo.getEmail());
        registerToken.setName(userTo.getName());
        registerToken.setPassword(userTo.getPassword());
        tokenRepository.save(registerToken);
        sendEmail(userTo.getEmail(), registerToken.getToken());
    }

    @Transactional
    public void confirmRegister(String token) {
        RegisterToken registerToken = getAndCheckToken(token);
        userRepository.save(new User(null, registerToken.getEmail(), registerToken.getName(), registerToken.getPassword(),
                true, Set.of(Role.USER)));
        tokenRepository.delete(registerToken);
    }
}
