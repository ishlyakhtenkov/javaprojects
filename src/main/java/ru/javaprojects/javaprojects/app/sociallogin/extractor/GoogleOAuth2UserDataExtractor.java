package ru.javaprojects.javaprojects.app.sociallogin.extractor;

import org.springframework.stereotype.Component;

@Component("google")
public class GoogleOAuth2UserDataExtractor extends OAuth2UserDataExtractor {

    @Override
    public String getAvatarUrl(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getAttribute("picture");
    }
}
