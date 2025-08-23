package com.manthan.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthan.aiservice.model.Recommendations;
import com.manthan.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final String USER_RECOMMENDATIONS_KEY = "user_recommendations:";
    private static final String ACTIVITY_RECOMMENDATION_KEY = "activity_recommendation:";

    public List<Recommendations> getUserRecommendation(String userId) {
        String cacheKey = USER_RECOMMENDATIONS_KEY + userId;
        try {
            @SuppressWarnings("unchecked")
            List<Recommendations> cached = (List<Recommendations>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Retrieved user recommendations from cache for userId: {}", userId);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve user recommendations from cache for userId: {}, error: {}", userId, e.getMessage());
        }

        List<Recommendations> recommendations = repository.findByUserId(userId);

        try {
            redisTemplate.opsForValue().set(cacheKey, recommendations, CACHE_TTL);
            log.info("Cached user recommendations for userId: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to cache user recommendations for userId: {}, error: {}", userId, e.getMessage());
        }

        return recommendations;
    }

    public Recommendations getActivityRecommendation(String activityId) {
        String cacheKey = ACTIVITY_RECOMMENDATION_KEY + activityId;

        try {
            Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObj != null) {
                // Use injected ObjectMapper to convert LinkedHashMap to Recommendations
                Recommendations recommendation = objectMapper.convertValue(cachedObj, Recommendations.class);
                if (recommendation != null) {
                    log.info("Retrieved activity recommendation from cache for activityId: {}", activityId);
                    return recommendation;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve activity recommendation from cache for activityId: {}, error: {}", activityId, e.getMessage());
        }

        Optional<Recommendations> recommendationOpt = repository.findByActivityId(activityId);
        Recommendations recommendation = recommendationOpt.orElseThrow(() -> new RuntimeException("No recommendation found for : " + activityId));

        try {
            redisTemplate.opsForValue().set(cacheKey, recommendation, CACHE_TTL);
            log.info("Cached activity recommendation for activityId: {}", activityId);
        } catch (Exception e) {
            log.warn("Failed to cache activity recommendation for activityId: {}, error: {}", activityId, e.getMessage());
        }

        return recommendation;
    }

    public Recommendations saveRecommendation(Recommendations recommendation) {
        Recommendations saved = repository.save(recommendation);
        invalidateUserCache(recommendation.getUserId());
        invalidateActivityCache(recommendation.getActivityId());
        return saved;
    }

    public void invalidateUserCache(String userId) {
        try {
            redisTemplate.delete(USER_RECOMMENDATIONS_KEY + userId);
            log.info("Invalidated user recommendations cache for userId: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate user recommendations cache for userId: {}, error: {}", userId, e.getMessage());
        }
    }

    public void invalidateActivityCache(String activityId) {
        try {
            redisTemplate.delete(ACTIVITY_RECOMMENDATION_KEY + activityId);
            log.info("Invalidated activity recommendation cache for activityId: {}", activityId);
        } catch (Exception e) {
            log.warn("Failed to invalidate activity recommendation cache for activityId: {}, error: {}", activityId, e.getMessage());
        }
    }

    public void clearAllRecommendationsCache() {
        try {
            var userKeys = redisTemplate.keys(USER_RECOMMENDATIONS_KEY + "*");
            var activityKeys = redisTemplate.keys(ACTIVITY_RECOMMENDATION_KEY + "*");

            if (userKeys != null && !userKeys.isEmpty()) redisTemplate.delete(userKeys);
            if (activityKeys != null && !activityKeys.isEmpty()) redisTemplate.delete(activityKeys);

            log.info("Cleared all recommendations cache");
        } catch (Exception e) {
            log.warn("Failed to clear recommendations cache, error: {}", e.getMessage());
        }
    }
}
