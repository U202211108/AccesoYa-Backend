package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.CreateEstablishmentRequest;
import accesoya_backend.places.application.dto.EstablishmentRequestResponse;
import accesoya_backend.places.application.dto.ReviewEstablishmentRequest;
import accesoya_backend.places.application.service.EstablishmentRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/establishment-requests")
@RequiredArgsConstructor
@Tag(name = "Establishment Requests", description = "Solicitudes para registrar establecimientos en AccesoYa")
@SecurityRequirement(name = "bearerAuth")
public class EstablishmentRequestController {

        private final EstablishmentRequestService requestService;

        // =====================================================
        // CREAR SOLICITUD
        // =====================================================

        @PostMapping
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Solicitar registro de establecimiento", description = """
                        Permite a un usuario solicitar el registro
                        de su establecimiento en AccesoYa.
                        La solicitud queda inicialmente en estado PENDING.
                        """)
        public ResponseEntity<EstablishmentRequestResponse> createRequest(

                        Authentication authentication,

                        @Valid @RequestBody CreateEstablishmentRequest request

        ) {

                EstablishmentRequestResponse response = requestService.createRequest(
                                authentication,
                                request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        // =====================================================
        // MIS SOLICITUDES
        // =====================================================

        @GetMapping("/me")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Consultar mis solicitudes", description = "Obtiene las solicitudes de establecimiento del usuario autenticado.")
        public ResponseEntity<List<EstablishmentRequestResponse>> getMyRequests(

                        Authentication authentication

        ) {

                return ResponseEntity.ok(
                                requestService.getMyRequests(
                                                authentication));
        }

        // =====================================================
        // SOLICITUDES PENDIENTES
        // =====================================================

        @GetMapping("/pending")
        @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
        @Operation(summary = "Consultar solicitudes pendientes", description = "Obtiene las solicitudes pendientes de revisión.")
        public ResponseEntity<List<EstablishmentRequestResponse>> getPendingRequests() {

                return ResponseEntity.ok(
                                requestService.getPendingRequests());
        }

        @PatchMapping("/{id}/review")
        @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
        @Operation(summary = "Revisar solicitud de establecimiento", description = """
                        Permite a un moderador o administrador
                        aprobar o rechazar una solicitud de establecimiento.
                        """)
        public ResponseEntity<EstablishmentRequestResponse> reviewRequest(

                        @PathVariable UUID id,

                        @Valid @RequestBody ReviewEstablishmentRequest review,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(
                                requestService.reviewRequest(
                                                id,
                                                review,
                                                authentication));
        }
}