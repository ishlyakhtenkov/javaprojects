package ru.javaprojects.projector.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import ru.javaprojects.projector.common.web.JacksonObjectMapper;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonObjectMapper.getMapper();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
