package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<String, Activity> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public ActivityResponse trackActivity(ActivityRequest activityRequest) {
        log.info("Tracking activity for user: {}", activityRequest.getUserId());

        try {
            boolean isValidUser = userValidationService.validateUser(activityRequest.getUserId());

            log.info("User validation result for {}: {}", activityRequest.getUserId(), isValidUser);

            if (!isValidUser) {
                log.error("User validation failed for user: {}", activityRequest.getUserId());
                throw new RuntimeException("Invalid or non-existent user: " + activityRequest.getUserId());
            }

            log.info("Building activity object");
            Activity activity = Activity.builder()
                    .userId(activityRequest.getUserId())
                    .type(activityRequest.getType())
                    .duration(activityRequest.getDuration())
                    .caloriesBurned(activityRequest.getCaloriesBurned())
                    .startTime(activityRequest.getStartTime())
                    .additionalMetrics(activityRequest.getAdditionalMetrics())
                    .build();

            log.info("Saving activity to database");
            Activity savedActivity = activityRepository.save(activity);
            log.info("Activity saved with id: {}", savedActivity.getId());

            try {
                log.info("Sending activity to Kafka topic: {}", topicName);
                kafkaTemplate.send(topicName, savedActivity.getUserId(), savedActivity);
                log.info("Activity sent to Kafka successfully");
            } catch (Exception e) {
                log.error("Failed to send activity to Kafka: {}", e.getMessage(), e);
                // Don't fail the request if Kafka fails
            }

            return mapToResponse(savedActivity);
        } catch (Exception e) {
            log.error("Failed to track activity: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ActivityResponse mapToResponse(Activity savedActivity) {
        ActivityResponse activityResponse = new ActivityResponse();
        activityResponse.setId(savedActivity.getId());
        activityResponse.setUserId(savedActivity.getUserId());
        activityResponse.setType(savedActivity.getType());
        activityResponse.setDuration(savedActivity.getDuration());
        activityResponse.setStartTime(savedActivity.getStartTime());
        activityResponse.setAdditionalMetrics(savedActivity.getAdditionalMetrics());
        activityResponse.setCreatedAt(savedActivity.getCreatedAt());
        activityResponse.setUpdatedAt(savedActivity.getUpdatedAt());
        return activityResponse;
    }
}
