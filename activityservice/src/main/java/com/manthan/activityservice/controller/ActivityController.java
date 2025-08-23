package com.manthan.activityservice.controller;

import com.manthan.activityservice.dto.ActivityRequest;
import com.manthan.activityservice.dto.ActivityResponse;
import com.manthan.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request, @RequestHeader("X-User-ID") String userId){
        if(userId!=null){
            request.setUserId(userId);
        }
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId){
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId){
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable String activityId,
            @RequestBody ActivityRequest request,
            @RequestHeader("X-User-ID") String userId) {

        // Ensure the userId in header is used
        if (userId != null) {
            request.setUserId(userId);
        }

        ActivityResponse updatedActivity = activityService.updateActivity(activityId, request);
        return ResponseEntity.ok(updatedActivity);
    }

}
