package net.seesharpsoft.spring.suite.boot.demo.controller;

import net.seesharpsoft.spring.suite.boot.demo.dto.CountryInfo;
import net.seesharpsoft.spring.suite.boot.demo.service.CountryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CountryController {

    @Autowired
    private CountryInfoService countryInfoService;

    @RequestMapping("/countryInfos")
    public List<CountryInfo> getCountryInfos(Specification<CountryInfo> specification) {
        return countryInfoService.getCountryInfos(specification);
    }

}
