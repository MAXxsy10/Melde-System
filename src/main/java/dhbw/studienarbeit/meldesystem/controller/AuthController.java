package dhbw.studienarbeit.meldesystem.controller;

import dhbw.studienarbeit.meldesystem.dto.ApiResponse;
import dhbw.studienarbeit.meldesystem.dto.AuthResponse;
import dhbw.studienarbeit.meldesystem.dto.LoginRequest;
import dhbw.studienarbeit.meldesystem.dto.RegisterRequest;
import dhbw.studienarbeit.meldesystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/anonymous")
    public ResponseEntity<ApiResponse<AuthResponse>> anonymousLogin() {
        log.info("Anonymous login request");
        AuthResponse response = authService.createAnonymousToken();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
