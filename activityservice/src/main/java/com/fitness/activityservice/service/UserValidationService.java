package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        log.info("Validating user: {}", userId);
        try {
            Boolean result = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            boolean isValid = result != null && result;
            log.info("User {} validation result: {}", userId, isValid);
            return isValid;

        } catch (WebClientResponseException e) {
            log.error("User service responded with error for user {}: {} - {}",
                    userId, e.getStatusCode(), e.getMessage());

            // If user service says user doesn't exist (404), user is invalid
            if (e.getStatusCode().value() == 404) {
                return false;
            }

            // For other errors (503, 500, etc.), assume service issue - reject for safety
            return false;

        } catch (Exception e) {
            log.error("Failed to validate user {}: {} - {}",
                    userId, e.getClass().getSimpleName(), e.getMessage());

            // For connection errors, timeout, etc. - reject for safety
            return false;
        }
    }
}
