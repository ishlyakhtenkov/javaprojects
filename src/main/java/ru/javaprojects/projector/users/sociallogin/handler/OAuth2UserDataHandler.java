package ru.javaprojects.projector.users.sociallogin.handler;

public interface OAuth2UserDataHandler {
    String getName(OAuth2UserData oAuth2UserData);

    String getEmail(OAuth2UserData oAuth2UserData);

    String getAvatarUrl(OAuth2UserData oAuth2UserData);
}
