/*
package com.example.ReactiveApp.router;

import com.example.ReactiveApp.handler.RepairsHandler;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.example.ReactiveApp.constants.ApplicationConstants.API_PATH;
import static com.example.ReactiveApp.constants.ApplicationConstants.PROCESS_REPAIRS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class RepairsRouterTests {

    private WebTestClient webTestClient;
    private RepairsHandler repairsHandler;

    @BeforeEach
    void setUp() {
        repairsHandler = mock(RepairsHandler.class);

        // Create the RouterFunction for testing
        RepairsRouter repairsRouter = new RepairsRouter();
        RouterFunction<ServerResponse> routerFunction = repairsRouter.doRoute(repairsHandler);

        // Set up WebTestClient with the RouterFunction
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction)
                .build();
    }

    @Test
    void testProcessRepairsRoute() {
        // Mock behavior of RepairsHandler
        when(repairsHandler.processRepairs(Mockito.any())).thenReturn(ServerResponse.ok().bodyValue("Repairs processed successfully"));

        // Perform POST request to /api/processRepairs with path and query parameters
        webTestClient.post()
                .uri(builder -> builder.path(API_PATH + PROCESS_REPAIRS)
                        .queryParam("dsId", "validDsId")
                        .build("validCaseId"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\"serial1\", \"serial2\", \"serial3\"]")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Repairs processed successfully");
    }


}
*/
