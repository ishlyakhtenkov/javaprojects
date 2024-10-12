package ru.javaprojects.projector.app.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.common.error.LocalizedException;
import ru.javaprojects.projector.common.util.AppUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
@AllArgsConstructor
@Slf4j
public class UIExceptionHandler {
    private final MessageSource messageSource;

    //https://stackoverflow.com/questions/45080119/spring-rest-exception-handling-fileuploadbasesizelimitexceededexception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public @ResponseBody ProblemDetail maxUploadSizeException(HttpServletRequest req, MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException at request {}: {}", req.getRequestURI(), e.toString());
        ErrorResponse.Builder builder = ErrorResponse.builder(e, HttpStatus.UNPROCESSABLE_ENTITY,
                AppUtil.getRootCause(e).getLocalizedMessage());
        return builder.build().getBody();
    }

    @ExceptionHandler(LocalizedException.class)
    public ModelAndView localizedExceptionHandler(HttpServletRequest req, LocalizedException e, Locale locale) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        String message = messageSource.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);
        return createExceptionModelAndView(e, message);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e, Locale locale) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        Throwable rootCause = AppUtil.getRootCause(e);
        String message = rootCause.getLocalizedMessage();
        if (e.getClass().isAssignableFrom(DataIntegrityViolationException.class)) {
            Optional<String> messageCode = DbConstraintMessageCodes.getMessageCode(message);
            if (messageCode.isPresent()) {
                message = messageSource.getMessage(messageCode.get(), null, locale);
            }
        }
        return createExceptionModelAndView(e, message);
    }

    private ModelAndView createExceptionModelAndView(Exception e, String message) {
        ModelAndView mav;
        if (e.getClass() == NoResourceFoundException.class) {
            mav = new ModelAndView("error/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
        } else {
            mav = new ModelAndView("error/exception",
                    Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "typeMessage", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            "message", message));
            mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        mav.addObject("authUser", AuthUser.safeGet());
        return mav;
    }
}
