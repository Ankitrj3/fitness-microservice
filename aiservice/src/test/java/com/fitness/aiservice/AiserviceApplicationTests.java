package com.fitness.aiservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.kafka.consumer.auto-startup=false",
		"spring.kafka.bootstrap-servers=",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.main.lazy-initialization=true"
})
class AiserviceApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context can load without external
		// dependencies
		// We disable Kafka, Eureka, and use lazy initialization to minimize startup
	}

}
