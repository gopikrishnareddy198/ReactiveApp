package com.example.ReactiveApp.handler;

import com.example.ReactiveApp.service.ProcessRepairsService;
import com.example.ReactiveApp.service.RepairsService;
import com.example.ReactiveApp.util.ValidationUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RepairsHandler {

    private ProcessRepairsService repairsService;

    public RepairsHandler(ProcessRepairsService repairsService) {
        this.repairsService = repairsService;
    }

    public Mono<ServerResponse> processRepairs(final ServerRequest request) {
        final String dsId = request.queryParam("dsId").orElse("");
        final String caseId = request.pathVariable("caseId");

        if (!ValidationUtils.isValidString(caseId)) {
            return ServerResponse.badRequest().bodyValue("Invalid caseId");
        } else if (!ValidationUtils.isValidString(dsId)) {
            return ServerResponse.badRequest().bodyValue("Invalid dsId");
        }

        Mono<List<String>> payloadMono = request.bodyToMono(new ParameterizedTypeReference<List<String>>() {});

        return payloadMono
                .flatMap(ValidationUtils::validateRequestBody)
                .flatMap(serialNumbers -> {
                    return repairsService.processDataAndUpload(caseId, dsId, serialNumbers)
                            .flatMap(success -> {
                                return ServerResponse.ok().bodyValue("Repairs processed successfully");
                            })
                            .onErrorResume(error -> {
                                return ServerResponse.badRequest().bodyValue("Error: " + error.getMessage());
                            });
                }).onErrorResume(error -> {
                    return ServerResponse.badRequest().bodyValue("Error: " + error.getMessage());
                });
    }

}
