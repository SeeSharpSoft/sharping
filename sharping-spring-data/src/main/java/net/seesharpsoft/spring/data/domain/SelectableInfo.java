package net.seesharpsoft.spring.data.domain;

import lombok.Getter;
import net.seesharpsoft.spring.data.jpa.expression.Operand;
import net.seesharpsoft.spring.data.jpa.expression.Operation;
import net.seesharpsoft.spring.data.jpa.selectable.*;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Transient;
import javax.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectableInfo<T> {

    public class FieldInfo {

        @Getter
        protected final Field field;

        @Getter
        protected final Operand selection;

        @Getter
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
    }

    public class JoinInfo {
        @Getter
        protected final String joinPath;

        @Getter
        protected final JoinType joinType;

        @Getter
        protected final String alias;

        @Getter
        protected final Operation on;

        public JoinInfo(SqlParser parser, Join joinDefinition) {
            this.joinPath = joinDefinition.value();
            this.joinType = joinDefinition.type();
            this.alias = joinDefinition.alias().isEmpty() ? joinDefinition.value() : joinDefinition.alias();
            this.on = joinDefinition.on().isEmpty() ? null : parser.parseExpression(joinDefinition.on());
        }
    }

    @Getter
    protected final Class<T> selectableClass;

    @Getter
    protected final Class rootClass;

    @Getter
    protected final List<FieldInfo> fields;

    @Getter
    protected final List<JoinInfo> joins;

    @Getter
    protected final Operation where;

    @Getter
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
        ReflectionUtils.doWithFields(rootClass, field -> {
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
}
