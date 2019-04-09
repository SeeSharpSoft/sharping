package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

public class SelectableRepositoryRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    protected final Environment environment;

    public SelectableRepositoryRegistryPostProcessor(Environment environment) {
        Assert.notNull(environment, "environment must not be null!");
        this.environment = environment;
    }

    protected Class<? extends  SelectableRepository> getImplementationClass() {
        try {
            return (Class<? extends SelectableRepository>)Class.forName(environment.getProperty(ConfigurationProperties.SELECTABLE_IMPL_CLASS, SelectableRepositoryImpl.class.getName()));
        } catch (ClassNotFoundException exc) {
            throw new RuntimeException(exc);
        }
    }

    protected String[] getBasePackages() {
        String basePackages = environment.getProperty(ConfigurationProperties.SELECTABLE_BASE_PACKAGES);
        if (basePackages == null) {
            return new String[0];
        }
        return basePackages.split(";");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        SelectableInterfaceScanRegistrar.registerSelectableRepositoryDefinitions(beanDefinitionRegistry, environment, getImplementationClass(), getBasePackages());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
