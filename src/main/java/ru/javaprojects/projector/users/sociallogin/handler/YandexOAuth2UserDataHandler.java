package ru.javaprojects.projector.users.sociallogin.handler;

import org.springframework.stereotype.Component;

@Component("yandex")
public class YandexOAuth2UserDataHandler implements OAuth2UserDataHandler {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String name = oAuth2UserData.getData("real_name");
        return name != null ? name : oAuth2UserData.getData("login");
    }

    @Override
    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("default_email");
    }
}
