package com.example.ReactiveApp.utils;

import com.example.ReactiveApp.handler.RepairsHandler;
import com.example.ReactiveApp.service.ProcessRepairsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.ReactiveApp.service.ProcessRepairsService;
import com.example.ReactiveApp.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RepairsHandlerTest {



    @Mock
    private ProcessRepairsService processRepairsService;

    @InjectMocks
    private RepairsHandler repairsHandler;

    @Mock
    private ServerRequest serverRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testProcessRepairs_validRequest() {
        List<String> validPayload = Arrays.asList("serial1", "serial2", "serial3");

        when(serverRequest.queryParam("dsId")).thenReturn(java.util.Optional.of("validDsId"));
        when(serverRequest.pathVariable("caseId")).thenReturn("validCaseId");
        when(serverRequest.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(validPayload));
        when(processRepairsService.processDataAndUpload(any(String.class), any(String.class), any(List.class))).thenReturn(Mono.empty());

        Mono<ServerResponse> responseMono = repairsHandler.processRepairs(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();
    }
}
