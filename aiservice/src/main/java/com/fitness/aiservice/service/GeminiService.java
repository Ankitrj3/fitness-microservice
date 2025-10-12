package com.fitness.aiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

        private final WebClient.Builder webClientBuilder;

        @Value("${gemini.api.url}")
        private String geminiApiUrl;

        @Value("${GEMINI_KEY}")
        private String geminiApiKey;

        @PostConstruct
        public void init() {
                log.info("Gemini API URL: {}", geminiApiUrl);
                log.info("Gemini API Key present: {}", geminiApiKey != null && !geminiApiKey.isEmpty());
        }

        public String getRecommendations(String details) {
                Map<String, Object> requestBody = Map.of(
                                "contents", new Object[] {
                                                Map.of("parts", new Object[] {
                                                                Map.of("text", details)
                                                })
                                });

                String response = webClientBuilder.build()
                                .post()
                                .uri(geminiApiUrl)
                                .header("Content-Type", "application/json")
                                .header("x-goog-api-key", geminiApiKey)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                return response;
        }
}
