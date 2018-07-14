package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public interface Operand {



    enum Type {
        OBJECT,
        PATH,
        EXPRESSION,
        OPERATION,
        SPECIFICATION
    }

    Object evaluate();

    Expression asExpression(Root root,
                            CriteriaQuery criteriaQuery,
                            CriteriaBuilder criteriaBuilder);
}
