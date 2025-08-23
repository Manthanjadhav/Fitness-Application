package com.manthan.activityservice.service;

import com.manthan.activityservice.Repository.ActivityRepository;
import com.manthan.activityservice.dto.ActivityRequest;
import com.manthan.activityservice.dto.ActivityResponse;
import com.manthan.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public ActivityResponse trackActivity(ActivityRequest request) {
        log.info("Calling user Validation Api for userId: "+request.getUserId());
        log.info("request : "+request);
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser){
            throw new RuntimeException("Invalid User:"+ request.getUserId());
        }
        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned((request.getCaloriesBurned()))
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);
        //publish to RabbitMQ for Ai Processing
        try{
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        }catch (Exception e){
            log.error("Failed to publish activity to RabbitMQ: ", e);
        }
        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity){
        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType())
                .caloriesBurned(activity.getCaloriesBurned())
                .duration(activity.getDuration())
                .startTime(activity.getStartTime())
                .additionalMetrics(activity.getAdditionalMetrics())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity>  activities = activityRepository.findByUserId(userId);
        return activities.stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(()->new RuntimeException("Activity not found with Id "+activityId));
    }

    public ActivityResponse updateActivity(String activityId, ActivityRequest request) {
        // Find existing activity
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with Id " + activityId));

        // Validate user
        if (!userValidationService.validateUser(request.getUserId())) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }

        // Update fields (only if values are provided in request)
        existingActivity.setType(request.getType() != null ? request.getType() : existingActivity.getType());
        existingActivity.setDuration(request.getDuration() != null ? request.getDuration() : existingActivity.getDuration());
        existingActivity.setCaloriesBurned(request.getCaloriesBurned() != null ? request.getCaloriesBurned() : existingActivity.getCaloriesBurned());
        existingActivity.setStartTime(request.getStartTime() != null ? request.getStartTime() : existingActivity.getStartTime());
        existingActivity.setAdditionalMetrics(request.getAdditionalMetrics() != null ? request.getAdditionalMetrics() : existingActivity.getAdditionalMetrics());

        Activity updatedActivity = activityRepository.save(existingActivity);

        // Optionally publish updated activity to RabbitMQ
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, updatedActivity);
        } catch (Exception e) {
            log.error("Failed to publish updated activity to RabbitMQ: ", e);
        }

        return mapToResponse(updatedActivity);
    }
}
