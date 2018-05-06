package net.seesharpsoft.spring.data.jpa;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;

public class StringToStringSpecificationDummyConverter implements Converter<String, Specification> {
    @Override
    public Specification convert(String s) {
        return new StringSpecificationDummy(s);
    }
}