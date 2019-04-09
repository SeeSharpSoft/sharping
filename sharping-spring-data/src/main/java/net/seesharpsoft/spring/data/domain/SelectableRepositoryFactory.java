package net.seesharpsoft.spring.data.domain;

public interface SelectableRepositoryFactory {

    <T> SelectableRepository<T> createRepository(Class<? extends SelectableRepository> implClass, Class<T> selectableClass);

    <T> SelectableRepository<T> createRepository(Class<T> selectableClass);
}
