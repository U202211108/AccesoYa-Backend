package accesoya_backend.iam.application.dto;

import accesoya_backend.iam.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(

        @NotNull(message = "El rol es obligatorio")
        Role role

) {
}