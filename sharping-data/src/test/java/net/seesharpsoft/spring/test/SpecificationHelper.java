package net.seesharpsoft.spring.test;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.lang.reflect.Field;

public class SpecificationHelper {

    private static Field SPECIFICATIONS_FIELD_SPEC;

    private static Class COMPOSED_SPECIFICATION;
    private static Field COMPOSED_SPECIFICATION_FIELD_LHS;
    private static Field COMPOSED_SPECIFICATION_FIELD_RHS;
    private static Field COMPOSED_SPECIFICATION_FIELD_TYPE;

    private static Class NEGATED_SPECIFICATION;
    private static Field NEGATED_SPECIFICATION_FIELD_SPEC;

    static {
        try {
            SPECIFICATIONS_FIELD_SPEC = Specifications.class.getDeclaredField("spec");
            SPECIFICATIONS_FIELD_SPEC.setAccessible(true);
            for (Class clazz : Specifications.class.getDeclaredClasses()) {
                if (clazz.getSimpleName().contains("ComposedSpecification")) {
                    COMPOSED_SPECIFICATION = clazz;
                }
                if (clazz.getSimpleName().contains("NegatedSpecification")) {
                    NEGATED_SPECIFICATION = clazz;
                }
            }
            COMPOSED_SPECIFICATION_FIELD_LHS = COMPOSED_SPECIFICATION.getDeclaredField("lhs");
            COMPOSED_SPECIFICATION_FIELD_LHS.setAccessible(true);
            COMPOSED_SPECIFICATION_FIELD_RHS = COMPOSED_SPECIFICATION.getDeclaredField("rhs");
            COMPOSED_SPECIFICATION_FIELD_RHS.setAccessible(true);
            COMPOSED_SPECIFICATION_FIELD_TYPE = COMPOSED_SPECIFICATION.getDeclaredField("compositionType");
            COMPOSED_SPECIFICATION_FIELD_TYPE.setAccessible(true);
            NEGATED_SPECIFICATION_FIELD_SPEC = NEGATED_SPECIFICATION.getDeclaredField("spec");
            NEGATED_SPECIFICATION_FIELD_SPEC.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private SpecificationHelper() {
        // static
    }

    public static String stringify(Specification specification) throws IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder();
        while (specification != null) {
            if (specification instanceof Specifications) {
                specification = (Specification) SPECIFICATIONS_FIELD_SPEC.get(specification);
            } else if (COMPOSED_SPECIFICATION.isAssignableFrom(specification.getClass())) {
                stringBuilder
                        .append("(")
                        .append(stringify((Specification)COMPOSED_SPECIFICATION_FIELD_LHS.get(specification)))
                        .append(" ")
                        .append(COMPOSED_SPECIFICATION_FIELD_TYPE.get(specification))
                        .append(" ")
                        .append(stringify((Specification)COMPOSED_SPECIFICATION_FIELD_RHS.get(specification)))
                        .append(")");
                break;
            } else if (NEGATED_SPECIFICATION.isAssignableFrom(specification.getClass())) {
                stringBuilder
                        .append("NOT (")
                        .append(stringify((Specification)NEGATED_SPECIFICATION_FIELD_SPEC.get(specification)))
                        .append(")");
                break;
            } else {
                stringBuilder
                        .append(specification);
                break;
            }
        }
        return stringBuilder.toString();
    }

}
