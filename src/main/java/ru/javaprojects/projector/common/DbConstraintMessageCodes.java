package ru.javaprojects.projector.common;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class DbConstraintMessageCodes {
    private static final Map<String, String> dbConstraintsMap = new HashMap<>();

    static {
        dbConstraintsMap.put("users_unique_email_idx", "duplicate.email");
    }

    public static Optional<String> getMessageCode(String exceptionMessage) {
        exceptionMessage = exceptionMessage.toLowerCase();
        for (Map.Entry<String, String> entry : dbConstraintsMap.entrySet()) {
            if (exceptionMessage.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
