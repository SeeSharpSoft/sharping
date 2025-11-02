package net.seesharpsoft.spring.data.jpa;

import net.seesharpsoft.spring.data.jpa.expression.Operation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import jakarta.persistence.criteria.*;
import java.util.Objects;

public class OperationSpecification<T> implements Specification<T> {

    private Operation operation;

    public OperationSpecification(Operation operation) {
        Assert.notNull(operation, "operation must not be null!");
        this.operation = operation;
    }

    protected Operation getOperation() {
        return operation;
    }

    @Override
    public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
        Expression expression = getOperation().asExpression(root, query, cb, null);
        Assert.isInstanceOf(Predicate.class, expression);
        return (Predicate)expression;
    }

    @Override
    public String toString() {
        return operation.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OperationSpecification)) {
            return false;
        }
        OperationSpecification otherCondition = (OperationSpecification)other;
        return Objects.equals(operation, otherCondition.getOperation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation);
    }
}
