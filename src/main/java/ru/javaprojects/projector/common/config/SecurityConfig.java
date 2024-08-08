package ru.javaprojects.projector.common.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.AuthUser;
import ru.javaprojects.projector.users.User;
import ru.javaprojects.projector.users.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserService userService;
    private final UserMdcFilter userMdcFilter;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userMdcFilter, AuthorizationFilter.class)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/register/**", "/profile/forgotPassword", "/profile/resetPassword").anonymous()
                                .requestMatchers("/", "/webjars/**", "/css/**", "/images/**", "/js/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
                )
                .logout((logout) ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                ).rememberMe((rememberMe) ->
                        rememberMe
                                .key("remember-me-key")
                                .rememberMeCookieName("projector-remember-me"));
        return http.build();
    }
}
