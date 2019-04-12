package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.spring.data.jpa.CriteriaQueryWrapper;
import net.seesharpsoft.spring.data.jpa.ExpressionHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.TupleElement;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
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
     *
     * @param from     source
     * @param name     join name
     * @param joinType join type
     * @return existing join or {@code null} if not existent
     */
    public static final Join findJoin(From<?, ?> from, String name, JoinType joinType) {
        Assert.notNull(from, "from must not be null");
        for (Join join : from.getJoins()) {
            if ((name.equalsIgnoreCase(join.getAlias()) || (join.getAlias() == null && name.equalsIgnoreCase(join.getAttribute().getName()))) &&
                    (joinType == null || joinType.equals(join.getJoinType()))) {
                return join;
            }
        }
        return null;
    }

    /**
     * Creates or reuses a join from given path to given field.
     *
     * @param from     current path
     * @param field    target field
     * @param joinType join type
     * @return a join to given field
     */
    public static final Join getJoin(From<?, ?> from, String field, JoinType joinType) {
        Join join = findJoin(from, field, joinType);
        return join == null ? createJoin(from, field, joinType) : join;
    }

    /**
     * Creates or reuses a join from given path to given field.
     *
     * @param from  current path
     * @param field target field
     * @return a join to given field
     */
    public static final Join getJoin(From<?, ?> from, String field) {
        return getJoin(from, field, JoinType.LEFT);
    }

    /**
     * Creates a join from given path to given field.
     *
     * @param from  current path
     * @param field target field
     * @return a join to given field
     */
    public static final Join createJoin(From<?, ?> from, String field, JoinType joinType) {
        return from.join(field, joinType);
    }

    /**
     * Parses the given elements for the given alias.
     *
     * @param nameOrAlias the alias to search for
     * @param elements    available elements
     * @return the found element as expression or null
     */
    public static final Expression findExpression(String nameOrAlias, List<TupleElement> elements) {
        Assert.notNull(elements, "elements must not be null!");
        return elements.stream()
                .filter(element ->
                        ((element instanceof Expression || element instanceof ExpressionHolder) && nameOrAlias.equalsIgnoreCase(element.getAlias())) ||
                                (element.getAlias() == null && element instanceof Path && (((Path) element).getModel() instanceof Attribute) && nameOrAlias.equalsIgnoreCase(((Attribute) ((Path) element).getModel()).getName()))
                )
                .map(element -> element instanceof ExpressionHolder ? ((ExpressionHolder) element).getExpression() : (Expression) element)
                .findFirst().orElse(null);
    }

    /**
     * Returns an expression or null if not found.
     *
     * @param from              the starting path
     * @param nameOrAliasOrPath name, alias or path of the expression
     * @param query             the query
     * @return an expression
     */
    public static final Expression getPath(From from, String nameOrAliasOrPath, AbstractQuery query) {
        return getPath(from, nameOrAliasOrPath, getContexts(query));
    }

    /**
     * Splits the path by '/', '\' or '.'.
     *
     * @param fullPath the full path like "user/name"
     * @return the parts as array, e.g. ["user", "name"]
     */
    public static final String[] getPathParts(String fullPath) {
        return fullPath.split("[/.\\\\]");
    }

    /**
     * Returns an expression for given path.
     *
     * @param from     the starting path
     * @param paths    all parts of path split
     * @param elements available elements to search in
     * @return a path
     */
    public static final Expression getPath(From from, String[] paths, List<TupleElement> elements) {
        Assert.notNull(elements, "elements must not be null");
        if (paths == null || paths.length == 0) {
            return from;
        }
        Path current = from;
        int firstIndex = 0;
        int lastIndex = paths.length - 1;
        Expression expression = findExpression(paths[0], elements);
        if (expression != null) {
            if (paths.length == 1) {
                return expression;
            } else if (expression instanceof Path) {
                current = (Path) expression;
                ++firstIndex;
            }
        }

        for (int index = firstIndex; index < lastIndex; ++index) {
            current = getJoin((From) current, paths[index]);
        }
        return current.get(paths[lastIndex]);
    }

    /**
     * Returns an expression for given path.
     *
     * @param from              the starting path
     * @param nameOrAliasOrPath name, alias or path of the expression
     * @param elements          available elements to search in
     * @return a path
     */
    public static final Expression getPath(From from, String nameOrAliasOrPath, List<TupleElement> elements) {
        if (nameOrAliasOrPath == null || nameOrAliasOrPath.isEmpty()) {
            return from;
        }
        return getPath(from, getPathParts(nameOrAliasOrPath), elements);
    }

    /**
     * Returns an expression for given path.
     *
     * @param from              the starting path
     * @param nameOrAliasOrPath name, alias or path of the expression
     * @return a path
     */
    public static final Expression getPath(From from, String nameOrAliasOrPath) {
        return getPath(from, nameOrAliasOrPath, Collections.emptyList());
    }

    public static Set<Root<?>> getAllRoots(AbstractQuery query) {
        Set<Root<?>> roots = new HashSet<>();
        roots.addAll(query.getRoots());
        if (query instanceof Subquery) {
            roots.addAll(getAllRoots(((Subquery) query).getParent()));
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
        Set<Root<?>> roots = getAllRoots(query);
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

        @Override
        public Expression asExpression(From root, AbstractQuery query, CriteriaBuilder criteriaBuilder, Class targetType) {
            return getPath(root, getValue(), query);
        }

        @Override
        public Class getJavaType(From root, List<TupleElement> contexts) {
            Expression expression = getPath(root, (String) getValue(), contexts);
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
                return (Expression) value;
            }
            if (value instanceof Operand) {
                return ((Operand) value).asExpression(root, query, criteriaBuilder, targetType);
            }
            if (value instanceof Specification) {
                return ((Specification) value).toPredicate((Root) root, new CriteriaQueryWrapper<>(query), criteriaBuilder);
            }
            if (value instanceof Iterable) {
                final Map mapped = new HashMap();
                ((Iterable) value).forEach(item -> mapped.put(item, item));
                value = mapped;
            }
            if (value instanceof Map) {
                return criteriaBuilder.values((Map) value);
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
