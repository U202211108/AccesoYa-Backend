package accesoya_backend.iam.application.dto;

import accesoya_backend.iam.domain.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

                @NotNull(message = "El estado es obligatorio") UserStatus status

) {
}