package ru.javaprojects.projector.users.sociallogin.handler;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("vk")
public class VkOAuth2UserDataHandler implements OAuth2UserDataHandler {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String firstName = getAttribute(oAuth2UserData, "first_name");
        String lastName = getAttribute(oAuth2UserData, "last_name");
        return firstName + (lastName != null ? " " + lastName : "");
    }

    @Override
    public String getEmail(OAuth2UserData oAuth2UserData) {
        return oAuth2UserData.getData("email");
    }

    private String getAttribute(OAuth2UserData oAuth2UserData, String name) {
        List<Map<String, Object>> attributesResponse = oAuth2UserData.getData("response");
        if (attributesResponse != null) {
            Map<String, Object> attributes = attributesResponse.get(0);
            if (attributes != null) {
                return (String) attributes.get(name);
            }
        }
        return null;
    }
}
