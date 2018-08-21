package net.seesharpsoft.spring.data.jpa;

import net.seesharpsoft.spring.data.jpa.expression.Dialect;
import net.seesharpsoft.spring.data.jpa.expression.Parser;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;

public class SpecificationConverter implements Converter<String, Specification> {

    private Parser parser;

    public SpecificationConverter(Dialect dialect, ConversionService conversionService) {
        this.parser = new Parser(dialect, conversionService);
    }
    
    @Override
    public Specification convert(String input) {
        try {
            return input == null || input.isEmpty() ? StaticSpecification.TRUE : new OperationSpecification(parser.parseExpression(input));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
