package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.UnhandledSwitchCaseException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Objects;

public class Operand {
    public enum Type {
        OBJECT,
        PATH,
        EXPRESSION,
        OPERATION,
        SPECIFICATION
    }

    /**
     * Creates or reuses a join from given path to given field.
     *
     * @param from  current path
     * @param field target field
     * @return a join to given field
     */
    protected static Join getJoin(From<?, ?> from, String field) {
        for (Join join : from.getJoins()) {
            if (join.getAttribute().getName().equals(field) && join.getJoinType().equals(JoinType.LEFT)) {
                return join;
            }
        }
        return from.join(field, JoinType.LEFT);
    }

    /**
     * Returns a path from given path.
     *
     * @param from       the starting path
     * @param stringPath the string representation of the path
     * @return a path
     */
    protected static Path getPath(From from, String stringPath) {
        if (stringPath == null || stringPath.isEmpty()) {
            return from;
        }
        From current = from;
        String[] paths = stringPath.split("/");
        int lastIndex = paths.length - 1;
        for (int index = 0; index < lastIndex; ++index) {
            current = getJoin(current, paths[index]);
        }
        return current.get(paths[lastIndex]);
    }

    private final Object value;
    private final Type type;
    private final Class javaType;

    public Operand(Object value, Type type, Class<?> javaType) {
        this.value = value;
        this.type = type;
        this.javaType = javaType;
    }

    public Operand(Object value, Type type) {
        this(value, type, value == null ? void.class : value.getClass());
    }

    public Operand(Object value) {
        this(value,
                value instanceof String ? Type.PATH :
                        (value instanceof Expression ? Type.EXPRESSION :
                                (value instanceof Operation ? Type.OPERATION :
                                        (value instanceof Specification ? Type.SPECIFICATION : Type.OBJECT))));
    }

    public Type getType() {
        return type;
    }

    public <T> T getValue() {
        return value == null ? null : (T)value;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public String getValueAsString() {
        return value == null ? "" : value.toString();
    }

    public Expression asExpression(Root root,
                                   CriteriaQuery criteriaQuery,
                                   CriteriaBuilder criteriaBuilder) {
        switch (getType()) {
            case PATH:
                return getPath(root, getValueAsString());
            case OBJECT:
                return getValue() == null ?
                        criteriaBuilder.nullLiteral(getJavaType()) :
                        criteriaBuilder.literal(getValue());
            case EXPRESSION:
                return (Expression) getValue();
            case OPERATION:
                Operation operation = getValue();
                if (operation == null) {
                    return criteriaBuilder.nullLiteral(getJavaType());
                }
                List<Operand> operands = operation.getOperands();
                return operation.getOperator().getExpression(root, criteriaQuery, criteriaBuilder, operands.toArray(new Operand[operands.size()]));
            case SPECIFICATION:
                Specification specification = getValue();
                if (specification == null) {
                    return criteriaBuilder.nullLiteral(getJavaType());
                }
                return specification.toPredicate(root, criteriaQuery, criteriaBuilder);
            default:
                throw new UnhandledSwitchCaseException(getType());
        }
    }

    @Override
    public String toString() {
        return this.value == null ? "null" :
                (type == Type.OBJECT && String.class.equals(javaType) ? "'" + value.toString() + "'" : value.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Operand)) {
            return false;
        }
        Operand otherOperand = (Operand) other;
        return Objects.equals(getType(), otherOperand.getType()) && Objects.equals(getValue(), otherOperand.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }
}