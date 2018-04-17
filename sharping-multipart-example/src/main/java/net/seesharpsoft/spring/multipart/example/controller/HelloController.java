package net.seesharpsoft.spring.multipart.example.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
