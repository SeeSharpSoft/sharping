package net.seesharpsoft.spring.data.jpa;

import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.OperationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class ODataExpression2SpecificationConverter implements Converter<String, Specification> {

    private OperationParser operationParser;

    @Autowired
    public ODataExpression2SpecificationConverter(ConversionService conversionService) {
        this.operationParser = new OperationParser(Dialects.ODATA, conversionService);
    }

    protected ODataExpression2SpecificationConverter() {
        this(DefaultConversionService.getSharedInstance());
    }

    @Override
    public Specification convert(String input) {
        try {
            return input == null || input.isEmpty() ? StaticSpecification.TRUE : new OperationSpecification(operationParser.parse(input));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
