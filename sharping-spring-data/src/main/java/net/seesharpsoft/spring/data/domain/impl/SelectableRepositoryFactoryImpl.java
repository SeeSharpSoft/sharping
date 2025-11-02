package net.seesharpsoft.spring.data.domain.impl;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.SelectableRepositoryFactory;
import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.jpa.JpaVendorUtilProxy;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.ServiceLoader;

public class SelectableRepositoryFactoryImpl implements SelectableRepositoryFactory {

    private static ServiceLoader<JpaVendorUtilProxy> jpaVendorUtilProxyServiceLoader = ServiceLoader.load(JpaVendorUtilProxy.class);

    protected final JpaVendorUtilProxy jpaVendorUtilProxy;

    protected final EntityManager entityManager;

    protected final SqlParser sqlParser;

    public SelectableRepositoryFactoryImpl(EntityManager entityManager, SqlParser sqlParser) {
        this.jpaVendorUtilProxy = getJpaVendorUtilProxy();
        this.entityManager = entityManager;
        this.sqlParser = sqlParser;
    }

    protected JpaVendorUtilProxy getJpaVendorUtilProxy() {
        Iterator<JpaVendorUtilProxy> jpaVendorUtilProxyIterator = jpaVendorUtilProxyServiceLoader.iterator();
        Assert.state(jpaVendorUtilProxyIterator.hasNext(), "no JpaVendorUtilProxy found!");
        return jpaVendorUtilProxyIterator.next();
    }

    @Override
    public <T> SelectableRepository<T> createRepository(Class<? extends SelectableRepository> implClass, Class<T> selectableClass) {
        try {
            Constructor<? extends SelectableRepository> selectableRepositoryConstructor = implClass.getConstructor(JpaVendorUtilProxy.class, EntityManager.class, SqlParser.class, Class.class);
            return selectableRepositoryConstructor.newInstance(jpaVendorUtilProxy, entityManager, sqlParser, selectableClass);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public <T> SelectableRepository<T> createRepository(Class<T> selectableClass) {
        return createRepository(SelectableRepositoryImpl.class, selectableClass);
    }
}
