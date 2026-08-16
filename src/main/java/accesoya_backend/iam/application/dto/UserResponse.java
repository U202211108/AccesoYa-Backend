package accesoya_backend.iam.application.dto;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt

) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}