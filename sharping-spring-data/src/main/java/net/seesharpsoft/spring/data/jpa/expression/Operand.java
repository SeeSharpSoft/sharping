package net.seesharpsoft.spring.data.jpa.expression;

import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.*;
import java.util.List;

public interface Operand {

    enum Type {
        OBJECT,
        PATH,
        EXPRESSION,
        OPERATION,
        SPECIFICATION
    }

    Object evaluate();

    Expression asExpression(From root,
                            AbstractQuery query,
                            CriteriaBuilder criteriaBuilder,
                            Class targetType);

    Class getJavaType(From root, List<TupleElement> contexts);
}
