package net.seesharpsoft.spring.test;

import jakarta.persistence.criteria.Selection;
import net.seesharpsoft.spring.data.jpa.JpaVendorUtilProxy;

import jakarta.persistence.criteria.Expression;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.tree.expression.SqmFunction;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JpaVendorUtilProxyDummy implements JpaVendorUtilProxy {
    @Override
    public boolean isAggregateFunction(Expression expression) {
        return expression instanceof SqmFunction && ((SqmFunction<?>) expression).getFunctionDescriptor().getFunctionKind().equals(FunctionKind.AGGREGATE);
    }

    @Override
    public List<Selection<?>> getAllSelections(Selection<?> selection) {
        if (selection == null) {
            return Collections.emptyList();
        }
        List<? extends Selection<?>> compoundItems = null;
        // TODO this is a bug?!
        if (selection instanceof SqmDynamicInstantiation<?>) {
            compoundItems = ((JpaSelection<?>) selection).getSelectionItems();
        } else if (selection.isCompoundSelection()) {
            compoundItems = selection.getCompoundSelectionItems();
        }

        if (compoundItems != null) {
            List<Selection<?>> result = new ArrayList<>();
            compoundItems.forEach(compoundSelection -> result.addAll(getAllSelections(compoundSelection)));
            return result;
        }
        return Collections.singletonList(selection);
    }
}
