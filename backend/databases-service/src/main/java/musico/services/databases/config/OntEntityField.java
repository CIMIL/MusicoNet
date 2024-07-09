package musico.services.databases.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OntEntityField {
    enum DataType {
        DATA, OBJECT
    }

    DataType type() default DataType.DATA;

    String pred() default "";
}
