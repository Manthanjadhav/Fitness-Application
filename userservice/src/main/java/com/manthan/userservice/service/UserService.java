package com.manthan.userservice.service;

import com.manthan.userservice.Repository.UserRepository;
import com.manthan.userservice.dto.RegisterRequest;
import com.manthan.userservice.dto.UserResponse;
import com.manthan.userservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail());
            return UserResponse.builder()
                    .id(existingUser.getId())
                    .keyCloakId(existingUser.getKeyCloakId())
                    .email(existingUser.getEmail())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt())
                    .build();
        }

        try {
            User user = new User();
            user.setKeyCloakId(request.getKeyCloakId());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            User savedUser = userRepository.save(user);

            return UserResponse.builder()
                    .id(savedUser.getId())
                    .keyCloakId(savedUser.getKeyCloakId())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .createdAt(savedUser.getCreatedAt())
                    .updatedAt(savedUser.getUpdatedAt())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }


    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public Boolean existsByUserId(String userId) {
        log.info("Calling user Validation Api for userId: "+userId);
        return userRepository.existsByKeyCloakId(userId);
    }
}
