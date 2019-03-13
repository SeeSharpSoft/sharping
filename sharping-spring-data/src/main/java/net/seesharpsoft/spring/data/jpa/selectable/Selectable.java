package net.seesharpsoft.spring.data.jpa.selectable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Selectable {

    Class<?> from() default void.class;

    Joins joins() default @Joins({});

    String where() default "";
}
