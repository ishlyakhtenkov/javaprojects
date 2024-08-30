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
        dbConstraintsMap.put("technologies_unique_name_idx", "duplicate.technology-name");
        dbConstraintsMap.put("architectures_unique_name_idx", "duplicate.architecture-name");
        dbConstraintsMap.put("projects_unique_name_idx", "duplicate.project-name");
        dbConstraintsMap.put("foreign key(architecture_id) references", "architecture.is-referenced");
        dbConstraintsMap.put("foreign key(technology_id) references", "technology.is-referenced");
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
