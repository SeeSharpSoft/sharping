package net.seesharpsoft.spring.multipart.boot.demo.controller;

import net.seesharpsoft.spring.multipart.boot.demo.entity.Person;
import net.seesharpsoft.spring.multipart.boot.demo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class ProtectedController {

    @Autowired
    private PersonRepository repository;

    @RequestMapping(value = "/secured", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String secured() {
        repository.saveAndFlush(new Person());
        return "SECURE! There are " + repository.count() + " persons in the database!";
    }

    @RequestMapping(value = "/add", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('ROLE_BASIC')")
    public String add() {
        repository.saveAndFlush(new Person());
        return "Add! There are " + repository.count() + " persons in the database!";
    }

}
