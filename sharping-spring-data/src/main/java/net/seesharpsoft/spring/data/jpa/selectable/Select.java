package net.seesharpsoft.spring.data.jpa.selectable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Select {
    String value() default "";

    boolean ignore() default false;
}
