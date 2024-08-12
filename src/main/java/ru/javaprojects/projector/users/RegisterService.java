package ru.javaprojects.projector.users;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.mail.MailSender;

import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static ru.javaprojects.projector.users.UserUtil.prepareToSave;

@Service
@RequiredArgsConstructor
public class RegisterService {
    public static final String CONFIRM_REGISTER_MESSAGE_LINK_TEMPLATE = "<a href='%s?token=%s'>%s</a>";

    private final RegisterTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailSender mailSender;
    private final MessageSource messageSource;

    @Value("${register.token-expiration-time}")
    private long tokenExpirationTime;

    @Value("${register.confirm-url}")
    private String confirmRegisterUrl;

    @Transactional
    public void register(UserTo userTo) {
        Assert.notNull(userTo, "userTo must not be null");
        prepareToSave(userTo);
        RegisterToken registerToken = tokenRepository.findByEmailIgnoreCase(userTo.getEmail()).orElseGet(RegisterToken::new);
        registerToken.setToken(UUID.randomUUID().toString());
        registerToken.setExpiryDate(new Date(System.currentTimeMillis() + tokenExpirationTime));
        registerToken.setEmail(userTo.getEmail());
        registerToken.setName(userTo.getName());
        registerToken.setPassword(userTo.getPassword());
        tokenRepository.save(registerToken);
        sendEmail(userTo.getEmail(), registerToken.getToken());
    }

    private void sendEmail(String to, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String confirmRegisterUrlLinkText = messageSource.getMessage("register.confirm-url-link-text", null, locale);
        String confirmRegisterMessageSubject = messageSource.getMessage("register.confirm-message-subject", null, locale);
        String confirmRegisterMessageText = messageSource.getMessage("register.confirm-message-text", null, locale);
        String link = String.format(CONFIRM_REGISTER_MESSAGE_LINK_TEMPLATE, confirmRegisterUrl, token, confirmRegisterUrlLinkText);
        String text = confirmRegisterMessageText + link;
        mailSender.sendEmail(to, confirmRegisterMessageSubject, text);
    }

    @Transactional
    public void confirmRegister(String token) {
        Assert.notNull(token, "token must not be null");
        RegisterToken registerToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Not found register token=" + token, "register.token-not-found", null));
        Date expiryDate = registerToken.getExpiryDate();
        if (new Date().after(expiryDate)) {
            throw new TokenExpiredException("Register token=" + registerToken.getToken() + " expired", "register.token-expired", null);
        }
        userRepository.save(new User(null, registerToken.getEmail(), registerToken.getName(), registerToken.getPassword(),
                true, Set.of(Role.USER)));
        tokenRepository.delete(registerToken);
    }
}
