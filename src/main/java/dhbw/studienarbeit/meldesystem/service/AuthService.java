package dhbw.studienarbeit.meldesystem.service;

import dhbw.studienarbeit.meldesystem.dto.AuthResponse;
import dhbw.studienarbeit.meldesystem.dto.LoginRequest;
import dhbw.studienarbeit.meldesystem.dto.RegisterRequest;
import dhbw.studienarbeit.meldesystem.dto.UserDTO;
import dhbw.studienarbeit.meldesystem.exceptions.BadRequestException;
import dhbw.studienarbeit.meldesystem.exceptions.ResourceNotFoundException;
import dhbw.studienarbeit.meldesystem.model.User;
import dhbw.studienarbeit.meldesystem.repository.UserRepository;
import dhbw.studienarbeit.meldesystem.security.JwtTokenProvider;
import dhbw.studienarbeit.meldesystem.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// ============= Auth Service =============
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .isAnonymous(request.getIsAnonymous())
                .verified(false)
                .notificationRadius(5) // Default 5km
                .languagePreference("DE")
                .build();

        user = userRepository.save(user);

        // Generate JWT token
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(userPrincipal);

        log.info("User registered successfully with ID: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDTO(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userPrincipal);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDTO(user))
                .build();
    }

    public AuthResponse createAnonymousToken() {
        log.info("Creating anonymous token");

        // Create temporary anonymous user
        String randomEmail = "anon_" + UUID.randomUUID().toString() + "@anonymous.local";

        User anonymousUser = User.builder()
                .email(randomEmail)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname("Anonym")
                .isAnonymous(true)
                .verified(false)
                .notificationRadius(5)
                .languagePreference("DE")
                .build();

        anonymousUser = userRepository.save(anonymousUser);

        UserPrincipal userPrincipal = UserPrincipal.create(anonymousUser);
        String token = tokenProvider.generateToken(userPrincipal);

        log.info("Anonymous token created for user: {}", anonymousUser.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDTO(anonymousUser))
                .build();
    }

    public boolean isAnonymous(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return true;
        }
        return userPrincipal.getIsAnonymous();
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

// ============= User Service =============