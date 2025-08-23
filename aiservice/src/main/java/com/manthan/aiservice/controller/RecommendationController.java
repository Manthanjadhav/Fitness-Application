package com.manthan.aiservice.controller;

import com.manthan.aiservice.model.Recommendations;
import com.manthan.aiservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendations>> getUserRecommendation(@PathVariable String userId){
        return ResponseEntity.ok(recommendationService.getUserRecommendation(userId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendations> getActivityRecommendation(@PathVariable String activityId){
        return ResponseEntity.ok(recommendationService.getActivityRecommendation(activityId));
    }

    // Additional endpoints for cache management

    @DeleteMapping("/cache/user/{userId}")
    public ResponseEntity<String> invalidateUserCache(@PathVariable String userId) {
        recommendationService.invalidateUserCache(userId);
        return ResponseEntity.ok("User recommendations cache invalidated for userId: " + userId);
    }

    @DeleteMapping("/cache/activity/{activityId}")
    public ResponseEntity<String> invalidateActivityCache(@PathVariable String activityId) {
        recommendationService.invalidateActivityCache(activityId);
        return ResponseEntity.ok("Activity recommendation cache invalidated for activityId: " + activityId);
    }

    @DeleteMapping("/cache/all")
    public ResponseEntity<String> clearAllCache() {
        recommendationService.clearAllRecommendationsCache();
        return ResponseEntity.ok("All recommendations cache cleared successfully");
    }
}