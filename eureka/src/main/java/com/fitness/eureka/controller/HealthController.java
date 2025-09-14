package com.fitness.eureka.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "Eureka Service Discovery Server");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("port", 8761);
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(healthInfo);
    }

    @GetMapping("/status")
    public ResponseEntity<String> simpleHealth() {
        return ResponseEntity.ok("Eureka Service Discovery Server is healthy and running!");
    }
}
