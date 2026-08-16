package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.AccessibilityResponse;
import accesoya_backend.places.application.dto.UpdateAccessibilityRequest;
import accesoya_backend.places.application.service.AccessibilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Accessibility", description = "Gestión de accesibilidad de establecimientos")
@SecurityRequirement(name = "bearerAuth")
public class AccessibilityController {

    private final AccessibilityService accessibilityService;

    // =====================================================
    // CONSULTAR ACCESIBILIDAD
    // =====================================================

    @GetMapping("/{placeId}/accessibility")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar accesibilidad", description = """
            Obtiene la información de accesibilidad
            de un establecimiento.

            Si el establecimiento todavía no posee
            un perfil de accesibilidad, se crea
            automáticamente con valores UNKNOWN.
            """)
    public ResponseEntity<AccessibilityResponse> getAccessibility(

            @PathVariable UUID placeId

    ) {

        AccessibilityResponse response = accessibilityService.getAccessibility(
                placeId);

        return ResponseEntity.ok(
                response);
    }

    // =====================================================
    // ACTUALIZAR ACCESIBILIDAD
    // =====================================================

    @PutMapping("/{placeId}/accessibility")
    @PreAuthorize("hasAnyRole('ESTABLISHMENT', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Actualizar accesibilidad", description = """
            Actualiza las características de
            accesibilidad de un establecimiento.

            Un establecimiento solamente puede
            modificar su propio establecimiento.

            Los usuarios MODERATOR y ADMIN pueden
            modificar cualquier establecimiento.
            """)
    public ResponseEntity<AccessibilityResponse> updateAccessibility(

            @PathVariable UUID placeId,

            @Valid @RequestBody UpdateAccessibilityRequest request,

            Authentication authentication

    ) {

        AccessibilityResponse response = accessibilityService.updateAccessibility(
                placeId,
                request,
                authentication);

        return ResponseEntity.ok(
                response);
    }
}