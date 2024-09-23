package ru.javaprojects.projector.users.sociallogin.handler;

import org.springframework.stereotype.Component;

@Component("gitlab")
public class GitLabOAuth2UserDataHandler implements OAuth2UserDataHandler{
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String name = oAuth2UserData.getData("name");
        return name != null ? name : oAuth2UserData.getData("username");
    }

    @Override
    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("email");
    }

    @Override
    public String getAvatarUrl(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("avatar_url");
    }
}
