package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryImpl;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SelectableInterfaceScanRegistrar.class)
public @interface EnableSelectableRepositories {

    String[] basePackages() default {};

    Class<?> repositoryBaseClass() default SelectableRepositoryImpl.class;
}
