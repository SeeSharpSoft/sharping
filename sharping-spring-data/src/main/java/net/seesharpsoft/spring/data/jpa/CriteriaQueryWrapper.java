package net.seesharpsoft.spring.data.jpa;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;

/**
 * Wrapper for abstract or criteria queries.
 *
 * @param <T> the query result type
 */
public class CriteriaQueryWrapper<T> implements CriteriaQuery<T> {

    private final AbstractQuery<T> query;

    public CriteriaQueryWrapper(AbstractQuery abstractQuery) {
        this.query = abstractQuery;
    }

    @Override
    public CriteriaQuery<T> select(Selection<? extends T> selection) {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).select(selection);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).multiselect(selections);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).multiselect(selectionList);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <X> Root<X> from(Class<X> entityClass) {
        return query.from(entityClass);
    }

    @Override
    public <X> Root<X> from(EntityType<X> entity) {
        return query.from(entity);
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> restriction) {
        query.where(restriction);
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... restrictions) {
        query.where(restrictions);
        return this;
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
        query.groupBy(grouping);
        return this;
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
        query.groupBy(grouping);
        return this;
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> restriction) {
        query.having(restriction);
        return this;
    }

    @Override
    public CriteriaQuery<T> having(Predicate... restrictions) {
        query.having(restrictions);
        return this;
    }

    @Override
    public CriteriaQuery<T> orderBy(Order... orders) {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).orderBy(orders);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> orderBy(List<Order> orders) {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).orderBy(orders);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
        query.distinct(distinct);
        return this;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return query.getRoots();
    }

    @Override
    public Selection<T> getSelection() {
        return query.getSelection();
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return query.getGroupList();
    }

    @Override
    public Predicate getGroupRestriction() {
        return query.getGroupRestriction();
    }

    @Override
    public boolean isDistinct() {
        return query.isDistinct();
    }

    @Override
    public Class<T> getResultType() {
        return query.getResultType();
    }

    @Override
    public List<Order> getOrderList() {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).getOrderList();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        if (query instanceof CriteriaQuery) {
            return ((CriteriaQuery) query).getParameters();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        return query.subquery(type);
    }

    @Override
    public Predicate getRestriction() {
        return query.getRestriction();
    }
}
