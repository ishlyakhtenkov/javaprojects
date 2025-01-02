package ru.javaprojects.javaprojects.app.sociallogin;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Service;
import ru.javaprojects.javaprojects.app.sociallogin.extractor.OAuth2UserData;
import ru.javaprojects.javaprojects.app.sociallogin.extractor.OAuth2UserDataExtractor;
import ru.javaprojects.javaprojects.common.model.File;
import ru.javaprojects.javaprojects.users.model.Role;
import ru.javaprojects.javaprojects.users.model.User;
import ru.javaprojects.javaprojects.users.repository.UserRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ru.javaprojects.javaprojects.app.config.SecurityConfig.PASSWORD_ENCODER;

@Service
@AllArgsConstructor
public class AppOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository repository;
    private final Map<String, OAuth2UserDataExtractor> oAuth2UserDataExtractors;

    @Override
    public org.springframework.security.oauth2.core.user.OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var oAuth2User = super.loadUser(userRequest);
        String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();
        var oAuth2UserDataExtractor = oAuth2UserDataExtractors.computeIfAbsent(clientRegistrationId,
                _ -> {
                    throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                            "Unknown provider: " + clientRegistrationId);
                });
        var oAuth2UserData = new OAuth2UserData(oAuth2User, userRequest);
        String email = oAuth2UserDataExtractor.getEmail(oAuth2UserData);
        String name = oAuth2UserDataExtractor.getName(oAuth2UserData);
        if (email == null || name == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN),
                    clientRegistrationId + " account does not contain email or name");
        }
        var user = new AppOAuth2User(oAuth2User, repository.findByEmailIgnoreCase(email.toLowerCase())
                .orElseGet(() -> {
                    String avatarUrl = oAuth2UserDataExtractor.getAvatarUrl(oAuth2UserData);
                    File avatar = null;
                    if (avatarUrl != null) {
                        avatar = new File(clientRegistrationId + "_oAuth2_avatar", avatarUrl);
                    }
                    return repository.save(new User(null, email, name, null,
                            PASSWORD_ENCODER.encode(UUID.randomUUID().toString()), true, Set.of(Role.USER), avatar));
                }));
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        return user;
    }
}
