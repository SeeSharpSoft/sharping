package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryImpl;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.Map;

import static org.apache.naming.ResourceRef.SINGLETON;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.DEPENDENCY_CHECK_ALL;

public class SelectableInterfaceScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    public static final String SELECTABLE_FACTORY_BEAN_NAME = "selectableRepositoryFactory";
    public static final String SELECTABLE_FACTORY_CREATE_METHOD_NAME = "createRepository";

    public static void registerSelectableRepositoryDefinitions(BeanDefinitionRegistry beanDefinitionRegistry,
                                                                     Environment environment,
                                                                     Class<? extends SelectableRepository> repositoryBaseClass,
                                                                     String[] basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            return;
        }

        if (repositoryBaseClass == null) {
            repositoryBaseClass = SelectableRepositoryImpl.class;
        }

        // using these packages, scan for interface annotated with Selectable
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false, environment) {
            // override isCandidateComponent to only scan for concrete classes
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && metadata.isConcrete();
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(Selectable.class));

        // scan all packages
        for (String basePackage : basePackages) {
            for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                Class selectableClass = null;
                try {
                    selectableClass = Class.forName(annotatedBeanDefinition.getBeanClassName());
                } catch (ClassNotFoundException exc) {
                    throw new RuntimeException(exc);
                }

                RootBeanDefinition finalBeanDefinition = new RootBeanDefinition();
                finalBeanDefinition.setDependencyCheck(DEPENDENCY_CHECK_ALL);
                finalBeanDefinition.setScope(SINGLETON);
                finalBeanDefinition.setTargetType(ResolvableType.forClassWithGenerics(SelectableRepository.class, selectableClass));
                finalBeanDefinition.setAutowireCandidate(true);
                finalBeanDefinition.setFactoryBeanName(SELECTABLE_FACTORY_BEAN_NAME);
                finalBeanDefinition.setFactoryMethodName(SELECTABLE_FACTORY_CREATE_METHOD_NAME);
                ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
                constructorArgumentValues.addGenericArgumentValue(repositoryBaseClass);
                constructorArgumentValues.addGenericArgumentValue(selectableClass);
                finalBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

                beanDefinitionRegistry.registerBeanDefinition(String.format("%sRepository", StringUtils.uncapitalize(selectableClass.getSimpleName())), finalBeanDefinition);
            }
        }
    }

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableSelectableRepositories.class.getCanonicalName());

        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("basePackages");
            Class<? extends SelectableRepository> repositoryBaseClass = (Class) annotationAttributes.get("repositoryBaseClass");

            if (basePackages.length == 0) {
                // If value attribute is not set, fallback to the package of the annotated class
                basePackages = new String[] {((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName()};
            }

            registerSelectableRepositoryDefinitions(registry, environment, repositoryBaseClass, basePackages);
        }
    }
}
