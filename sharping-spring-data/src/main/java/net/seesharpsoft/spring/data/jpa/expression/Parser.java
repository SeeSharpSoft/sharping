package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.UnhandledSwitchCaseException;
import net.seesharpsoft.commons.util.Lexer;
import net.seesharpsoft.commons.util.Tokenizer;
import net.seesharpsoft.spring.data.jpa.expression.Dialect.Token;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static net.seesharpsoft.spring.data.jpa.expression.Dialect.Token.*;

public class Parser {

    enum Primitive {
        NULL("null", void.class, source -> null),
        BOOLEAN("true|false", boolean.class, null),
        LONG("[-+]?[0-9]+L", Long.class, source -> source.substring(0, source.length() - 1)),
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
    private Lexer<Token> lexer;

    private static Tokenizer<Token> createTokenizer(Dialect dialect) {
        Tokenizer<Token> tokenizer = new Tokenizer();
        tokenizer.setCaseSensitive(dialect.isCaseSensitive());
        tokenizer.setTrimPattern("\n| ");
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

        expectOperand.addNextState(expectOperand, BRACKET_OPEN, UNARY_OPERATOR, UNARY_OPERATOR_METHOD, BINARY_OPERATOR_METHOD, TERTIARY_OPERATOR_METHOD, METHOD_PARAMETER_SEPARATOR);
        expectOperand.addNextState(expectOperator, OPERAND, NULL);
        expectOperator.addNextState(expectOperator, BRACKET_CLOSE);
        expectOperator.addNextState(expectOperand, BINARY_OPERATOR, METHOD_PARAMETER_SEPARATOR);

        return lexer;
    }

    public Parser(Dialect dialect, ConversionService conversionService) {
        this.dialect = dialect;
        this.setConversionService(conversionService);
    }

    public Parser(Dialect dialect) {
        this(dialect, DefaultConversionService.getSharedInstance());
    }

    protected Lexer<Token> getLexer() {
        if (this.lexer == null) {
            this.lexer = createLexer(createTokenizer(dialect));
        }
        return this.lexer;
    }

    public <T extends Operand> T parseExpression(String expression) throws ParseException {
        List<Tokenizer.TokenInfo<Token>> rpn = toRPN(tokenize(expression));
        return (T) evaluateRPN(rpn);
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
        return getLexer().tokenize(input);
    }

    private Operand getOperand(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token()) {
            case OPERAND:
                return parseValue(tokenInfo.text());
            case NULL:
                return null;
            default:
                throw new UnhandledSwitchCaseException(tokenInfo.token());
        }
    }

    private static boolean isOperator(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token()) {
            case UNARY_OPERATOR:
            case BINARY_OPERATOR:
            case UNARY_OPERATOR_METHOD:
            case BINARY_OPERATOR_METHOD:
            case TERTIARY_OPERATOR_METHOD:
                return true;
            default:
                return false;
        }
    }

    private static boolean isMethod(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token()) {
            case UNARY_OPERATOR_METHOD:
            case BINARY_OPERATOR_METHOD:
            case TERTIARY_OPERATOR_METHOD:
                return true;
            default:
                return false;
        }
    }

    private Operator getOperator(Tokenizer.TokenInfo<Token> tokenInfo) {
        Assert.isTrue(isOperator(tokenInfo), "operator tokenInfo required!");
        return dialect.getOperator(tokenInfo.text());
    }

    private List<Tokenizer.TokenInfo<Token>> toRPN(List<Tokenizer.TokenInfo<Token>> inputTokenInfos) throws ParseException {
        Queue<Tokenizer.TokenInfo<Token>> in = new LinkedList(inputTokenInfos);
        List<Tokenizer.TokenInfo<Token>> out = new LinkedList();
        Deque<Tokenizer.TokenInfo> stack = new LinkedList();

        while (!in.isEmpty()) {
            Tokenizer.TokenInfo<Token> tokenInfo = in.poll();
            switch (tokenInfo.token()) {
                case UNARY_OPERATOR:
                case BINARY_OPERATOR:
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
                    while (!stack.isEmpty() && stack.peek().token() != BRACKET_OPEN) {
                        out.add(stack.pop());
                    }
                    stack.pop();
                    break;
                case UNARY_OPERATOR_METHOD:
                case BINARY_OPERATOR_METHOD:
                case TERTIARY_OPERATOR_METHOD:
                    Assert.isTrue(in.peek().token() == BRACKET_OPEN, "opening bracket after method name expected!");
                    stack.push(in.poll());
                    stack.push(tokenInfo);
                    break;
                case METHOD_PARAMETER_SEPARATOR:
                    while (!stack.isEmpty() && !isMethod(stack.peek())) {
                        out.add(stack.pop());
                    }
                    break;
                case OPERAND:
                case NULL:
                    out.add(tokenInfo);
                    break;
                default:
                    throw new UnhandledSwitchCaseException(tokenInfo.token());
            }
        }
        while (!stack.isEmpty()) {
            out.add(stack.pop());
        }

        return out;
    }

    private Operand evaluateRPN(List<Tokenizer.TokenInfo<Token>> tokenInfos) {
        Deque<Operand> operands = new LinkedList<>();

        for (Tokenizer.TokenInfo tokenInfo : tokenInfos) {
            if (isOperator(tokenInfo)) {
                Operator operator = getOperator(tokenInfo);
                Operation operation = null;
                Operand right = operands.pop();
                switch (operator.getNAry()) {
                    case UNARY:
                        operation = new Operations.Unary(operator, right);
                        break;
                    case BINARY:
                        Operand left = operands.pop();
                        operation = new Operations.Binary(operator, left, right);
                        break;
                    case TERTIARY:
                        Operand second = operands.pop();
                        Operand first = operands.pop();
                        operation = new Operations.Tertiary(operator, first, second, right);
                        break;
                    default:
                        throw new UnhandledSwitchCaseException(operator.getNAry());
                }
                operands.push(operation);
            } else {
                operands.push(getOperand(tokenInfo));
            }
        }

        return operands.pop();
    }
}
