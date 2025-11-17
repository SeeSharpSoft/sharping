package net.seesharpsoft.spring.data.jpa;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.util.Arrays;

public class MultiInput2SpecificationConverter<T> implements Converter<T[], Specification<?>> {

    private Converter<T, Specification<?>> singleConverter;

    public MultiInput2SpecificationConverter(Converter<T, Specification<?>> singleConverter) {
        Assert.notNull(singleConverter, "single converter must be provided!");
        this.singleConverter = singleConverter;
    }

    @Override
    public Specification<?> convert(T[] filters) {
        return filters.length == 0 ?
                StaticSpecification.TRUE :
                Arrays.stream(filters)
                        .map(filter -> singleConverter.convert(filter))
                        .reduce((spec1, spec2) -> spec1.and(spec2))
                        .orElse(StaticSpecification.TRUE);
    }
}
