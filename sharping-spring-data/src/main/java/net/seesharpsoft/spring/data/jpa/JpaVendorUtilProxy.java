package net.seesharpsoft.spring.data.jpa;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface JpaVendorUtilProxy {
    boolean isAggregateFunction(Expression<?> expression);

    default List<Selection<?>> getAllSelections(Selection<?> selection) {
        if (selection == null) {
            return Collections.emptyList();
        }
        if (selection.isCompoundSelection()) {
            List<Selection<?>> result = new ArrayList<>();
            selection.getCompoundSelectionItems().forEach(compoundSelection -> result.addAll(getAllSelections(compoundSelection)));
            return result;
        }
        return Collections.singletonList(selection);
    }
}
