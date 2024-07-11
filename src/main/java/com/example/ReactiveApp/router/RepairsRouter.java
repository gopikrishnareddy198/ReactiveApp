package com.example.ReactiveApp.router;

import com.example.ReactiveApp.handler.RepairsHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.example.ReactiveApp.constants.ApplicationConstants.API_PATH;
import static com.example.ReactiveApp.constants.ApplicationConstants.PROCESS_REPAIRS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class RepairsRouter {

    @Bean

    @RouterOperations(
            {
                    @RouterOperation(
                            path = API_PATH+PROCESS_REPAIRS,
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = RepairsHandler.class,
                            beanMethod = "processRepairs",
                            operation = @Operation(
                                    operationId = "processRepairs",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation"
                                            )
                                    }

                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> doRoute(final RepairsHandler repairsHandler) {
        return
                nest(accept(APPLICATION_JSON),
                        route(POST(API_PATH+PROCESS_REPAIRS),repairsHandler::processRepairs));
    }

}
