package net.seesharpsoft.spring.suite.boot.demo.repository;

import net.seesharpsoft.spring.suite.boot.demo.model.Team;
import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<Team, Integer> {
}
