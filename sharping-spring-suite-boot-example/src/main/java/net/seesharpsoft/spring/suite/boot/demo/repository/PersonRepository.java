package net.seesharpsoft.spring.suite.boot.demo.repository;

import net.seesharpsoft.spring.suite.boot.demo.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Integer> {
}
