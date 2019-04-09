package net.seesharpsoft.spring.data.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SelectableRepository<T> extends JpaSpecificationExecutor<T> {
}
