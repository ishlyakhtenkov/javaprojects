package ru.javaprojects.projector.common.validation;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;

@UtilityClass
public class ValidationUtil {

    public static void checkNew(HasId bean) {
        if (!bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must be new (id=null)",
                    "error.has-id.must-be-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }

    public static void checkNotNew(HasId bean) {
        if (bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must not be new (id!=null)",
                    "error.has-id.must-not-be-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }

    //  Conservative when you reply, but accept liberally (http://stackoverflow.com/a/32728226/548473)
    public static void assureIdConsistent(HasId bean, long id) {
        if (bean.isNew()) {
            bean.setId(id);
        } else if (bean.id() != id) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must have id=" + id,
                    "error.has-id.must-have-id", new Object[]{bean.getClass().getSimpleName(), id});
        }
    }
}
