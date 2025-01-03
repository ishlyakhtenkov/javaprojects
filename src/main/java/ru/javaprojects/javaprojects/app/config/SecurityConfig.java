package ru.javaprojects.javaprojects.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.client.RestTemplate;
import ru.javaprojects.javaprojects.app.AuthUser;
import ru.javaprojects.javaprojects.app.sociallogin.AppOAuth2UserService;
import ru.javaprojects.javaprojects.app.sociallogin.OAuth2AuthenticationSuccessHandler;
import ru.javaprojects.javaprojects.app.sociallogin.TokenResponseConverter;
import ru.javaprojects.javaprojects.common.error.NotFoundException;
import ru.javaprojects.javaprojects.users.model.Role;
import ru.javaprojects.javaprojects.users.model.User;
import ru.javaprojects.javaprojects.users.service.UserService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final String LOGIN_SUCCESS_URL = "/";

    private final UserService userService;
    private final SessionRegistry sessionRegistry;
    private final UserMdcLoggingFilter userMdcLoggingFilter;
    private final AppOAuth2UserService AppOAuth2UserService;

    @Value("${remember-me.key}")
    private String rememberMeKey;

    @Value("${remember-me.cookie-name}")
    private String rememberMeCookieName;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            try {
                User user = userService.getByEmail(email);
                return new AuthUser(user);
            } catch (NotFoundException ex) {
                throw new UsernameNotFoundException(ex.getMessage());
            }
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthFailureHandler();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(LOGIN_SUCCESS_URL, rememberMeKey, rememberMeCookieName);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userMdcLoggingFilter, AuthorizationFilter.class)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/management/**").hasRole(Role.ADMIN.name())
                                .requestMatchers("/register/**", "/profile/forgot-password", "/profile/reset-password",
                                        "/login").anonymous()
                                .requestMatchers(HttpMethod.GET, "/", "/search/**", "/tags/*", "/about", "/contact",
                                        "/profile/*/view", "/profile/by-keyword",
                                        "/projects/*/view", "/projects/fresh", "/projects/popular", "/projects/by-author",
                                        "/projects/by-tag", "/projects/by-keyword",
                                        "/webjars/**", "/css/**", "/images/**", "/js/**", "/content/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .defaultSuccessUrl(LOGIN_SUCCESS_URL, true)
                                .failureHandler(authenticationFailureHandler())
                )
                .oauth2Login((oauth2Login) ->
                        oauth2Login
                                .loginPage("/login")
                                .successHandler(oAuth2AuthSuccessHandler())
                                .tokenEndpoint((tokenEndpoint) ->
                                        tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient()))
                                .userInfoEndpoint((userInfoEndpoint) ->
                                        userInfoEndpoint.userService(AppOAuth2UserService))
                                .failureHandler(authenticationFailureHandler())
                )
                .logout((logout) ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                )
                .rememberMe((rememberMe) ->
                        rememberMe
                                .key(rememberMeKey)
                                .rememberMeCookieName(rememberMeCookieName))
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .maximumSessions(5)
                                .sessionRegistry(sessionRegistry));
        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        var accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        var tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new TokenResponseConverter());
        var restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(),
                tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }
}
