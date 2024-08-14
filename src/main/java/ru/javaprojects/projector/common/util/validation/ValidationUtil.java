package ru.javaprojects.projector.common.util.validation;

import lombok.experimental.UtilityClass;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.NonNull;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;

@UtilityClass
public class ValidationUtil {

    public static void checkNew(HasId bean) {
        if (!bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must be new (id=null)",
                    "has-id.must-be-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }

    public static void checkNotNew(HasId bean) {
        if (bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must be not new (id!=null)",
                    "has-id.must-be-not-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }

    //  Conservative when you reply, but accept liberally (http://stackoverflow.com/a/32728226/548473)
    public static void assureIdConsistent(HasId bean, long id) {
        if (bean.isNew()) {
            bean.setId(id);
        } else if (bean.id() != id) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must have id=" + id,
                    "has-id.must-have-id", new Object[]{bean.getClass().getSimpleName(), id});
        }
    }

    public static void assureIdConsistent(Iterable<? extends HasId> beans, long id) {
        beans.forEach(b -> assureIdConsistent(b, id));
    }

    //  https://stackoverflow.com/a/65442410/548473
    @NonNull
    public static Throwable getRootCause(@NonNull Throwable t) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }
}
