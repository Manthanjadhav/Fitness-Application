package com.manthan.aiservice.service;

import com.manthan.aiservice.dto.ActivityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(ActivityDTO activity) {
        log.info("Received activity for processing: {}", activity.getId());

        // This will automatically create or update the recommendation
        aiService.generateRecommendation(activity);

        log.info("Processed recommendation for activity: {}", activity.getId());
    }
}

