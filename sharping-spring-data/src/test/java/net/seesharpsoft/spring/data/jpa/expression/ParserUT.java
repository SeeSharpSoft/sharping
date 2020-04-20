package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.commons.util.Tokenizer;
import net.seesharpsoft.spring.data.jpa.expression.Dialect.Token;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParserUT {

    @Test
    public void odata_tokenize_should_return_correct_token_list_very_simple() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        String rootTokenText = "a eq 1";
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize(rootTokenText);

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 0, 1)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 2, 4)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText,  5, 6))
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_simple() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        String rootTokenText ="a eq 'eq lt ne' AND b lt 3";
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize(rootTokenText);

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 0, 1)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 2, 4)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 5, 15)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 16, 19)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 20, 21)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 22, 24)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 25, 26))
        ));
    }

    @Test
    public void odata_tokenize_should_return_correct_token_list_normal() throws ParseException {
        Parser parser = new Parser(Dialects.ODATA);
        String rootTokenText = "a eq 'eq lt ne' AND (b lt 3 or c gt '123' or not (a/b in [test,test2]))";
        List<Tokenizer.TokenInfo<Token>> tokenInfos = parser.tokenize(rootTokenText);

        assertThat(tokenInfos, contains(
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 0, 1)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 2, 4)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 5, 15)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 16, 19)),
                new Tokenizer.TokenInfo(Token.BRACKET_OPEN, new Tokenizer.CharRange(rootTokenText, 20, 21)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 21, 22)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 23, 25)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 26, 27)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 28, 30)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText,  31, 32)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 33, 35)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 36, 41)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 42, 44)),
                new Tokenizer.TokenInfo(Token.UNARY_OPERATOR, new Tokenizer.CharRange(rootTokenText,  45, 48)),
                new Tokenizer.TokenInfo(Token.BRACKET_OPEN, new Tokenizer.CharRange(rootTokenText,49, 50)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 50, 53)),
                new Tokenizer.TokenInfo(Token.BINARY_OPERATOR, new Tokenizer.CharRange(rootTokenText, 54, 56)),
                new Tokenizer.TokenInfo(Token.OPERAND, new Tokenizer.CharRange(rootTokenText, 57, 69)),
                new Tokenizer.TokenInfo(Token.BRACKET_CLOSE, new Tokenizer.CharRange(rootTokenText, 69, 70)),
                new Tokenizer.TokenInfo(Token.BRACKET_CLOSE, new Tokenizer.CharRange(rootTokenText, 70, 71))
        ));
    }

    @Test
    public void odata_parse_should_return_correct_specification_normal() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operand operation = parser.parseExpression("a eq 'eq lt ne' AND (/b. lt 3 or c gt '123' or not (a/b in [test,test2]))");

        assertThat(operation, instanceOf(Operation.class));
        assertThat(operation.toString(), is("(({a} == 'eq lt ne') && (({b} < 3) || (({c} > '123') || ! ({a/b} IN {[test,test2]}))))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_complex() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operand operation = parser.parseExpression("not (not (a ne 'eq lt ne' AND (b lt 3 or c gt '123')) or startswith(field,'prefix') and not a/b in [test,test2])");

        assertThat(operation, instanceOf(Operation.class));
        assertThat(operation.toString(), is("! (! (({a} != 'eq lt ne') && (({b} < 3) || ({c} > '123'))) || (({field} startsWith 'prefix') && (! {a/b} IN {[test,test2]})))"));
    }

    @Test
    public void odata_parse_should_return_correct_specification_with_functions() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.ODATA);
        Operand operation = parser.parseExpression("a ne (z div 2) or (endswith(x, abc add 5 sub (2 mul 3))) ne (y gt 5) AND (b sub a gt 0) or 3 mod 5");

        assertThat(operation, instanceOf(Operation.class));
        assertThat(operation.toString(), is("(({a} != ({z} / 2)) || (((({x} endsWith ({abc} + (5 - (2 * 3)))) != ({y} > 5)) && (({b} - {a}) > 0)) || (3 % 5)))"));
    }

    @Test
    public void parser_should_return_correct_specification_with_if_else_case() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.SHARP);
        Operand operation = parser.parseExpression("if(a != (z / 2), abc + 5 - (2 * 3), (b - a) * (3 % 5))");

        assertThat(operation, instanceOf(Operation.class));
        assertThat(operation.toString(), is("if(({a} != ({z} / 2)),({abc} + (5 - (2 * 3))),(({b} - {a}) * (3 % 5)))"));
    }

    @Test
    public void parser_should_return_correct_specification_with_as_operator() throws ParseException, IllegalAccessException {
        Parser parser = new Parser(Dialects.SHARP);
        Operand operation = parser.parseExpression("count(a) as countA");

        assertThat(operation, instanceOf(Operation.class));
        assertThat(operation.toString(), is("(count {a} as {countA})"));
    }

}
