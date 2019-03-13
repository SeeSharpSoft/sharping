package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.jpa.expression.Operation;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SelectableInfo {

    public class SelectableField {

        protected final Field field;

        protected final Operation selection;

        public SelectableField(SqlParser parser, Field field) {
            this.field = field;

            Select selectAnnotation = field.getAnnotation(Select.class);
            String expression = selectAnnotation == null ? field.getName() : selectAnnotation.value();
            this.selection = parser.parseExpression(expression);
        }

        public Operation getSelection() {
            return this.selection;
        }
    }

    protected final Class rootClass;

    protected final List<SelectableField> fields;

    protected final Operation where;

    public SelectableInfo(SqlParser parser, Class<?> selectableClass) {
        this.fields = createSelectableFields(parser);

        Selectable selectableAnnotation = selectableClass.getAnnotation(Selectable.class);
        if (selectableAnnotation != null) {
            this.rootClass = selectableAnnotation.from().equals(void.class) ? selectableClass : selectableAnnotation.from();
            where = parser.parseExpression(selectableAnnotation.where());
        } else {
            this.rootClass = selectableClass;
            where = null;
        }
    }

    protected boolean isSelectField(Field field) {
        Select selectAnnotation = field.getAnnotation(Select.class);
        return (selectAnnotation != null && !selectAnnotation.ignore()) ||
                (selectAnnotation == null && !field.isAnnotationPresent(Transient.class));
    }

    protected List<SelectableField> createSelectableFields(SqlParser parser) {
        final List<SelectableField> selectableFields = new ArrayList<>();
        ReflectionUtils.doWithFields(rootClass, field -> {
            if (isSelectField(field)) {
                selectableFields.add(new SelectableField(parser, field));
            }
        });
        return selectableFields;
    }
}
