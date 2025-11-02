package net.seesharpsoft.spring.data.jpa.selectable;

import jakarta.persistence.criteria.JoinType;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Joins.class)
public @interface Join {

    String value();

    JoinType type() default JoinType.LEFT;

    String alias() default "";

    String on() default "";
}
