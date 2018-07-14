package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.commons.util.Tokenizer;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import net.seesharpsoft.spring.data.jpa.expression.Dialect.Token;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParserUT {

    @Test
    public void odata_tokenize_should_return_correct_token_list_very_simple() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize("a eq 1");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(Token.OPERAND, "1")
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_simple() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize("a eq 'eq lt ne' AND b lt 3");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(Token.OPERAND, "'eq lt ne'"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "AND"),
                new Tokenizer.TokenInfo(Token.OPERAND, "b"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "lt"),
                new Tokenizer.TokenInfo(Token.OPERAND, "3")
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_normal() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize("a eq 'eq lt ne' AND (b lt 3 or c gt '123' or not (a/b in [test,test2]))");

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, "a"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "eq"),
                new Tokenizer.TokenInfo(Token.OPERAND, "'eq lt ne'"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "AND"),
                new Tokenizer.TokenInfo(Token.BRACKET_OPEN, "("),
                new Tokenizer.TokenInfo(Token.OPERAND, "b"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "lt"),
                new Tokenizer.TokenInfo(Token.OPERAND, "3"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "or"),
                new Tokenizer.TokenInfo(Token.OPERAND, "c"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "gt"),
                new Tokenizer.TokenInfo(Token.OPERAND, "'123'"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "or"),
                new Tokenizer.TokenInfo(Token.UNARY_OPERATOR, "not"),
                new Tokenizer.TokenInfo(Token.BRACKET_OPEN, "("),
                new Tokenizer.TokenInfo(Token.OPERAND, "a/b"),
                new Tokenizer.TokenInfo(Token.OPERATOR, "in"),
                new Tokenizer.TokenInfo(Token.OPERAND, "[test,test2]"),
                new Tokenizer.TokenInfo(Token.BRACKET_CLOSE, ")"),
                new Tokenizer.TokenInfo(Token.BRACKET_CLOSE, ")")
        ));
    }

    @Test
    public void odata_parse_should_return_correct_specification_normal() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operation operation = parser.parseExpression("a eq 'eq lt ne' AND (/b. lt 3 or c gt '123' or not (a/b in [test,test2]))");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("(({a} == 'eq lt ne') && (({b} < 3) || (({c} > '123') || ! ({a/b} IN {[test,test2]}))))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_complex() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operation operation = parser.parseExpression("not (not (a ne 'eq lt ne' AND (b lt 3 or c gt '123')) or startswith(field,'prefix') and not a/b in [test,test2])");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("! (! (({a} != 'eq lt ne') && (({b} < 3) || ({c} > '123'))) || (({field} startsWith 'prefix') && (! {a/b} IN {[test,test2]})))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_with_functions() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operation operation = parser.parseExpression("a ne (z div 2) or (endswith(x, abc add 5 sub (2 mul 3))) ne (y gt 5) AND (b sub a gt 0) or 3 mod 5");

        assertThat(operation, notNullValue());
        assertThat(operation.toString(), is("(({a} != ({z} / 2)) || (((({x} endsWith ({abc} + (5 - (2 * 3)))) != ({y} > 5)) && (({b} - {a}) > 0)) || (3 % 5)))"));
    }

}
