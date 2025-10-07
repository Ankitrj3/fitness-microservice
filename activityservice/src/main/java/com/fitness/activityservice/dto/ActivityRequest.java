package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.ActivityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityRequest {
    @NotNull(message = "User ID cannot be null")
    private String userId;
    
    @NotNull(message = "Activity type cannot be null")
    private ActivityType type;
    
    @Positive(message = "Duration must be positive")
    private Integer duration;
    
    @Positive(message = "Calories burned must be positive")
    private Integer caloriesBurned;
    
    @NotNull(message = "Start time cannot be null")
    private LocalDateTime startTime;
    
    private Map<String, Object> additionalMetrics;

}
