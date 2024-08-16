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
import ru.javaprojects.projector.users.to.RegisterTo;

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
    public void register(RegisterTo registerTo) {
        Assert.notNull(registerTo, "registerTo must not be null");
        prepareToSave(registerTo);
        RegisterToken registerToken = ((RegisterTokenRepository) tokenRepository)
                .findByEmailIgnoreCase(registerTo.getEmail()).orElseGet(RegisterToken::new);
        registerToken.setToken(UUID.randomUUID().toString());
        registerToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
        registerToken.setEmail(registerTo.getEmail());
        registerToken.setName(registerTo.getName());
        registerToken.setPassword(registerTo.getPassword());
        tokenRepository.save(registerToken);
        sendEmail(registerTo.getEmail(), registerToken.getToken());
    }

    @Transactional
    public void confirmRegister(String token) {
        RegisterToken registerToken = getAndCheckToken(token);
        userRepository.save(new User(null, registerToken.getEmail(), registerToken.getName(), registerToken.getPassword(),
                true, Set.of(Role.USER)));
        tokenRepository.delete(registerToken);
    }
}
