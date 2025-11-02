package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.commons.collection.Pair;
import net.seesharpsoft.spring.test.mock.CriteriaBuilderMockBuilder;
import net.seesharpsoft.spring.test.mock.CriteriaQueryMockBuilder;
import net.seesharpsoft.spring.test.mock.ExpressionMockBuilder;
import org.junit.jupiter.api.Test;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OperatorsUT {

    @Test
    public void binary_operator_asExpression_should_handle_null_values() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.OR.createExpression(root, query, builder, null, null);
        assertThat(expression.toString(), is("or(null, null)"));
    }

    @Test
    public void binary_operator_asExpression_should_handle_null_value() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.OR.createExpression(root, query, builder, null);
        assertThat(expression.toString(), is("or(null, null)"));
    }

    @Test
    public void binary_operator_asExpression_should_fail_without_values() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
            CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
            Root root = ExpressionMockBuilder.newRoot(null);

            Operators.OR.createExpression(root, query, builder);
        });
    }

    @Test
    public void equals_operator_asExpression_should_handle_null_values() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.EQUALS.createExpression(root, query, builder, null, null);
        assertThat(expression.toString(), is("and()"));
    }

    @Test
    public void equals_operator_asExpression_should_handle_null_value() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.EQUALS.createExpression(root, query, builder, null);
        assertThat(expression.toString(), is("and()"));
    }

    @Test
    public void equals_operator_asExpression_should_fail_without_values() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
            CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
            Root root = ExpressionMockBuilder.newRoot(null);

            Operators.EQUALS.createExpression(root, query, builder);
        });
    }

    @Test
    public void unary_operator_asExpression_should_handle_null_value() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.NOT.createExpression(root, query, builder, null);
        assertThat(expression.toString(), is("not(null)"));
    }

    @Test
    public void unary_operator_asExpression_should_handle_no_values() {
        assertThrows(IllegalArgumentException.class, () -> {
            CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
            CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
            Root root = ExpressionMockBuilder.newRoot(null);

            Operators.NOT.createExpression(root, query, builder);
        });
    }

    @Test
    public void numerical_operators_should_evaluate_correctly() {
        List<Pair> inputs = new ArrayList<>();
        inputs.add(Pair.of(2, 2));
        inputs.add(Pair.of(2.0, 3));
        inputs.add(Pair.of(null, 3));
        inputs.add(Pair.of(null, null));
        inputs.add(Pair.of(-1f, 10));
        inputs.add(Pair.of(4, 0d));
        inputs.add(Pair.of(4, -2f));

        Map<Operator, List> operatorResultMap = new HashMap<>();
        operatorResultMap.put(Operators.ADD, Arrays.asList(4, 5.0, 3, 0, 9f, 4, 2));
        operatorResultMap.put(Operators.SUB, Arrays.asList(0, -1.0, -3, 0, -11f, 4, 6));
        operatorResultMap.put(Operators.DIV, Arrays.asList(1, BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(3), Operators.Numerical.MATH_CONTEXT).doubleValue(), 0, null, -1f/10, null, -2));
        operatorResultMap.put(Operators.MUL, Arrays.asList(4, 6.0, 0, 0, -10f, 0, -8));
        operatorResultMap.put(Operators.MOD, Arrays.asList(0, 2.0, 0, null, -1f, null, 0));

        operatorResultMap.forEach((key, value) -> {
                    for (int i = 0; i < inputs.size(); ++i) {
                        Object result = value.get(i);
                        Pair pair = inputs.get(i);
                        if (result == null) {
                            boolean failed = false;
                            try {
                                key.evaluate(pair.getFirst(), pair.getSecond());
                            } catch(ArithmeticException exc) {
                                failed = true;
                            }
                            assertThat(String.format("%s(%s, %s) should fail", key, pair.getFirst(), pair.getSecond()), failed, is(true));
                        } else {
                            assertThat(String.format("%s(%s, %s) = %s", key, pair.getFirst(), pair.getSecond(), result), key.evaluate(pair.getFirst(), pair.getSecond()), is(result));
                        }
                    }
                });
    }
}
