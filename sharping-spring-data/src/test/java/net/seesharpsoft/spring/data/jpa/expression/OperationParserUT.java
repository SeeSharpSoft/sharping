package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.commons.util.Tokenizer;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OperationParserUT {

    @Test
    public void odata_tokenize_should_return_correct_token_list_very_simple() throws ParseException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<OperationParser.Token>> tokenInfos = operationParser.tokenize("a eq 1");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "1")
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_simple() throws ParseException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<OperationParser.Token>> tokenInfos = operationParser.tokenize("a eq 'eq lt ne' AND b lt 3");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "'eq lt ne'"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "AND"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "b"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "lt"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "3")
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_normal() throws ParseException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<OperationParser.Token>> tokenInfos = operationParser.tokenize("a eq 'eq lt ne' AND (b lt 3 or c gt '123' or not (a/b in [test,test2]))");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "'eq lt ne'"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "AND"),
                new Tokenizer.TokenInfo(OperationParser.Token.BRACKET_OPEN, "("),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "b"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "lt"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "3"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "or"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "c"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "gt"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "'123'"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "or"),
                new Tokenizer.TokenInfo(OperationParser.Token.UNARY_OPERATOR, "not"),
                new Tokenizer.TokenInfo(OperationParser.Token.BRACKET_OPEN, "("),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "a/b"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERATOR, "in"),
                new Tokenizer.TokenInfo(OperationParser.Token.OPERAND, "[test,test2]"),
                new Tokenizer.TokenInfo(OperationParser.Token.BRACKET_CLOSE, ")"),
                new Tokenizer.TokenInfo(OperationParser.Token.BRACKET_CLOSE, ")")
        ));
    }

    @Test
    public void odata_parse_should_return_correct_specification_normal() throws ParseException, IllegalAccessException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        Operation operation = operationParser.parse("a eq 'eq lt ne' AND (b lt 3 or c gt '123' or not (a/b in [test,test2]))");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("((a == 'eq lt ne') && ((b < 3) || ((c > '123') || ! (a/b IN [test,test2]))))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_complex() throws ParseException, IllegalAccessException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        Operation operation = operationParser.parse("not (not (a ne 'eq lt ne' AND (b lt 3 or c gt '123')) or startswith(field,'prefix') and not a/b in [test,test2])");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("! (! ((a != 'eq lt ne') && ((b < 3) || (c > '123'))) || ((field startsWith 'prefix') && (! a/b IN [test,test2])))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_with_functions() throws ParseException, IllegalAccessException {
        OperationParser operationParser = new OperationParser(Dialects.ODATA);
        Operation operation = operationParser.parse("a ne (z div 2) or (endswith(x, abc add 5 sub (2 mul 3))) ne (y gt 5) AND (b sub a gt 0) or 3 mod 5");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("((a != (z / 2)) || ((((x endsWith (abc + (5 - (2 * 3)))) != (y > 5)) && ((b - a) > 0)) || (3 % 5)))"));
    }

}
