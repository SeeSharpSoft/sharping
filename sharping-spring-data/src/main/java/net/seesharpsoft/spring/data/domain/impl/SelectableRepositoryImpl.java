package net.seesharpsoft.spring.data.domain.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.seesharpsoft.spring.data.domain.SelectableInfo;
import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.jpa.JpaVendorUtilProxy;
import net.seesharpsoft.spring.data.jpa.ExpressionHolder;
import net.seesharpsoft.spring.data.jpa.expression.Operands;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SelectableRepositoryImpl<T> implements SelectableRepository<T> {

    @AllArgsConstructor
    private static class SimpleTupleElement implements ExpressionHolder {

        @Getter
        private final Expression expression;

        @Getter
        private final String alias;

        public SimpleTupleElement(Expression expression) {
            this(expression, expression.getAlias());
        }

        @Override
        public Class getJavaType() {
            return expression.getJavaType();
        }
    }

    protected final EntityManager entityManager;

    protected final SqlParser sqlParser;

    protected final SelectableInfo<T> selectableInfo;

    protected final JpaVendorUtilProxy jpaVendorUtilProxy;

    public SelectableRepositoryImpl(JpaVendorUtilProxy jpaVendorUtilProxy, EntityManager entityManager, SqlParser sqlParser, Class<T> selectableClass) {
        this.jpaVendorUtilProxy = jpaVendorUtilProxy;
        this.entityManager = entityManager;
        this.sqlParser = sqlParser;
        this.selectableInfo = new SelectableInfo(sqlParser, selectableClass);
    }

    protected List<TupleElement> getAllTupleElements(AbstractQuery<?> query) {
        List<TupleElement> tupleElements = Operands.getContexts(query);
        query.getRoots().forEach((Root<?> root) ->
                root.getModel().getAttributes().forEach((Attribute attribute) -> {
                            if (attribute instanceof SingularAttribute) {
                                tupleElements.add(new SimpleTupleElement(root.get(attribute.getName()), attribute.getName()));
                            }
                        }
                )
        );
        return tupleElements;
    }

    protected CriteriaQuery<T> prepareJoins(Root root, CriteriaQuery query, CriteriaBuilder builder) {
        for (SelectableInfo.JoinInfo joinInfo : selectableInfo.getJoins()) {
            String[] paths = Operands.getPathParts(joinInfo.getJoinPath());
            Path joinPath = (Path) Operands.getPath(root, paths, getAllTupleElements(query));
            Join join = Operands.createJoin((From) joinPath.getParentPath(), paths[paths.length - 1], joinInfo.getJoinType());
            Assert.notNull(join, "join instance expected!");
            if (joinInfo.getAlias() != null) {
                join.alias(joinInfo.getAlias());
            }
            if (joinInfo.getOn() != null) {
                Predicate prevOn = join.getOn();
                if (prevOn == null) {
                    join.on(joinInfo.getOn().asExpression(root, query, builder, Boolean.class));
                } else {
                    join.on(prevOn, (Predicate) joinInfo.getOn().asExpression(root, query, builder, Boolean.class));
                }
            }
        }
        return query;
    }

    protected AbstractQuery prepareSelection(Root root, CriteriaQuery<T> query, CriteriaBuilder builder) {
        List<Selection<?>> selections = new ArrayList<>();
        for (SelectableInfo.FieldInfo fieldInfo : selectableInfo.getFields()) {
            Selection selection = fieldInfo.getSelection().asExpression(root, query, builder, null);
            if (fieldInfo.getAlias() != null) {
                selection.alias(fieldInfo.getAlias());
            }
            selections.add(selection);
        }
        return query.multiselect(selections);
    }

    protected AbstractQuery prepareWhere(Root root, AbstractQuery query, CriteriaBuilder builder) {
        if (selectableInfo.getWhere() != null) {
            return query.where(selectableInfo.getWhere().asExpression(root, query, builder, Boolean.class));
        }
        return query;
    }

    protected boolean isAggregateFunction(Expression expression) {
        return jpaVendorUtilProxy.isAggregateFunction(expression);
    }

    protected AbstractQuery prepareGroupBy(Root root, AbstractQuery query, CriteriaBuilder builder) {
        List<Expression> groupBys = new ArrayList<>();
        for (Selection<?> selection : Operands.getAllSelections(query.getSelection())) {
            Assert.isInstanceOf(Expression.class, selection, "selection is expected to be an expression!");
            Expression expression = (Expression) selection;
            if (!isAggregateFunction(expression) && !(expression instanceof Predicate)) {
                groupBys.add(expression);
            }
        }
        return query.groupBy(groupBys);
    }

    protected AbstractQuery prepareHaving(Root root, AbstractQuery query, CriteriaBuilder builder) {
        if (selectableInfo.getHaving() != null) {
            return query.having(selectableInfo.getHaving().asExpression(root, query, builder, Boolean.class));
        }
        return query;
    }

    protected CriteriaQuery<T> prepareQuery(Root root, CriteriaQuery query, CriteriaBuilder builder) {
        return (CriteriaQuery<T>) prepareHaving(
                root,
                prepareGroupBy(
                        root,
                        prepareWhere(
                                root,
                                prepareSelection(
                                        root,
                                        prepareJoins(root, query, builder),
                                        builder
                                ),
                                builder
                        ),
                        builder
                ),
                builder
        );
    }

    protected CriteriaQuery<T> applySpecification(Root root, CriteriaQuery<T> query, CriteriaBuilder builder, Specification specification) {
        Predicate predicate = specification == null ? null : specification.toPredicate(root, query, builder);
        if (predicate == null) {
            return query;
        }

        Predicate restriction = query.getRestriction();
        if (restriction == null) {
            query.where(predicate);
        } else {
            query.where(restriction, predicate);
        }
        return query;
    }

    protected CriteriaQuery<T> applySort(Root root, CriteriaQuery<T> query, CriteriaBuilder builder, Sort sort) {
        if (sort == null) {
            return query;
        }
        List<TupleElement> elements = getAllTupleElements(query);
        List<Order> orders = new ArrayList<>();
        Iterator<Sort.Order> sortIterator = sort.iterator();
        List<Expression<?>> groupBys = new ArrayList<>(query.getGroupList());
        while (sortIterator.hasNext()) {
            Sort.Order sortOrder = sortIterator.next();
            Expression orderExpression = Operands.getPath(root, sortOrder.getProperty(), elements);
            Assert.notNull(orderExpression, String.format("order expression for '%s' not found!", sortOrder.getProperty()));
            if (!groupBys.contains(orderExpression) && !isAggregateFunction(orderExpression)) {
                groupBys.add(orderExpression);
            }
            orders.add(sortOrder.isAscending() ? builder.asc(orderExpression) : builder.desc(orderExpression));
        }
        query.orderBy(orders);
        query.groupBy(groupBys);
        return query;
    }

    protected CriteriaQuery<T> createCriteriaQuery(Specification specification, Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(selectableInfo.getSelectableClass());
        Root root = query.from(selectableInfo.getRootClass());
        query = prepareQuery(root, query, builder);
        query = applySpecification(root, query, builder, specification);
        query = applySort(root, query, builder, sort);
        return query;
    }

    protected TypedQuery createTypedQuery(Specification spec, Sort sort) {
        CriteriaQuery query = createCriteriaQuery(spec, sort);
        return entityManager.createQuery(query);
    }

    protected TypedQuery createTypedQuery(Specification spec, Pageable pageable) {
        TypedQuery typedQuery = createTypedQuery(spec, pageable == null ? null : pageable.getSort());
        if (pageable != null) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }
        return typedQuery;
    }

    @Override
    public Optional<T> findOne(Specification spec) {
        List<T> resultList = findAll(spec);
        Assert.state(resultList.size() < 2, "findOne has ambiguous results!");
        return resultList.size() == 0 ? Optional.empty() : Optional.of(resultList.get(0));
    }

    @Override
    public List<T> findAll(Specification spec) {
        return findAll(spec, (Sort) null);
    }

    @Override
    public Page<T> findAll(Specification spec, Pageable pageable) {
        TypedQuery<T> typedQuery = createTypedQuery(spec, pageable);
        List<T> resultList = typedQuery.getResultList();
        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    @Override
    public List<T> findAll(Specification spec, Sort sort) {
        TypedQuery<T> typedQuery = createTypedQuery(spec, sort);
        return typedQuery.getResultList();
    }

    @Override
    public long count(Specification spec) {
        // TODO create count/count-distinct query
        return findAll(spec).size();
    }
}
