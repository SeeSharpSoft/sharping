package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.spring.test.mock.CriteriaBuilderMockBuilder;
import net.seesharpsoft.spring.test.mock.CriteriaQueryMockBuilder;
import net.seesharpsoft.spring.test.mock.ExpressionMockBuilder;
import org.junit.Test;
import javafx.util.Pair;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

    @Test(expected = IllegalArgumentException.class)
    public void binary_operator_asExpression_should_fail_without_values() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Operators.OR.createExpression(root, query, builder);
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

    @Test(expected = IllegalArgumentException.class)
    public void equals_operator_asExpression_should_fail_without_values() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Operators.EQUALS.createExpression(root, query, builder);
    }

    @Test
    public void unary_operator_asExpression_should_handle_null_value() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Expression expression = Operators.NOT.createExpression(root, query, builder, null);
        assertThat(expression.toString(), is("not(null)"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unary_operator_asExpression_should_handle_no_values() {
        CriteriaBuilder builder = CriteriaBuilderMockBuilder.newCriteriaBuilder();
        CriteriaQuery query = CriteriaQueryMockBuilder.newCriteriaQuery();
        Root root = ExpressionMockBuilder.newRoot(null);

        Operators.NOT.createExpression(root, query, builder);
    }

    @Test
    public void numerical_operators_should_evaluate_correctly() {
        List<Pair> inputs = new ArrayList<>();
        inputs.add(new Pair(2, 2));
        inputs.add(new Pair(2.0, 3));
        inputs.add(new Pair(null, 3));
        inputs.add(new Pair(null, null));
        inputs.add(new Pair(-1f, 10));
        inputs.add(new Pair(4, 0d));
        inputs.add(new Pair(4, -2f));

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
                                key.evaluate(pair.getKey(), pair.getValue());
                            } catch(ArithmeticException exc) {
                                failed = true;
                            }
                            assertThat(String.format("%s(%s, %s) should fail", key, pair.getKey(), pair.getValue()), failed, is(true));
                        } else {
                            assertThat(String.format("%s(%s, %s) = %s", key, pair.getKey(), pair.getValue(), result), key.evaluate(pair.getKey(), pair.getValue()), is(result));
                        }
                    }
                });
    }
}
