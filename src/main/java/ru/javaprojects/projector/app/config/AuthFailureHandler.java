package ru.javaprojects.projector.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class AuthFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String error = (exception instanceof DisabledException) ? "disabled-credentials" : "bad-credentials";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.sendRedirect("/login?error=" + error);
    }
}
