package accesoya_backend.iam.interfaces.rest;

import accesoya_backend.iam.application.dto.AuthResponse;
import accesoya_backend.iam.application.dto.LoginRequest;
import accesoya_backend.iam.application.dto.RegisterRequest;
import accesoya_backend.iam.application.dto.UserResponse;
import accesoya_backend.iam.application.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = userService.login(request);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // REFRESCAR SESIÓN
    // =====================================================

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshSession(
            Authentication authentication) {

        AuthResponse response = userService.refreshSession(
                authentication);

        return ResponseEntity.ok(response);
    }
}