package com.example.ReactiveApp.util;

import reactor.core.publisher.Mono;

import java.util.List;

public class ValidationUtils {

    public static boolean isValidString(String id) {
        // Replace with actual validation logic (e.g., regex, length check)
        return id == null || id.trim().isEmpty();
    }

    public static Mono<List<String>> validateRequestBody(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Request body cannot be empty"));
        }
        for (String item : list) {
            if (item == null || item.trim().isEmpty()) {
                return Mono.error(new IllegalArgumentException("Request body contains empty strings"));
            }
        }
        return Mono.just(list);
    }
}
