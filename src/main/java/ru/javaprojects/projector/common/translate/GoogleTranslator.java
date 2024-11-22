package ru.javaprojects.projector.common.translate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.javaprojects.projector.common.mail.MailException;

import java.util.ArrayList;
import java.util.Objects;

@Component
@Profile("!dev")
@Slf4j
public class GoogleTranslator implements Translator {
    private static final String GOOGLE_TRANSLATE_URL_TEMPLATE =
            "https://translate.google.so/translate_a/t?client=any_client_id_works&sl=auto&tl=%s&q=%s&tbb=1&ie=UTF-8&oe=UTF-8";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public String translate(String text, String locale) {
        String textInfo = text.length() < 20 ? text : text.substring(0, 19);
        log.info("translate {}... to {}", textInfo, locale);
        try {
            String googleTranslateUrl = String.format(GOOGLE_TRANSLATE_URL_TEMPLATE, locale, text);
            ResponseEntity<Object[]> response = restTemplate.getForEntity(googleTranslateUrl, Object[].class);
            return ((ArrayList<String>) Objects.requireNonNull(response.getBody())[0]).get(0);
        } catch (Exception e) {
            throw new TranslateException("Failed to translate (" + textInfo + "): " + e.getMessage(),
                    "error.failed-to-translate", new Object[]{textInfo});
        }
    }
}
