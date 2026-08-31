package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.DashboardResponse;
import accesoya_backend.places.application.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Información del dashboard según el rol")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    // =====================================================
    // DASHBOARD
    // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
    // =====================================================

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                    'CONSULTOR',
                    'OPERADOR_FLNOC',
                    'SUPERVISOR',
                    'ADMIN'
            )
            """)
    @Operation(summary = "Obtener dashboard", description = """
            Obtiene información del dashboard de acuerdo
            con el rol del usuario autenticado.

            CONSULTOR:
            recibe únicamente información general.

            OPERADOR_FLNOC y SUPERVISOR:
            reciben información general y operativa.

            ADMIN:
            recibe información general y operativa completa.
            """)
    public ResponseEntity<DashboardResponse> getDashboard(
            Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(authentication));
    }
}