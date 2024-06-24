package com.example.ReactiveApp.router;

import com.example.ReactiveApp.handler.RepairsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import com.example.ReactiveApp.handler.ErrorHandler;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.example.ReactiveApp.constants.ApplicationConstants.API_PATH;
import static com.example.ReactiveApp.constants.ApplicationConstants.PROCESS_REPAIRS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class RepairsRouter {

    @Bean
    public RouterFunction<ServerResponse> doRoute(final RepairsHandler repairsHandler) {
        return
                nest(accept(APPLICATION_JSON),
                        route(POST(API_PATH+PROCESS_REPAIRS),repairsHandler::processRepairs));
    }

}
