package ru.javaprojects.projector.app.sociallogin;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Service;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.users.model.Role;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.app.sociallogin.extractor.OAuth2UserData;
import ru.javaprojects.projector.app.sociallogin.extractor.OAuth2UserDataExtractor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository repository;
    private final Map<String, OAuth2UserDataExtractor> oAuth2UserDataExtractors;

    @Override
    public org.springframework.security.oauth2.core.user.OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        org.springframework.security.oauth2.core.user.OAuth2User oAuth2User = super.loadUser(userRequest);
        String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserDataExtractor oAuth2UserDataExtractor = oAuth2UserDataExtractors.computeIfAbsent(clientRegistrationId,
                clientRegId -> {
                    throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                            "Unknown provider: " + clientRegistrationId);
                });
        OAuth2UserData oAuth2UserData = new OAuth2UserData(oAuth2User, userRequest);
        String email = oAuth2UserDataExtractor.getEmail(oAuth2UserData);
        String name = oAuth2UserDataExtractor.getName(oAuth2UserData);
        if (email == null || name == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN),
                    clientRegistrationId + " account does not contain email or name");
        }
        AppOAuth2User user = new AppOAuth2User(oAuth2User, repository.findByEmailIgnoreCase(email.toLowerCase())
                .orElseGet(() -> {
                    String avatarUrl = oAuth2UserDataExtractor.getAvatarUrl(oAuth2UserData);
                    File avatar = null;
                    if (avatarUrl != null) {
                        avatar = new File(clientRegistrationId + "_oAuth2_avatar", avatarUrl);
                    }
                    return repository.save(new User(null, email, name, null, UUID.randomUUID().toString(), true,
                            Set.of(Role.USER), avatar));
                }));
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        return user;
    }
}
