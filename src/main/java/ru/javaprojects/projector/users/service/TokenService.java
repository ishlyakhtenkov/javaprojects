package ru.javaprojects.projector.users.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.mail.MailSender;
import ru.javaprojects.projector.users.error.TokenException;
import ru.javaprojects.projector.users.model.token.Token;
import ru.javaprojects.projector.users.repository.TokenRepository;
import ru.javaprojects.projector.users.repository.UserRepository;

import java.util.Date;
import java.util.Locale;

public abstract class TokenService<T extends Token> {
    public static final String CONFIRMATION_LINK_TEMPLATE = "<a href='%s?token=%s'>%s</a>";

    private final MailSender mailSender;
    private final MessageSource messageSource;
    protected final UserRepository userRepository;
    protected final TokenRepository<T> tokenRepository;
    protected long tokenExpirationTime;
    private final String confirmUrl;
    private final String messageCodePrefix;

    public TokenService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                        TokenRepository<T> tokenRepository, long tokenExpirationTime, String confirmUrl,
                        String messageCodePrefix) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenExpirationTime = tokenExpirationTime;
        this.confirmUrl = confirmUrl;
        this.messageCodePrefix = messageCodePrefix;
    }

    protected void sendEmail(String to, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String linkText = messageSource.getMessage(messageCodePrefix + ".message-link-text", null, locale);
        String messageSubject = messageSource.getMessage(messageCodePrefix + ".message-subject", null, locale);
        String messageText = messageSource.getMessage(messageCodePrefix + ".message-text", null, locale);
        String confirmationLink = String.format(CONFIRMATION_LINK_TEMPLATE, confirmUrl, token, linkText);
        mailSender.sendEmail(to, messageSubject, messageText + confirmationLink);
    }

    protected T getAndCheckToken(String token) {
        Assert.notNull(token, "token must not be null");
        T dbToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Not found " + messageCodePrefix + " token=" + token,
                messageCodePrefix + ".token-not-found", null));
        if (new Date().after(dbToken.getExpiryDate())) {
            throw new TokenException(messageCodePrefix + " token=" + dbToken.getToken() + " expired",
                    messageCodePrefix + ".token-expired", null);
        }
        return dbToken;
    }
}
