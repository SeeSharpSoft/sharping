package net.seesharpsoft.spring.data.jpa.expression;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.List;

public interface Operation extends Operand {
    Operator getOperator();

    List getOperands();

    default Expression asExpression(Root root,
                             CriteriaQuery query,
                             CriteriaBuilder builder) {
        return getOperator().createExpression(root, query, builder, getOperands().toArray());
    }

    default Object evaluate() {
        return getOperator().evaluate(getOperands().toArray());
    }
}
