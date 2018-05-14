package net.seesharpsoft.spring.multipart.boot.demo.repository;

import net.seesharpsoft.spring.multipart.boot.demo.entity.Person;
import net.seesharpsoft.spring.multipart.boot.demo.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByFirstNameLike(String firstName);

    Person findByFirstNameEquals(String username);
}