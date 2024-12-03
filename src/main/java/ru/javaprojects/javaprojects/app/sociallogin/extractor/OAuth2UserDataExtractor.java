package ru.javaprojects.javaprojects.app.sociallogin.extractor;

public abstract class OAuth2UserDataExtractor {
    public String getName(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getAttribute("name");
    }

    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getAttribute("email");
    }

    public abstract String getAvatarUrl(OAuth2UserData oAuth2UserData);
}
