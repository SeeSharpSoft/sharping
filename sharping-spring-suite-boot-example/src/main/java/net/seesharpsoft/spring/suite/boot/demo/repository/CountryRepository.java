package net.seesharpsoft.spring.suite.boot.demo.repository;

import net.seesharpsoft.spring.suite.boot.demo.model.Country;
import org.springframework.data.repository.CrudRepository;

public interface CountryRepository extends CrudRepository<Country, Integer> {
}
