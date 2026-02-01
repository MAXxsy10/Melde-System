package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.dto.UpdateUserRequest;
import dhbw.studienarbeit.meldesystem.dto.UserDTO;
import dhbw.studienarbeit.meldesystem.exceptions.ResourceNotFoundException;
import dhbw.studienarbeit.meldesystem.model.User;
import dhbw.studienarbeit.meldesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserDTO(user);
    }

    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user profile: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update fields if provided
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        if (request.getNotificationRadius() != null) {
            user.setNotificationRadius(request.getNotificationRadius());
        }

        if (request.getLanguagePreference() != null) {
            user.setLanguagePreference(request.getLanguagePreference());
        }

        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
        }

        user = userRepository.save(user);

        log.info("User profile updated successfully: {}", userId);
        return mapToUserDTO(user);
    }

    public void updateFcmToken(Long userId, String fcmToken) {
        log.info("Updating FCM token for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFcmToken(fcmToken);
        userRepository.save(user);

        log.info("FCM token updated successfully for user: {}", userId);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .isAnonymous(user.getIsAnonymous())
                .verified(user.getVerified())
                .notificationRadius(user.getNotificationRadius())
                .languagePreference(user.getLanguagePreference())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

