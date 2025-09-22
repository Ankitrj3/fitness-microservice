package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
  private final GeminiService geminiService;

  public Recommendation generateRecommendation(Activity activity) {
    log.info("Starting recommendation generation for activity: {}", activity.getId());
    String prompt = createPromptForActivity(activity);
    log.info("Created prompt for Gemini API");
    String aiResponse = geminiService.getRecommendations(prompt);
    log.info("RESPONSE FROM AI {}", aiResponse);
    Recommendation result = processAIResponse(activity, aiResponse);
    log.info("Final recommendation result: {}", result);
    return result;
  }

  private Recommendation processAIResponse(Activity activity, String aiResponse) {
    try {
      log.info("Raw AI Response: {}", aiResponse);

      if (aiResponse == null || aiResponse.trim().isEmpty()) {
        log.warn("AI response is null or empty");
        return createDefaultRecommendation(activity);
      }

      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(aiResponse);
      JsonNode textNode = rootNode.path("candidates")
          .get(0)
          .path("content")
          .get("parts")
          .get(0)
          .path("text");
      String jsonContent = textNode.asText()
          .replaceAll("```json", "")
          .replaceAll("```", "")
          .replaceAll("\\n\\s*\\n", "\n")
          .trim();
      log.info("Cleaned JSON Content: {}", jsonContent);
      log.info("Extracted JSON Content: {}", jsonContent);
      JsonNode analysisNode = mapper.readTree(jsonContent);
      log.info("Parsed Analysis Node: {}", analysisNode.toString());
      JsonNode analysisJson = analysisNode.path("analysis");
      log.info("Analysis JSON: {}", analysisJson.toString());

      StringBuilder fullAnalysis = new StringBuilder();
      addAnalysisSection(fullAnalysis, analysisJson, "overall", "Overall:");
      addAnalysisSection(fullAnalysis, analysisJson, "pace", "Pace:");
      addAnalysisSection(fullAnalysis, analysisJson, "heartRate", "HeartRate:");
      addAnalysisSection(fullAnalysis, analysisJson, "caloriesBurned", "CaloriesBurned:");

      log.info("Full Analysis Built: {}", fullAnalysis.toString());

      JsonNode improvementsNode = analysisNode.path("improvements");
      JsonNode suggestionsNode = analysisNode.path("suggestions");
      JsonNode safetyNode = analysisNode.path("safety");

      log.info("Improvements Node: {}", improvementsNode.toString());
      log.info("Suggestions Node: {}", suggestionsNode.toString());
      log.info("Safety Node: {}", safetyNode.toString());

      List<String> improvements = extractImprovements(improvementsNode);
      List<String> Suggestions = extractSuggestion(suggestionsNode);
      List<String> Safety = extractSafetyGuidlines(safetyNode);

      log.info("Extracted improvements: {}", improvements);
      log.info("Extracted suggestions: {}", Suggestions);
      log.info("Extracted safety: {}", Safety);

      Recommendation recommendation = Recommendation.builder()
          .activityId(activity.getId())
          .userId(activity.getUserId())
          .type(activity.getType().toString())
          .recommendation(fullAnalysis.toString().trim())
          .improvements(improvements)
          .suggestions(Suggestions)
          .safety(Safety)
          .createdAt(LocalDateTime.now())
          .build();

      log.info("Successfully created recommendation: {}", recommendation);
      return recommendation;

    } catch (Exception e) {
      log.error("Failed to process AI response at step: {}", e.getClass().getSimpleName());
      log.error("Error message: {}", e.getMessage());
      log.error("Full stack trace: ", e);
      log.error("AI Response that failed: {}", aiResponse);
      log.warn("Falling back to default recommendation");
      return createDefaultRecommendation(activity);
    }
  }

  private Recommendation createDefaultRecommendation(Activity activity) {
    return Recommendation.builder()
        .activityId(activity.getId())
        .userId(activity.getUserId())
        .type(activity.getType().toString())
        .recommendation("Unable to generate detailed analysis")
        .improvements(Collections.singletonList("Continue With current routine"))
        .suggestions(Collections.singletonList("Consider consulting a fitness consultant"))
        .safety(Arrays.asList(
            "Always warm up before exerxise",
            "Stay Hydrated",
            "Listen to your body"))
        .createdAt(LocalDateTime.now())
        .build();
  }

  private List<String> extractSafetyGuidlines(JsonNode safetyNode) {
    List<String> safety = new ArrayList<>();
    if (safetyNode.isArray()) {
      safetyNode.forEach(item -> safety.add(item.asText()));
    }
    return safety.isEmpty() ? Collections.singletonList("Follow general safety guidelines") : safety;
  }

  private List<String> extractSuggestion(JsonNode suggestionsNode) {
    List<String> Suggestions = new ArrayList<>();
    if (suggestionsNode.isArray()) {
      suggestionsNode.forEach(suggest -> {
        String workout = suggest.path("workout").asText();
        String description = suggest.path("description").asText();
        Suggestions.add(String.format("%s: %s", workout, description));
      });
    }
    return Suggestions.isEmpty() ? Collections.singletonList("No specific suggestion provided") : Suggestions;
  }

  private List<String> extractImprovements(JsonNode improvementsNode) {
    List<String> improvementList = new ArrayList<>();
    if (improvementsNode.isArray()) {
      improvementsNode.forEach(improvement -> {
        String area = improvement.path("area").asText();
        String recommendation = improvement.path("recommendation").asText();
        improvementList.add(String.format("%s: %s", area, recommendation));

      });
    }
    return improvementList.isEmpty() ? Collections.singletonList("No specific improvement provided") : improvementList;
  }

  private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
    if (!analysisNode.path(key).isMissingNode()) {
      fullAnalysis.append(prefix)
          .append(analysisNode.path(key).asText())
          .append("\n\n");
    }
  }

  private String createPromptForActivity(Activity activity) {
    return String.format(
        """
            Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
            {
              "analysis": {
                "overall": "Overall analysis here",
                "pace": "Pace analysis here",
                "heartRate": "Heart rate analysis here",
                "caloriesBurned": "Calories analysis here"
              },
              "improvements": [
                {
                  "area": "Area name",
                  "recommendation": "Detailed recommendation"
                }
              ],
              "suggestions": [
                {
                  "workout": "Workout name",
                  "description": "Detailed workout description"
                }
              ],
              "safety": [
                "Safety point 1",
                "Safety point 2"
              ]
            }

            Analyze this activity:
            Activity Type: %s
            Duration: %d minutes
            Calories Burned: %d
            Additional Metrics: %s

            Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
            Ensure the response follows the EXACT JSON format shown above.
            """,
        activity.getType(),
        activity.getDuration(),
        activity.getCaloriesBurned(),
        activity.getAdditionalMetrics());
  }
}
