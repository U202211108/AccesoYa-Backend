package accesoya_backend.iam.application.dto;

import accesoya_backend.iam.domain.model.Role;

import java.util.UUID;

public record AuthResponse(

        String token,
        UUID userId,
        String firstName,
        String lastName,
        String email,
        Role role

) {
}