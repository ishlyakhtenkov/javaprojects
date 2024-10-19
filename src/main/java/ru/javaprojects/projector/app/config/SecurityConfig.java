package ru.javaprojects.projector.app.config;

import lombok.AllArgsConstructor;
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
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.users.model.Role;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.app.sociallogin.AppOAuth2UserService;
import ru.javaprojects.projector.app.sociallogin.TokenResponseConverter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserService userService;
    private final SessionRegistry sessionRegistry;
    private final UserMdcLoggingFilter userMdcLoggingFilter;
    private final AppOAuth2UserService AppOAuth2UserService;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userMdcLoggingFilter, AuthorizationFilter.class)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/management/**").hasRole(Role.ADMIN.name())
                                .requestMatchers("/register/**", "/profile/forgot-password", "/profile/reset-password",
                                        "/login").anonymous()
                                .requestMatchers(HttpMethod.GET, "/", "/projects/*/view", "/webjars/**", "/css/**",
                                        "/images/**", "/js/**", "/content/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
                                .failureHandler(authenticationFailureHandler())
                )
                .oauth2Login((oauth2Login) ->
                        oauth2Login
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
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
                                .key("remember-me-key")
                                .rememberMeCookieName("projector-remember-me"))
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .maximumSessions(5)
                                .sessionRegistry(sessionRegistry));
        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new TokenResponseConverter());
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }
}
