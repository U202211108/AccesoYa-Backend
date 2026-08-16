package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.EstablishmentRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewEstablishmentRequest(

        @NotNull(message = "El estado de revisión es obligatorio") EstablishmentRequestStatus status,

        @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres") String comment

) {
}