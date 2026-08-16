package accesoya_backend.iam.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre no puede superar los 100 caracteres") String firstName,

        @NotBlank(message = "El apellido es obligatorio") @Size(max = 100, message = "El apellido no puede superar los 100 caracteres") String lastName,

        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") @Size(max = 150, message = "El correo no puede superar los 150 caracteres") String email,

        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres") String password

) {
}