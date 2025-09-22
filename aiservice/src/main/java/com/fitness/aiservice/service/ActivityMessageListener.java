package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "activity-processor-group")
    public void processActivity(Activity activity) {
        log.info("Received activity for processing: {}", activity.getUserId());
        try {
            Recommendation recommendation = activityAIService.generateRecommendation(activity);
            log.info("Generated recommendation: {}", recommendation);
            Recommendation savedRecommendation = recommendationRepository.save(recommendation);
            log.info("Successfully saved recommendation with ID: {}", savedRecommendation.getId());
        } catch (Exception e) {
            log.error("Failed to process activity {}: {}", activity.getId(), e.getMessage(), e);
        }
    }
}
