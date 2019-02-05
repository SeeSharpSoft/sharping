package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.spring.data.jpa.CriteriaQueryWrapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.TupleElement;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.*;

public class Operands {

    private Operands() {
        // static
    }

    /**
     * Creates an operand from given value. Returns null if value is null.
     *
     * @param value the value to get an operand for
     * @return an Operand implementation or null if value is null
     */
    public static final Operand from(Object value) {
        if (value == null || value instanceof Operand) {
            return (Operand) value;
        }
        return new Wrapper(value);
    }

    public static final Operand asReference(String path) {
        return new FieldReference(path);
    }

    /**
     * Find an existing join.
     * @param from source
     * @param name join name
     * @param joinType join type
     * @return existing join or {@code null} if not existent
     */
    public static final Join findJoin(From<?, ?> from, String name, JoinType joinType) {
        Assert.notNull(from, "from must not be null");
        for (Join join : from.getJoins()) {
            if ((name.equals(join.getAlias()) || name.equals(join.getAttribute().getName())) && (joinType == null || joinType.equals(join.getJoinType()))) {
                return join;
            }
        }
        return null;
    }

    /**
     * Creates or reuses a join from given path to given field.
     *
     * @param from  current path
     * @param field target field
     * @return a join to given field
     */
    public static final Join getJoin(From<?, ?> from, String field) {
        Join join = findJoin(from, field, JoinType.INNER);
        return join == null ? from.join(field, JoinType.INNER) : join;
    }

    /**
     * Returns a path from given path.
     *
     * @param from       the starting path
     * @param stringPath the string representation of the path
     * @return a path
     */
    public static final Path getPath(From from, String stringPath) {
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

    public static Set<From> getAllRoots(AbstractQuery query) {
        Set<From> roots = new HashSet<>();
        roots.addAll(query.getRoots());
        if (query instanceof Subquery) {
            roots.addAll(getAllRoots(((Subquery)query).getParent()));
        }
        return roots;
    }

    public static Set<Join<?, ?>> getAllJoins(From from) {
        Set<Join<?, ?>> joins = new HashSet<>();
        Set<Join<?, ?>> fromJoins = from.getJoins();
        joins.addAll(fromJoins);
        fromJoins.forEach(join -> joins.addAll(getAllJoins(join)));
        return joins;
    }

    public static final List<Selection> getAllSelections(Selection<?> selection) {
        if (selection == null) {
            return Collections.emptyList();
        }
        if (selection.isCompoundSelection()) {
            List<Selection> result = new ArrayList<>();
            selection.getCompoundSelectionItems().forEach(compoundSelection -> result.addAll(getAllSelections(compoundSelection)));
            return result;
        }
        return Collections.singletonList(selection);
    }

    public static List<TupleElement> getContexts(AbstractQuery query) {
        List<TupleElement> elements = new ArrayList<>();
        Set<From> roots = getAllRoots(query);
        elements.addAll(roots);
        roots.forEach(root -> elements.addAll(getAllJoins(root)));
        elements.addAll(getAllSelections(query.getSelection()));
        return elements;
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
                clazz = current.getClass();
            }
            return current;
        }

        @Override
        public String toString() {
            return String.format("{%s}", this.<String>getValue());
        }

        protected Expression findExpression(From root, List<TupleElement> tupleElements, String name) {
            Assert.notNull(name, "name must not be null");
            if (root != null) {
                try {
                    return getPath(root, name);
                } catch (IllegalArgumentException exc) {
                    // TODO do not rely on an exception
                }
            }

            return tupleElements.stream()
                    .filter(element -> element instanceof Expression && name.equalsIgnoreCase(element.getAlias()))
                    .map(element -> (Expression) element)
                    .findFirst().orElse(null);
        }

        @Override
        public Expression asExpression(From root, AbstractQuery query, CriteriaBuilder criteriaBuilder, Class targetType) {
            return findExpression(root, getContexts(query), getValue());
        }

        @Override
        public Class getJavaType(From root, List<TupleElement> contexts) {
            Expression expression = findExpression(root, contexts, getValue());
            return expression == null ? null : expression.getJavaType();
        }
    }

    protected static class Wrapper implements Operand {
        private final Object value;
        private final ConversionService conversionService;

        public Wrapper(Object value, ConversionService conversionService) {
            if (value instanceof Wrapper) {
                Wrapper wrapper = (Wrapper) value;
                this.value = wrapper.getValue();
                this.conversionService = wrapper.conversionService;
            } else {
                this.value = value;
                this.conversionService = conversionService;
            }
        }

        public Wrapper(Object value) {
            this(value, DefaultConversionService.getSharedInstance());
        }

        @Override
        public Expression asExpression(From root, AbstractQuery query, CriteriaBuilder criteriaBuilder, Class targetType) {
            Object value = this.getValue();
            if (value == null) {
                return criteriaBuilder.nullLiteral(void.class);
            }
            if (value instanceof Expression) {
                return (Expression)value;
            }
            if (value instanceof Operand) {
                return ((Operand)value).asExpression(root, query, criteriaBuilder, targetType);
            }
            if (value instanceof Specification) {
                return ((Specification)value).toPredicate((Root) root, new CriteriaQueryWrapper<>(query), criteriaBuilder);
            }
            if (value instanceof Iterable) {
                final Map mapped = new HashMap();
                ((Iterable)value).forEach(item -> mapped.put(item, item));
                value = mapped;
            }
            if (value instanceof Map) {
                return criteriaBuilder.values((Map)value);
            }
            return criteriaBuilder.literal(targetType == null || targetType.equals(Void.TYPE) ? getValue() : conversionService.convert(getValue(), targetType));
        }

        @Override
        public Class getJavaType(From root, List<TupleElement> contexts) {
            if (this.getValue() instanceof Expression) {
                return this.<Expression>getValue().getJavaType();
            }
            if (this.getValue() instanceof Operand) {
                return this.<Operand>getValue().getJavaType(root, contexts);
            }
            return null;
        }

        public <T> T getValue() {
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
