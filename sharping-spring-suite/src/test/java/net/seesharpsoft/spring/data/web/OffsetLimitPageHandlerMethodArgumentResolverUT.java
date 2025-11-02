package net.seesharpsoft.spring.data.web;

import net.seesharpsoft.spring.data.domain.OffsetLimitRequest;
import net.seesharpsoft.spring.test.controller.SimpleControllerDummy;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.StartsWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OffsetLimitPageHandlerMethodArgumentResolverUT {

    private MockMvc mockMvc;

    private OffsetLimitPageHandlerMethodArgumentResolver argumentResolverMock;
    
    private static final Pageable FALLBACK_PAGEABLE = PageRequest.of(0, 1);
    private static final int MAX_PAGE_SIZE = 14;

    public OffsetLimitPageHandlerMethodArgumentResolver getDefaultArgumentResolver() {
        OffsetLimitPageHandlerMethodArgumentResolver offsetLimitPageHandlerMethodArgumentResolver = new OffsetLimitPageHandlerMethodArgumentResolver();
        offsetLimitPageHandlerMethodArgumentResolver.setFallbackPageable(FALLBACK_PAGEABLE);
        offsetLimitPageHandlerMethodArgumentResolver.setMaxPageSize(MAX_PAGE_SIZE);
        return offsetLimitPageHandlerMethodArgumentResolver;
    }

    public void setup() {
        setup(getDefaultArgumentResolver());
    }

    public void setup(OffsetLimitPageHandlerMethodArgumentResolver argumentResolver) {
        argumentResolverMock = spy(
                argumentResolver
        );

        mockMvc = MockMvcBuilders.standaloneSetup(new SimpleControllerDummy())
                .setCustomArgumentResolvers(argumentResolverMock)
                .build();
    }

    @Test
    public void handler_should_be_used_for_simple_pageable_endpoint() throws Exception {
        setup();
        mockMvc.perform(get("/pageable"))
                .andExpect(status().isOk());

        verify(argumentResolverMock).resolveArgument(any(), any(), any(), any());
    }

    @Test
    public void handler_should_resolve_empty_pageable_parameter_as_fallback_pageable() throws Exception {
        setup();
        mockMvc.perform(get("/pageable"))
                .andExpect(status().isOk())
                .andExpect(content().string(FALLBACK_PAGEABLE.toString()));
    }

    @Test
    public void handler_should_ignore_insufficient_limit_offset_and_fallback_to_page_request_0() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?offset=1&size=4"))
                .andExpect(status().isOk())
                .andExpect(content().string(PageRequest.of(FALLBACK_PAGEABLE.getPageNumber(), 4).toString()));
    }

    @Test
    public void handler_should_ignore_insufficient_limit_offset_and_fallback_to_page_request_1() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?page=1&offset=4"))
                .andExpect(status().isOk())
                .andExpect(content().string(PageRequest.of(1, FALLBACK_PAGEABLE.getPageSize()).toString()));
    }

    @Test
    public void handler_should_resolve_parameter_as_OffsetLimitRequest() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?offset=1&limit=5"))
                .andExpect(status().isOk())
                // .andExpect(content().string(new StartsWith(new OffsetLimitRequest(1, 5).toString())))
        ;
    }

    @Test
    public void handler_should_resolve_parameter_as_OffsetLimitRequest_with_sort() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?offset=1&limit=5&sort=test,desc"))
                .andExpect(status().isOk())
                // .andExpect(content().string(new StartsWith(new OffsetLimitRequest(1, 5, new Sort(Sort.Direction.DESC, "test")).toString())))
        ;
    }
    
    @Test
    public void handler_should_resolve_parameter_as_PageRequest() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(content().string(PageRequest.of(1, 5).toString()));
    }

    @Test
    public void handler_should_resolve_parameter_as_PageRequest_with_sort() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?page=1&size=5&sort=test1,desc&sort=test2"))
                .andExpect(status().isOk())
                .andExpect(content().string(PageRequest.of(1, 5, Sort.by(new Sort.Order(Sort.Direction.DESC, "test1"), new Sort.Order(Sort.Direction.ASC, "test2"))).toString()));
    }

    @Test
    public void handler_should_resolve_incorrect_offset_parameter_with_default_value() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?offset=a&limit=5"))
                .andExpect(status().isOk())
                .andExpect(content().string(new OffsetLimitRequest(0, 5).toString()));
    }

    @Test
    public void handler_should_resolve_incorrect_limit_parameter_with_default_value() throws Exception {
        setup();
        mockMvc.perform(get("/pageable?offset=1&limit=3a4"))
                .andExpect(status().isOk())
                .andExpect(content().string(new OffsetLimitRequest(1, MAX_PAGE_SIZE).toString()));
    }
    
}
