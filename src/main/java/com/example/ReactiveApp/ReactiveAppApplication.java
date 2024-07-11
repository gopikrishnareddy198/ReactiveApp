package com.example.ReactiveApp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Spring WebFlux CRUD Example",
		version = "1.0",
		description = "Spring WebFlux CRUD Example Sample documents"
))
public class ReactiveAppApplication {

	public static void main1(String[] args) {
		SpringApplication.run(ReactiveAppApplication.class, args);
	}


	public static void main(String[] args) {
	/*	System.out.println("In sample main method");
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader)classLoader).getURLs();

		for (URL url: urls){
			System.out.println(url);
		}*/

		Mono<String> stringMono = Mono.just("Hello");

		stringMono.doOnSuccess(ReactiveAppApplication::print)
				.subscribe();





	}

	public static void print(String element){
		System.out.println(element);
	}
}
