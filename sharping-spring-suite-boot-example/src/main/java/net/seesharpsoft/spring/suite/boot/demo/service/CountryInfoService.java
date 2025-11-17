package net.seesharpsoft.spring.suite.boot.demo.service;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.suite.boot.demo.dto.CountryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryInfoService {

    @Autowired
    private SelectableRepository<CountryInfo> repository;

    public List<CountryInfo> getCountryInfos(Specification<CountryInfo> specification) {
        return repository.findAll(specification);
    }
}
