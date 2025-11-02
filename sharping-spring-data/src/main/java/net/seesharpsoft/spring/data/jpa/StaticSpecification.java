package net.seesharpsoft.spring.data.jpa;

import net.seesharpsoft.UnhandledSwitchCaseException;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Objects;

public class StaticSpecification implements Specification {

    public static final Specification TRUE = new StaticSpecification(Type.TRUE);
    public static final Specification FALSE = new StaticSpecification(Type.FALSE);

    private enum Type {
        TRUE,
        FALSE
    }

    private final Type type;

    private StaticSpecification(Type type) {
        this.type = type;
    }

    @Override
    public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder criteriaBuilder) {
        switch (type) {
            case TRUE:
                return criteriaBuilder.and();
            case FALSE:
                return criteriaBuilder.or();
            default:
                throw new UnhandledSwitchCaseException(type);
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StaticSpecification)) {
            return false;
        }
        return Objects.equals(this.type, ((StaticSpecification)other).type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
