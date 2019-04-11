package net.seesharpsoft.spring.data.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface SelectableRepository<T> extends JpaSpecificationExecutor<T> {
    default List<T> findAll() {
        return findAll((Specification)null);
    }

    default Page<T> findAll(Pageable pageable) {
        return findAll(null, pageable);
    }

    default List<T> findAll(Sort sort) {
        return findAll(null, sort);
    }
}
