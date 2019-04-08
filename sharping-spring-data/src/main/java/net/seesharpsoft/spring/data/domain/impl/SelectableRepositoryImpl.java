package net.seesharpsoft.spring.data.domain.impl;

import net.seesharpsoft.spring.data.domain.SelectableInfo;
import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.jpa.expression.Operands;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectableRepositoryImpl<T> implements SelectableRepository<T> {

    protected final EntityManager entityManager;

    protected final SqlParser sqlParser;

    protected final SelectableInfo<T> selectableInfo;

    public SelectableRepositoryImpl(EntityManager entityManager, SqlParser sqlParser, Class selectableClass) {
        this.entityManager = entityManager;
        this.sqlParser = sqlParser;
        this.selectableInfo = new SelectableInfo(sqlParser, selectableClass);
    }

    protected CriteriaQuery<T> prepareJoins(Root root, CriteriaQuery query, CriteriaBuilder builder) {
        for (SelectableInfo.JoinInfo joinInfo : selectableInfo.getJoins()) {
            Join join = (Join) Operands.getPath(root, joinInfo.getJoinPath(), query);
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

    private AbstractQuery prepareSelection(Root root, CriteriaQuery<T> query, CriteriaBuilder builder) {
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

    private AbstractQuery prepareWhere(Root root, AbstractQuery query, CriteriaBuilder builder) {
        if (selectableInfo.getWhere() != null) {
            return query.where(selectableInfo.getWhere().asExpression(root, query, builder, Boolean.class));
        }
        return query;
    }

    private AbstractQuery prepareGroupBy(Root root, AbstractQuery query, CriteriaBuilder builder) {
        List<Expression> groupBys = new ArrayList<>();
        for (Selection<?> selection : Operands.getAllSelections(query.getSelection())) {

        }
        return query;
    }

    private AbstractQuery prepareHaving(Root root, AbstractQuery query, CriteriaBuilder builder) {
        if (selectableInfo.getHaving() != null) {
            return query.having(selectableInfo.getHaving().asExpression(root, query, builder, Boolean.class));
        }
        return query;
    }

//    private AbstractQuery prepareOrderBy(Root root, AbstractQuery query, CriteriaBuilder builder) {
//        return query;
//    }


    protected CriteriaQuery<T> prepareQuery(Root root, CriteriaQuery query, CriteriaBuilder builder) {
        return (CriteriaQuery<T>) prepareHaving(
                root,
                prepareGroupBy(
                        root,
                        prepareWhere(
                                root,
                                prepareSelection(root, prepareJoins(root, query, builder), builder),
                                builder
                        ),
                        builder
                ),
                builder
        );
    }

    protected CriteriaQuery<T> applySpecification(Root root, CriteriaQuery<T> query, CriteriaBuilder builder, Specification specification) {
        if (specification == null) {
            return query;
        }

        Predicate restriction = query.getRestriction();
        if (restriction == null) {
            query.where(specification.toPredicate(root, query, builder));
        } else {
            query.where(restriction, specification.toPredicate(root, query, builder));
        }
        return query;
    }

    protected CriteriaQuery<T> applySort(Root root, CriteriaQuery<T> query, CriteriaBuilder builder, Sort sort) {
        if (sort == null) {
            return query;
        }
        List<TupleElement> elements = Operands.getContexts(query);
        List<Order> orders = new ArrayList<>();
        Iterator<Sort.Order> sortIterator = sort.iterator();
        while (sortIterator.hasNext()) {
            Sort.Order sortOrder = sortIterator.next();
            Expression orderExpression = Operands.findExpression(sortOrder.getProperty(), elements);
            orders.add(sortOrder.isAscending() ? builder.asc(orderExpression) : builder.desc(orderExpression));
        }
        query.orderBy(orders);
        return query;
    }

    protected CriteriaQuery<T> createQuery(Specification specification, Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(selectableInfo.getRootClass());
        Root root = query.from(selectableInfo.getRootClass());
        query = prepareQuery(root, query, builder);
        query = applySpecification(root, query, builder, specification);
        query = applySort(root, query, builder, sort);
        return query;
    }

    protected TypedQuery createQuery(Specification spec, Pageable pageable) {
        CriteriaQuery query = createQuery(spec, pageable == null ? null : pageable.getSort());
        TypedQuery typedQuery = entityManager.createQuery(query);
        if (pageable != null) {
            typedQuery.setFirstResult(pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }
        return typedQuery;
    }

    @Override
    public T findOne(Specification spec) {
        TypedQuery query = createQuery(spec, (Pageable)null);

        return null;
    }

    @Override
    public List<T> findAll(Specification spec) {
        return null;
    }

    @Override
    public Page<T> findAll(Specification spec, Pageable pageable) {
        return null;
    }

    @Override
    public List<T> findAll(Specification spec, Sort sort) {
        return null;
    }

    @Override
    public long count(Specification spec) {
        return 0;
    }
}
