package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.UnhandledSwitchCaseException;
import net.seesharpsoft.commons.util.Lexer;
import net.seesharpsoft.commons.util.Tokenizer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import net.seesharpsoft.spring.data.jpa.expression.Dialect.Token;
import static net.seesharpsoft.spring.data.jpa.expression.Dialect.Token.*;

public class Parser {

    enum Primitive {
        NULL("null", void.class, source -> null),
        BOOLEAN("true|false", boolean.class, null),
        INTEGER("[-+]?[0-9]+", Integer.class, null),
        GUID("[0-9A-Fa-f]{8}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{12}", UUID.class, null),
        STRING("'.+?'", String.class, source -> source.substring(1, source.length() - 1)),
        DOUBLE("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?", double.class, null),
        DATE("[0-9]{4}[-][0-9]{2}[-][0-9]{2}", LocalDate.class, null),
        DATETIME("(datetime')?[0-9]{4}[-][0-9]{2}[-][0-9]{2}[T ][0-9]{2}[:][0-9]{2}[:][0-9]{2,4}(Z|[+-][0-9]{4})?'?", LocalDateTime.class, source -> source.startsWith("datetime'") ? source.substring("datetime'".length(), source.length() - 1) : source);

        final Pattern pattern;
        final Class javaType;
        final Converter<String, String> converter;

        Primitive(String pattern, Class javaType, Converter<String, String> converter) {
            this.pattern = Pattern.compile("(?i)^" + pattern + "$");
            this.javaType = javaType;
            this.converter = converter;
        }

        public static Primitive parse(String input) {
            for (Primitive type : Primitive.values()) {
                if (type.pattern.matcher(input).matches()) {
                    return type;
                }
            }
            return null;
        }

        public Class getJavaType() {
            return javaType;
        }

        public Object convert(ConversionService conversionService, String input) {
            String preparedInput = converter == null ? input : converter.convert(input);
            if (preparedInput == null) {
                return null;
            }
            return conversionService == null ? preparedInput : conversionService.convert(preparedInput, javaType);
        }
    }

    private ConversionService conversionService;
    private final Dialect dialect;
    private final Tokenizer<Token> tokenizer;
    private final Lexer<Token> lexer;

    private static Tokenizer<Token> createTokenizer(Dialect dialect) {
        Tokenizer<Token> tokenizer = new Tokenizer();
        tokenizer.setCaseSensitive(dialect.isCaseSensitive());
        for (Token token : Token.values()) {
            String pattern = dialect.getRegexPattern(token);
            if (pattern != null && !pattern.isEmpty()) {
                tokenizer.add(token, pattern);
            }
        }
        return tokenizer;
    }

    private static Lexer<Token> createLexer(Tokenizer<Token> tokenizer) {
        Lexer<Token> lexer = new Lexer(tokenizer);

        Lexer.State<Token> expectOperand = lexer.addState("operand");
        Lexer.State<Token> expectOperator = lexer.addState("operator");

        expectOperand.addNextState(expectOperand, BRACKET_OPEN, UNARY_OPERATOR, METHOD_PARAMETER_SEPARATOR, METHOD);
        expectOperand.addNextState(expectOperator, OPERAND, NULL);
        expectOperator.addNextState(expectOperator, BRACKET_CLOSE);
        expectOperator.addNextState(expectOperand, OPERATOR, METHOD_PARAMETER_SEPARATOR);

        return lexer;
    }

    public Parser(Dialect dialect, ConversionService conversionService) {
        this.dialect = dialect;
        this.setConversionService(conversionService);
        this.tokenizer = createTokenizer(dialect);
        this.lexer = createLexer(this.tokenizer);
    }

    public Parser(Dialect dialect) {
        this(dialect, DefaultConversionService.getSharedInstance());
    }

    public Operation parseExpression(String expression) throws ParseException {
        List<Tokenizer.TokenInfo<Token>> rpn = toRPN(tokenize(expression));
        return evaluateRPN(rpn);
    }

    public Operand parseValue(String value) {
        if (value == null) {
            return null;
        }
        Primitive type = Primitive.parse(value);
        if (type == null) {
            return new Operands.FieldReference(value);
        }
        if (type == Primitive.NULL) {
            return null;
        }
        return new Operands.Wrapper(type.convert(getConversionService(), value), conversionService);
    }

    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    protected List<Tokenizer.TokenInfo<Token>> tokenize(String input) throws ParseException {
        return lexer.tokenize(input);
    }

    private Operand getOperand(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token) {
            case OPERAND:
                return parseValue(tokenInfo.sequence);
            case NULL:
                return null;
            default:
                throw new UnhandledSwitchCaseException(tokenInfo.token);
        }
    }

    private static boolean isOperator(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token) {
            case UNARY_OPERATOR:
            case OPERATOR:
            case METHOD:
                return true;
            default:
                return false;
        }
    }

    private Operator getOperator(Tokenizer.TokenInfo<Token> tokenInfo) {
        Assert.isTrue(isOperator(tokenInfo), "operator tokenInfo required!");
        return dialect.getOperator(tokenInfo.sequence);
    }

    private List<Tokenizer.TokenInfo<Token>> toRPN(List<Tokenizer.TokenInfo<Token>> inputTokenInfos) throws ParseException {
        Queue<Tokenizer.TokenInfo<Token>> in = new LinkedList(inputTokenInfos);
        List<Tokenizer.TokenInfo<Token>> out = new LinkedList();
        Deque<Tokenizer.TokenInfo> stack = new LinkedList();

        while(!in.isEmpty()) {
            Tokenizer.TokenInfo<Token> tokenInfo = in.poll();
            switch (tokenInfo.token) {
                case UNARY_OPERATOR:
                case OPERATOR:
                    Operator operator = getOperator(tokenInfo);
                    while (!stack.isEmpty() && isOperator(stack.peek())) {
                        if (!operator.hasHigherPrecedenceThan(getOperator(stack.peek()))) {
                            out.add(stack.pop());
                            continue;
                        }
                        break;
                    }
                    stack.push(tokenInfo);
                    break;
                case BRACKET_OPEN:
                    stack.push(tokenInfo);
                    break;
                case BRACKET_CLOSE:
                    while (!stack.isEmpty() && stack.peek().token != BRACKET_OPEN) {
                        out.add(stack.pop());
                    }
                    stack.pop();
                    break;
                case METHOD:
                    Assert.isTrue(in.peek().token == BRACKET_OPEN, "opening bracket after method name expected!");
                    stack.push(in.poll());
                    stack.push(tokenInfo);
                    break;
                case METHOD_PARAMETER_SEPARATOR:
                    // ignore
                    break;
                case OPERAND:
                case NULL:
                    out.add(tokenInfo);
                    break;
                default:
                    throw new UnhandledSwitchCaseException(tokenInfo.token);
            }
        }
        while (!stack.isEmpty()) {
            out.add(stack.pop());
        }

        return out;
    }

    private Operation evaluateRPN(List<Tokenizer.TokenInfo<Token>> tokenInfos) {
        Deque<Operand> operands = new LinkedList<>();

        for (Tokenizer.TokenInfo tokenInfo : tokenInfos) {
            if (isOperator(tokenInfo)) {
                Operator operator = getOperator(tokenInfo);
                Operation operation = null;
                Operand right = operands.pop();
                if (operator.getNAry() == Operator.NAry.BINARY) {
                    Operand left = operands.pop();
                    operation = new Operations.Binary(operator, left, right);
                } else if (operator.getNAry() == Operator.NAry.UNARY) {
                    operation = new Operations.Unary(operator, right);
                }
                operands.push(operation);
            } else {
                operands.push(getOperand(tokenInfo));
            }
        }

        Operand result = operands.pop();
        if (!(result instanceof Operation)) {
            throw new RuntimeException("operation expected!");
        }
        return (Operation)result;
    }
}
