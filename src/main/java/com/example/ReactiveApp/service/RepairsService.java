package com.example.ReactiveApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class RepairsService {

    public Mono<Void> processRepairs(final String fileName, final String dsId, final List<String> serialNumbers){
        log.info("processRepairs(fileName: {}, dsId: {}, serialNumbers: {})",fileName, dsId, serialNumbers);
        return Mono.empty();
    }
}
