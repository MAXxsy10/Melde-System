package dhbw.studienarbeit.meldesystem.controller;


import dhbw.studienarbeit.meldesystem.dto.ApiResponse;
import dhbw.studienarbeit.meldesystem.dto.UpdateUserRequest;
import dhbw.studienarbeit.meldesystem.dto.UserDTO;
import dhbw.studienarbeit.meldesystem.security.CurrentUser;
import dhbw.studienarbeit.meldesystem.security.UserPrincipal;
import dhbw.studienarbeit.meldesystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @CurrentUser UserPrincipal currentUser) {

        UserDTO user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @CurrentUser UserPrincipal currentUser) {

        log.info("Updating user profile: {}", currentUser.getId());
        UserDTO user = userService.updateUser(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated", user));
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> updateFcmToken(
            @RequestParam String token,
            @CurrentUser UserPrincipal currentUser) {

        userService.updateFcmToken(currentUser.getId(), token);
        return ResponseEntity.ok(ApiResponse.success("FCM token updated", null));
    }
}

