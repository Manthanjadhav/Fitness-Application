package com.manthan.aiservice.service;

import com.manthan.aiservice.model.Recommendations;
import com.manthan.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository repository;

    public List<Recommendations> getUserRecommendation(String userId) {
        return repository.findByUserId(userId);
    }

    public Recommendations getActivityRecommendation(String activityId) {
        return repository.findByActivityId(activityId)
                .orElseThrow(()->new RuntimeException("No recommendation found for : "+ activityId));
    }
}
