package ru.javaprojects.javaprojects.app.config;

import org.springframework.security.core.session.AbstractSessionEvent;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import ru.javaprojects.javaprojects.app.AuthUser;

public class AppSessionRegistry extends SessionRegistryImpl {
    @Override
    public void onApplicationEvent(AbstractSessionEvent event) {
        super.onApplicationEvent(event);
        if (event instanceof HttpSessionCreatedEvent sessionCreatedEvent) {
            String sessionId = sessionCreatedEvent.getSession().getId();
            if (getSessionInformation(sessionId) == null && AuthUser.safeGet() != null) {
                registerNewSession(sessionId, AuthUser.safeGet());
            }
        }
    }
}
