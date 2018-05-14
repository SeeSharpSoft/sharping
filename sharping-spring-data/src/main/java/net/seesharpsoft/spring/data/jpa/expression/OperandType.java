package net.seesharpsoft.spring.data.jpa.expression;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public enum OperandType {
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

    OperandType(String pattern, Class javaType, Converter<String, String> converter) {
        this.pattern = Pattern.compile("(?i)^" + pattern + "$");
        this.javaType = javaType;
        this.converter = converter;
    }

    public static OperandType parse(String input) {
        for (OperandType type : OperandType.values()) {
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