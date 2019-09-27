package net.seesharpsoft.spring.data.jpa.selectable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Joins {
    Join[] value();
}
