package accesoya_backend.places.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateEstablishmentRequest(

        @NotNull(message = "El lugar seleccionado es obligatorio") UUID placeId,

        @NotBlank(message = "El nombre del establecimiento es obligatorio") @Size(max = 200, message = "El nombre no puede superar los 200 caracteres") String businessName,

        @NotBlank(message = "La dirección es obligatoria") @Size(max = 1000, message = "La dirección no puede superar los 1000 caracteres") String businessAddress,

        @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres") String businessPhone,

        @Size(max = 100, message = "El tipo de establecimiento no puede superar los 100 caracteres") String businessType,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres") String description

) {
}