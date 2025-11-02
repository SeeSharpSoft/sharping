package net.seesharpsoft.spring.data.jpa.expression;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import java.util.List;

public interface Operation extends Operand {
    Operator getOperator();

    List getOperands();

    default Expression asExpression(From root,
                                    AbstractQuery query,
                                    CriteriaBuilder builder,
                                    Class targetType) {
        return getOperator().createExpression(root, query, builder, getOperands().toArray());
    }

    default Object evaluate() {
        return getOperator().evaluate(getOperands().toArray());
    }
}
