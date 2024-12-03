package ru.javaprojects.javaprojects.common.translate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
public class MockTranslator implements Translator {
    @Override
    public String translate(String text, String locale) {
        log.info("translate {}... to {}", text.length() < 20 ? text : text.substring(0, 19), locale);
        return "to " + locale + ":" + text;
    }
}
