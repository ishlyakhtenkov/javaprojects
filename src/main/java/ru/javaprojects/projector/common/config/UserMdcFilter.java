package ru.javaprojects.projector.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.javaprojects.projector.users.AuthUser;

import java.io.IOException;

@Component
public class UserMdcFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        AuthUser authUser = AuthUser.safeGet();
        String user = authUser != null ? authUser.getUser().getEmail() : "anonymous";
        MDC.put("user", user);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("user");
        }
    }
}
