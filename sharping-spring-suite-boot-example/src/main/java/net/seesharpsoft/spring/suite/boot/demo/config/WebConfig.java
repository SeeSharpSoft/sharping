package net.seesharpsoft.spring.suite.boot.demo.config;

import net.seesharpsoft.spring.data.web.SpecificationHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SpecificationHandlerMethodArgumentResolver customArgumentResolver; // Inject your resolver

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(customArgumentResolver);
    }
}
