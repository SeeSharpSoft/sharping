package net.seesharpsoft.spring.data.domain;

import jakarta.persistence.Transient;
import jakarta.persistence.criteria.JoinType;
import net.seesharpsoft.spring.data.jpa.expression.Operand;
import net.seesharpsoft.spring.data.jpa.expression.Operation;
import net.seesharpsoft.spring.data.jpa.selectable.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectableInfo<T> {

    public class FieldInfo {

        protected final Field field;

        protected final Operand selection;

        protected final String alias;

        public FieldInfo(SqlParser parser, Field field, Alias alias) {
            this.field = field;
            this.alias = alias == null ? field.getName() : alias.value();

            Select selectAnnotation = field.getAnnotation(Select.class);
            String expression = selectAnnotation == null ? field.getName() : selectAnnotation.value();
            this.selection = parser.parseExpression(expression);
        }

        public Operand getSelection() {
            return this.selection;
        }

        public Field getField() {
            return field;
        }

        public String getAlias() {
            return alias;
        }
    }

    public class JoinInfo {
        protected final String joinPath;

        protected final JoinType joinType;

        protected final String alias;

        protected final Operation on;

        public JoinInfo(SqlParser parser, Join joinDefinition) {
            this.joinPath = joinDefinition.value();
            this.joinType = joinDefinition.type();
            this.alias = joinDefinition.alias().isEmpty() ? joinDefinition.value() : joinDefinition.alias();
            this.on = joinDefinition.on().isEmpty() ? null : parser.parseExpression(joinDefinition.on());
        }

        public String getJoinPath() {
            return joinPath;
        }

        public JoinType getJoinType() {
            return joinType;
        }

        public String getAlias() {
            return alias;
        }

        public Operation getOn() {
            return on;
        }
    }

    protected final Class<T> selectableClass;

    protected final Class rootClass;

    protected final List<FieldInfo> fields;

    protected final List<JoinInfo> joins;

    protected final Operation where;

    protected final Operation having;

    public SelectableInfo(SqlParser parser, Class<T> selectableClass) {
        this.selectableClass = selectableClass;

        Selectable selectableAnnotation = selectableClass.getAnnotation(Selectable.class);
        if (selectableAnnotation != null) {
            this.rootClass = selectableAnnotation.from().equals(void.class) ? selectableClass : selectableAnnotation.from();
            this.joins = createJoinInfos(parser, selectableAnnotation.joins());
            this.where = parser.parseExpression(selectableAnnotation.where());
            this.having = parser.parseExpression(selectableAnnotation.having());

        } else {
            this.rootClass = selectableClass;
            this.joins = Collections.emptyList();
            this.where = null;
            this.having = null;
        }

        this.fields = createSelectableFields(parser);
    }

    protected boolean isSelectField(Field field) {
        Select selectAnnotation = field.getAnnotation(Select.class);
        return (selectAnnotation != null && !selectAnnotation.ignore()) ||
                (selectAnnotation == null && !field.isAnnotationPresent(Transient.class));
    }

    protected List<FieldInfo> createSelectableFields(SqlParser parser) {
        final List<FieldInfo> selectableFields = new ArrayList<>();
        ReflectionUtils.doWithFields(selectableClass, field -> {
            if (isSelectField(field)) {
                selectableFields.add(new FieldInfo(parser, field, field.getAnnotation(Alias.class)));
            }
        });
        return selectableFields;
    }

    protected List<JoinInfo> createJoinInfos(SqlParser parser, Joins joins) {
        final List<JoinInfo> joinInfos = new ArrayList<>();
        for (Join joinDefinition : joins.value()) {
            joinInfos.add(new JoinInfo(parser, joinDefinition));
        }
        return joinInfos;
    }

    public Class<T> getSelectableClass() {
        return selectableClass;
    }

    public Class getRootClass() {
        return rootClass;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public List<JoinInfo> getJoins() {
        return joins;
    }

    public Operation getWhere() {
        return where;
    }

    public Operation getHaving() {
        return having;
    }
}
