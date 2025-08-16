package com.manthan.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.aiservice.dto.GeminiRecommendation;
import com.manthan.aiservice.model.Recommendations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Recommendations parseGeminiResponse(String geminiApiResponse, String activityId, String userId, String activityType) {
        try {
            // Step 1: Extract "text" field
            JsonNode root = objectMapper.readTree(geminiApiResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Step 2: Clean backticks and "```json"
            String cleanJson = text.replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Step 3: Map to GeminiRecommendation DTO
            GeminiRecommendation geminiRec = objectMapper.readValue(cleanJson, GeminiRecommendation.class);
            StringBuilder recommendationStr = new StringBuilder();
            recommendationStr
                    .append("Overall: ").append(geminiRec.getAnalysis().getOverall()).append("\n")
                    .append("Pace: ").append(geminiRec.getAnalysis().getPace()).append("\n")
                    .append("Heart Rate: ").append(geminiRec.getAnalysis().getHeartRate()).append("\n")
                    .append("Calories Burned: ").append(geminiRec.getAnalysis().getCaloriesBurned()).append("\n\n");

            // Step 4: Convert to Recommendations entity
            return Recommendations.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .activityType(activityType)
                    .recommendation(recommendationStr.toString())
                    .improvements(geminiRec.getImprovements().stream()
                            .map(imp -> imp.getArea() + ": " + imp.getRecommendation())
                            .toList())
                    .suggestions(geminiRec.getSuggestions().stream()
                            .map(sug -> sug.getWorkout() + ": " + sug.getDescription())
                            .toList())
                    .safety(geminiRec.getSafety())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return Recommendations.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .activityType(activityType)
                    .recommendation("No AI analysis available. Default recommendation: Maintain consistent workouts and track progress.")
                    .improvements(List.of("Improve pace gradually", "Stay consistent", "Track progress weekly"))
                    .suggestions(List.of("Go for a light jog", "Do stretching exercises"))
                    .safety(List.of("Stay hydrated", "Warm up before workout", "Cool down after workout"))
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
}
