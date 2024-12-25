package ru.javaprojects.javaprojects.app.sociallogin;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.HashMap;
import java.util.Map;

public class TokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
    @Override
    public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
        String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
        var accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        Map<String, Object> additionalParameters = new HashMap<>(tokenResponseParameters);
        return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(accessTokenType)
                .additionalParameters(additionalParameters)
                .build();
    }
}
