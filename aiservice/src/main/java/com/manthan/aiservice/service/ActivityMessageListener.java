package com.manthan.aiservice.service;

import com.manthan.aiservice.dto.ActivityDTO;
import com.manthan.aiservice.model.Recommendations;
import com.manthan.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;
    @RabbitListener(queues = "activity.queue")
    public void processActivity(ActivityDTO activity){
        log.info("Received activity for processing: {}", activity.getId());
        Recommendations recommendations = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendations);
    }
}
