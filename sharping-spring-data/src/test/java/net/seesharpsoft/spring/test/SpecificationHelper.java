package net.seesharpsoft.spring.test;

import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;

public class SpecificationHelper {

    private static Class<?> COMPOSED_SPECIFICATION;
    private static Field COMPOSED_SPECIFICATION_FIELD_LHS;
    private static Field COMPOSED_SPECIFICATION_FIELD_RHS;
    private static Field COMPOSED_SPECIFICATION_FIELD_COMBINER;

    private static Class<?> NEGATED_SPECIFICATION;
    private static Field NEGATED_SPECIFICATION_FIELD_SPEC;

    static {
        try {
            // Find inner classes of Specification interface
            for (Class<?> clazz : Specification.class.getDeclaredClasses()) {
                if (clazz.getSimpleName().contains("Composed")) {
                    COMPOSED_SPECIFICATION = clazz;
                }
                if (clazz.getSimpleName().contains("Negated")) {
                    NEGATED_SPECIFICATION = clazz;
                }
            }

            if (COMPOSED_SPECIFICATION != null) {
                COMPOSED_SPECIFICATION_FIELD_LHS = COMPOSED_SPECIFICATION.getDeclaredField("lhs");
                COMPOSED_SPECIFICATION_FIELD_LHS.setAccessible(true);
                COMPOSED_SPECIFICATION_FIELD_RHS = COMPOSED_SPECIFICATION.getDeclaredField("rhs");
                COMPOSED_SPECIFICATION_FIELD_RHS.setAccessible(true);
                COMPOSED_SPECIFICATION_FIELD_COMBINER = COMPOSED_SPECIFICATION.getDeclaredField("combiner");
                COMPOSED_SPECIFICATION_FIELD_COMBINER.setAccessible(true);
            }

            if (NEGATED_SPECIFICATION != null) {
                NEGATED_SPECIFICATION_FIELD_SPEC = NEGATED_SPECIFICATION.getDeclaredField("spec");
                NEGATED_SPECIFICATION_FIELD_SPEC.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to initialize SpecificationHelper", e);
        }
    }

    private SpecificationHelper() {
        // static utility class
    }

    public static String stringify(Specification<?> specification) throws IllegalAccessException {
        if (specification == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (COMPOSED_SPECIFICATION != null && COMPOSED_SPECIFICATION.isAssignableFrom(specification.getClass())) {
            Specification<?> lhs = (Specification<?>) COMPOSED_SPECIFICATION_FIELD_LHS.get(specification);
            Specification<?> rhs = (Specification<?>) COMPOSED_SPECIFICATION_FIELD_RHS.get(specification);
            Object combiner = COMPOSED_SPECIFICATION_FIELD_COMBINER.get(specification);

            // The combiner is a Combiner enum with values AND/OR
            String combinerType = combiner.toString();

            stringBuilder
                    .append("(")
                    .append(stringify(lhs))
                    .append(" ")
                    .append(combinerType)
                    .append(" ")
                    .append(stringify(rhs))
                    .append(")");
        } else if (NEGATED_SPECIFICATION != null && NEGATED_SPECIFICATION.isAssignableFrom(specification.getClass())) {
            Specification<?> spec = (Specification<?>) NEGATED_SPECIFICATION_FIELD_SPEC.get(specification);

            stringBuilder
                    .append("NOT (")
                    .append(stringify(spec))
                    .append(")");
        } else {
            stringBuilder.append(specification.toString());
        }

        return stringBuilder.toString();
    }
}