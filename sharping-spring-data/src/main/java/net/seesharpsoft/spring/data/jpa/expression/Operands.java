package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.UnhandledSwitchCaseException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.Objects;

public class Operands {

    private Operands() {
        // static
    }

    public static final int TYPE_FIELD_NAME = 1;
    public static final int TYPE_PRIMITIVE = 2;
    public static final int TYPE_EXPRESSION = 3;

    /**
     * Creates an operand from given value. Returns null if value is null.
     *
     * @param value the value to get an operand for
     * @return an Operand implementation or null if value is null
     */
    public static Operand from(Object value) {
        if (value == null || value instanceof Operand) {
            return (Operand) value;
        }
        return new Wrapper(value);
    }

    public static Operand asReference(String path) {
        return new FieldReference(path);
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
        String[] paths = stringPath.split("[/.\\\\]");
        int lastIndex = paths.length - 1;
        for (int index = 0; index < lastIndex; ++index) {
            current = getJoin(current, paths[index]);
        }
        return current.get(paths[lastIndex]);
    }

    public static class FieldReference extends Wrapper {

        public FieldReference(String fieldReference) {
            super(fieldReference
                    .replaceAll("^[/.\\\\]*", "")
                    .replaceAll("[/.\\\\]*$", "")
                    .replaceAll("[/.\\\\][/.\\\\]", "/"));
            Assert.hasText(fieldReference, "fieldReference must not be empty!");
        }

        public Object evaluate(Object obj) {
            Assert.notNull(obj, "input parameter must not be null!");
            Object current = obj;
            String[] paths = this.<String>getValue().split("[/.\\\\]");
            Class<?> clazz = obj.getClass();
            for (String path : paths) {
                Field field = ReflectionUtils.findField(clazz, path);
                current = ReflectionUtils.getField(field, current);
                if (current == null) {
                    break;
                }
            }
            return current;
        }

        @Override
        public String toString() {
            return String.format("{%s}", this.<String>getValue());
        }

        @Override
        public Expression asExpression(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
            return getPath(root, getValue());
        }
    }

    public static class Wrapper implements Operand {
        private final Object value;

        public Wrapper(Object value) {
            if (value instanceof Wrapper) {
                Wrapper wrapper = (Wrapper) value;
                this.value = wrapper.getValue();
            } else {
                this.value = value;
            }
        }

        @Override
        public Expression asExpression(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
            if (this.getValue() == null) {
                return criteriaBuilder.nullLiteral(void.class);
            }
            if (this.getValue() instanceof Expression) {
                return this.getValue();
            }
            if (this.getValue() instanceof Operand) {
                return this.<Operand>getValue().asExpression(root, criteriaQuery, criteriaBuilder);
            }
            if (this.getValue() instanceof Specification) {
                return this.<Specification>getValue().toPredicate(root, criteriaQuery, criteriaBuilder);
            }
            return criteriaBuilder.literal(getValue());
        }

        protected <T> T getValue() {
            return this.value == null ? null : (T) this.value;
        }

        @Override
        public Object evaluate() {
            return getValue();
        }

        @Override
        public String toString() {
            return this.value == null ? "null" :
                    (this.value instanceof String ? "'" + value.toString() + "'" : value.toString());
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (!Objects.equals(this.getClass(), other.getClass())) {
                return false;
            }
            Wrapper otherOperand = (Wrapper) other;
            return Objects.equals(getValue(), otherOperand.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.<Object>getValue());
        }
    }
}