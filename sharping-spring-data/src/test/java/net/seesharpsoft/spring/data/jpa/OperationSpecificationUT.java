package net.seesharpsoft.spring.data.jpa;

import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.Parser;
import net.seesharpsoft.spring.test.mock.CriteriaBuilderMockBuilder;
import net.seesharpsoft.spring.test.mock.CriteriaQueryMockBuilder;
import net.seesharpsoft.spring.test.mock.ExpressionMockBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OperationSpecificationUT {

    private CriteriaBuilder builder;

    private CriteriaQuery query;

    private Root root;

    @BeforeEach
    public void before() {
        builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        query = CriteriaQueryMockBuilder.newCriteriaQuery();
        root = ExpressionMockBuilder.newRoot(null);
    }

    @Test
    public void toPredicate_should_return_proper_predicate() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.JAVA);
        Specification specification = new OperationSpecification(
                parser.parseExpression("fString == 'eq < ne' && (fInteger < 3 || fDouble >= 123 || !(fInteger != fDouble / 4))")
        );

        Predicate predicate = specification.toPredicate(root, query, builder);
        assertThat(predicate.toString(), is("and(equal(fString, 'eq < ne'), or(lessThan(fInteger, '3'), or(greaterThanOrEqualTo(fDouble, '123'), not(not(equal(fInteger, quot(fDouble, '4')))))))"));
    }

    @Test
    public void toPredicate_should_return_proper_predicate_including_null_values() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.JAVA);
        Specification specification = new OperationSpecification(
                parser.parseExpression("(fString != null && fString == 'eq < ne') && (fInteger < 3 || fDouble >= 123 || !(fInteger != fDouble / 4))")
        );

        Predicate predicate = specification.toPredicate(root, query, builder);
        assertThat(predicate.toString(), is("and(and(not(isNull(fString)), equal(fString, 'eq < ne')), or(lessThan(fInteger, '3'), or(greaterThanOrEqualTo(fDouble, '123'), not(not(equal(fInteger, quot(fDouble, '4')))))))"));
    }
}
