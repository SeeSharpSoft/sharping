package net.seesharpsoft.spring.data.domain.impl;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.SelectableRepositoryFactory;
import net.seesharpsoft.spring.data.domain.SqlParser;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SelectableRepositoryFactoryImpl implements SelectableRepositoryFactory {

    protected final EntityManager entityManager;

    protected final SqlParser sqlParser;

    public SelectableRepositoryFactoryImpl(EntityManager entityManager, SqlParser sqlParser) {
        this.entityManager = entityManager;
        this.sqlParser = sqlParser;
    }

    @Override
    public <T> SelectableRepository<T> createRepository(Class<? extends SelectableRepository> implClass, Class<T> selectableClass) {
        try {
            Constructor<? extends SelectableRepository> selectableRepositoryConstructor = implClass.getConstructor(EntityManager.class, SqlParser.class, Class.class);
            return selectableRepositoryConstructor.newInstance(entityManager, sqlParser, selectableClass);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exc) {
            throw new RuntimeException(exc);
        }
    }

    @Override
    public <T> SelectableRepository<T> createRepository(Class<T> selectableClass) {
        return createRepository(SelectableRepositoryImpl.class, selectableClass);
    }
}
