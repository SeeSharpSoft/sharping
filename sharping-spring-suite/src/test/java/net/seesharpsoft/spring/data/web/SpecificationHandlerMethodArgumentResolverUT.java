package net.seesharpsoft.spring.data.web;

import net.seesharpsoft.spring.data.jpa.StringToStringSpecificationDummyConverter;

import net.seesharpsoft.spring.test.controller.SimpleControllerDummy;
import org.junit.Test;
import org.mockito.internal.matchers.StartsWith;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpecificationHandlerMethodArgumentResolverUT {

    private MockMvc mockMvc;

    private SpecificationHandlerMethodArgumentResolver argumentResolverMock;

    public SpecificationHandlerMethodArgumentResolver getDefaultArgumentResolver() {
        return new SpecificationHandlerMethodArgumentResolver(new StringToStringSpecificationDummyConverter());
    }

    public void setup() {
        setup(getDefaultArgumentResolver());
    }

    public void setup(SpecificationHandlerMethodArgumentResolver argumentResolver) {
        argumentResolverMock = spy(
                argumentResolver
        );

        mockMvc = MockMvcBuilders.standaloneSetup(new SimpleControllerDummy())
                .setCustomArgumentResolvers(argumentResolverMock)
                .build();
    }

    @Test
    public void handler_should_be_used_for_simple_specification_endpoint() throws Exception {
        setup();
        mockMvc.perform(get("/specification"))
                .andExpect(status().isOk());

        verify(argumentResolverMock).resolveArgument(any(), any(), any(), any());
    }

    @Test
    public void handler_should_resolve_empty_filter_parameter_as_empty_specification() throws Exception {
        setup();
        mockMvc.perform(get("/specification"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void handler_should_resolve_empty_filter_parameter_as_empty_specification_ignoring_other_parameters() throws Exception {
        setup();
        mockMvc.perform(get("/specification?q_filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void handler_should_resolve_filter_parameter_as_specification() throws Exception {
        setup();
        mockMvc.perform(get("/specification?filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_multiple_filter_parameter_as_specifications() throws Exception {
        setup();
        mockMvc.perform(get("/specification?filter=a eq 1&filter=b eq 2"))
                .andExpect(status().isOk())
                .andExpect(content().string(new StartsWith("org.springframework.data.jpa.domain.Specifications")));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_qualifier_as_specification() throws Exception {
        setup();
        mockMvc.perform(get("/specificationWithQualifier?q_filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_different_parameter_name_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setFilterParameterName("$myFilter");
        setup(resolver);
        mockMvc.perform(get("/specification?$myFilter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_different_parameter_name_and_with_qualifier_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setFilterParameterName("$myFilter");
        setup(resolver);
        mockMvc.perform(get("/specificationWithQualifier?q_$myFilter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_prefix_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setPrefix("@");
        setup(resolver);
        mockMvc.perform(get("/specification?@filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_prefix_and_qualifier_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setPrefix("@");
        setup(resolver);
        mockMvc.perform(get("/specificationWithQualifier?@q_filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_delimiter_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setQualifierDelimiter("-");
        setup(resolver);
        mockMvc.perform(get("/specificationWithQualifier?q-filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_prefix_and_delimiter_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setPrefix("@");
        resolver.setQualifierDelimiter("-");
        setup(resolver);
        mockMvc.perform(get("/specificationWithQualifier?@q-filter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }

    @Test
    public void handler_should_resolve_filter_parameter_with_prefix_and_delimiter_and_name_as_specification() throws Exception {
        SpecificationHandlerMethodArgumentResolver resolver = getDefaultArgumentResolver();
        resolver.setPrefix("@");
        resolver.setQualifierDelimiter("-");
        resolver.setFilterParameterName("$myFilter");
        setup(resolver);
        mockMvc.perform(get("/specificationWithQualifier?@q-$myFilter=a eq 1"))
                .andExpect(status().isOk())
                .andExpect(content().string("a eq 1"));
    }
}
