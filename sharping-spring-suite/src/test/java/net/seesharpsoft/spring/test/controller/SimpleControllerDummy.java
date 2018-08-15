package net.seesharpsoft.spring.test.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleControllerDummy {

    @RequestMapping(value = "/specification", produces = MediaType.TEXT_PLAIN_VALUE)
    public String specification(Specification specification) {
        return specification.toString();
    }

    @RequestMapping(value = "/specificationWithQualifier", produces = MediaType.TEXT_PLAIN_VALUE)
    public String specificationWithRequestParam(@Qualifier(value = "q") Specification specification) {
        return specification.toString();
    }

}
