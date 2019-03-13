package net.seesharpsoft.spring.data.domain.impl;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SelectableRepositoryImpl implements SelectableRepository {

    private final Class<?> selectableClass;

    public SelectableRepositoryImpl(Class selectableClass) {
        this.selectableClass = selectableClass;
    }

    @Override
    public Object findOne(Specification spec) {
        return null;
    }

    @Override
    public List findAll(Specification spec) {
        return null;
    }

    @Override
    public Page findAll(Specification spec, Pageable pageable) {
        return null;
    }

    @Override
    public List findAll(Specification spec, Sort sort) {
        return null;
    }

    @Override
    public long count(Specification spec) {
        return 0;
    }
}
