package net.seesharpsoft.spring.data.jpa;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.util.Assert;

import java.util.Arrays;

public class MultiInput2SpecificationConverter<T> implements Converter<T[], Specification> {

    private Converter<T, Specification> singleConverter;

    public MultiInput2SpecificationConverter(Converter<T, Specification> singleConverter) {
        Assert.notNull(singleConverter, "single converter must be provided!");
        this.singleConverter = singleConverter;
    }

    @Override
    public Specification convert(T[] filters) {
        return filters == null ?
                StaticSpecification.TRUE :
                Arrays.stream(filters)
                        .map(filter -> singleConverter.convert(filter))
                        .reduce((specification1, specification2) -> Specifications.where(specification1).and(specification2))
                        .orElse(StaticSpecification.TRUE);
    }
}
