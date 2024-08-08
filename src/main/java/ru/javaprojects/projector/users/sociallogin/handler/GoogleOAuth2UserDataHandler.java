package ru.javaprojects.projector.users.sociallogin.handler;

import org.springframework.stereotype.Component;

@Component("google")
public class GoogleOAuth2UserDataHandler implements OAuth2UserDataHandler {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("name");
    }

    @Override
    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("email");
    }
}
