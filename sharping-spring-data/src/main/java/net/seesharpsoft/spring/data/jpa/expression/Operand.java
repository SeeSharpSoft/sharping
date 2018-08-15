package net.seesharpsoft.spring.data.jpa.expression;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

public interface Operand {

    enum Type {
        OBJECT,
        PATH,
        EXPRESSION,
        OPERATION,
        SPECIFICATION
    }

    Object evaluate();

    Expression asExpression(Root root,
                            CriteriaQuery criteriaQuery,
                            CriteriaBuilder criteriaBuilder,
                            Class targetType);

    Class getJavaType(Root root);
}
