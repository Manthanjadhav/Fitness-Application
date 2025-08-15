package com.manthan.activityservice.service;

import com.manthan.activityservice.Repository.ActivityRepository;
import com.manthan.activityservice.dto.ActivityRequest;
import com.manthan.activityservice.dto.ActivityResponse;
import com.manthan.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;

    public ActivityResponse trackActivity(ActivityRequest request) {
        log.info("Calling user Validation Api for userId: "+request.getUserId());
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
}
