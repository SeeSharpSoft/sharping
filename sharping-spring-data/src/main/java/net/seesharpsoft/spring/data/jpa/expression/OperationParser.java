package net.seesharpsoft.spring.data.jpa.expression;

import net.seesharpsoft.UnhandledSwitchCaseException;
import net.seesharpsoft.commons.util.Lexer;
import net.seesharpsoft.commons.util.Tokenizer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.seesharpsoft.spring.data.jpa.expression.OperationParser.Token.*;

public class OperationParser {

    public enum Token {
        OPERAND,
        OPERATOR,
        METHOD,
        METHOD_PARAMETER_SEPARATOR,
        UNARY_OPERATOR,
        BRACKET_OPEN,
        BRACKET_CLOSE;
    }

    private final Dialect dialect;
    private final ConversionService conversionService;
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
        expectOperand.addNextState(expectOperator, OPERAND);
        expectOperator.addNextState(expectOperator, BRACKET_CLOSE);
        expectOperator.addNextState(expectOperand, OPERATOR, METHOD_PARAMETER_SEPARATOR);

        return lexer;
    }

    public OperationParser(Dialect dialect, ConversionService conversionService) {
        this.dialect = dialect;
        this.conversionService = conversionService;
        this.tokenizer = createTokenizer(dialect);
        this.lexer = createLexer(this.tokenizer);
    }

    public OperationParser(Dialect dialect) {
        this(dialect, DefaultConversionService.getSharedInstance());
    }

    public Operation parse(String oDataExpression) throws ParseException {
        List<Tokenizer.TokenInfo<Token>> rpn = toRPN(tokenize(oDataExpression));
        return evaluateRPN(rpn);
    }

    protected List<Tokenizer.TokenInfo<Token>> tokenize(String input) throws ParseException {
        return lexer.tokenize(input);
    }

    private Operand getOperand(Tokenizer.TokenInfo<Token> tokenInfo) {
        switch (tokenInfo.token) {
            case OPERAND:
                return dialect.parseOperand(tokenInfo.sequence, conversionService);
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
                    out.add(tokenInfo);
                    break;
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
                    operation = new BinaryOperation(left, operator, right);
                } else if (operator.getNAry() == Operator.NAry.UNARY) {
                    operation = new UnaryOperation(operator, right);
                }
                operands.push(new Operand(operation));
            } else {
                operands.push(getOperand(tokenInfo));
            }
        }

        Operand result = operands.pop();
        if (result.getType() != Operand.Type.OPERATION) {
            throw new RuntimeException("operation expected!");
        }

        return result.getValue();
    }
}
