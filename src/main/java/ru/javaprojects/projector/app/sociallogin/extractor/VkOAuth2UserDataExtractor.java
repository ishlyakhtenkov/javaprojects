package ru.javaprojects.projector.app.sociallogin.extractor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("vk")
public class VkOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    @Override
    public String getName(OAuth2UserData oAuth2UserData) {
        String name = null;
        List<Map<String, Object>> response = oAuth2UserData.getAttribute("response");
        if (response != null) {
            Map<String, Object> attributes = response.get(0);
            if (attributes != null) {
                String firstName =  (String) attributes.get("first_name");
                String lastName =  (String) attributes.get("last_name");
                if (firstName != null) {
                    name = firstName + (lastName != null ? " " + lastName : "");
                }
            }
        }
        return name;
    }

    @Override
    public String getAvatarUrl(OAuth2UserData oAuth2UserData) {
        return null;
    }
}
