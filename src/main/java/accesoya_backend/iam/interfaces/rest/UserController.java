package accesoya_backend.iam.interfaces.rest;

import accesoya_backend.iam.application.dto.UpdateProfileRequest;
import accesoya_backend.iam.application.dto.UserResponse;
import accesoya_backend.iam.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Operaciones relacionadas con el perfil del usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    public ResponseEntity<UserResponse> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getMyProfile(authentication));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del perfil del usuario autenticado")
    public ResponseEntity<UserResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateMyProfile(
                        authentication,
                        request));
    }
}