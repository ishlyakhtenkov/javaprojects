package ru.javaprojects.projector.app.sociallogin.extractor;

import org.springframework.stereotype.Component;

@Component("yandex")
public class YandexOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String name = oAuth2UserData.getAttribute("real_name");
        return name != null ? name : oAuth2UserData.getAttribute("login");
    }

    @Override
    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getAttribute("default_email");
    }

    @Override
    public String getAvatarUrl(OAuth2UserData oAuth2UserData) {
        String avatarId = oAuth2UserData.getAttribute("default_avatar_id");
        return avatarId != null ? String.format("https://avatars.yandex.net/get-yapic/%s/islands-200", avatarId) : null;
    }
}
