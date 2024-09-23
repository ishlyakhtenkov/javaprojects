package ru.javaprojects.projector.users.sociallogin.extractor;

import org.springframework.stereotype.Component;

@Component("gitlab")
public class GitLabOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String name = super.getName(oAuth2UserData);
        return name != null ? name : oAuth2UserData.getAttribute("username");
    }

    @Override
    public String getAvatarUrl(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getAttribute("avatar_url");
    }
}
