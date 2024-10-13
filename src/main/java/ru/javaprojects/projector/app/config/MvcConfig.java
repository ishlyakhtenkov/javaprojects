package ru.javaprojects.projector.app.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.projects.ProjectService;

import java.util.Locale;

@Configuration
@AllArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    private final ProjectService projectService;

    // Add projects to view model to render on header dropdown selector
    private final HandlerInterceptor projectsInterceptor = new WebRequestHandlerInterceptorAdapter(new WebRequestInterceptor() {
        @Override
        public void postHandle(WebRequest request, ModelMap model) {
            if (model != null) {
                model.addAttribute("enabledProjects", projectService.getAllEnabled());
            }
        }

        @Override
        public void afterCompletion(WebRequest request, Exception ex) {
        }

        @Override
        public void preHandle(WebRequest request) {
        }
    });

    // Add authUser to view model
    private final HandlerInterceptor authInterceptor = new WebRequestHandlerInterceptorAdapter(new WebRequestInterceptor() {
        @Override
        public void postHandle(WebRequest request, ModelMap model) {
            if (model != null) {
                AuthUser authUser = AuthUser.safeGet();
                if (authUser != null) {
                    model.addAttribute("authUser", authUser);
                }
            }
        }

        @Override
        public void afterCompletion(WebRequest request, Exception ex) {
        }

        @Override
        public void preHandle(WebRequest request) {
        }
    });

    @Bean
    LocaleResolver localeResolver() {
        return new CookieLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor()).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
        registry.addInterceptor(authInterceptor).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
        registry.addInterceptor(projectsInterceptor).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/content/**").addResourceLocations("file:./content/");
        registry.setOrder(Integer.MAX_VALUE);
    }
}
