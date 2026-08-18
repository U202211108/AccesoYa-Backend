package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.CreatePlaceRequest;
import accesoya_backend.places.application.dto.PlaceAccessibilityResponse;
import accesoya_backend.places.application.dto.PlaceDetailResponse;
import accesoya_backend.places.application.dto.PlaceMapResponse;
import accesoya_backend.places.application.dto.PlaceResponse;
import accesoya_backend.places.application.dto.PlaceSearchResponse;
import accesoya_backend.places.application.dto.UpdatePlaceAccessibilityRequest;
import accesoya_backend.places.application.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import accesoya_backend.places.application.dto.FlmNocSiteResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "Consulta y gestión de lugares registrados en AccesoYa")
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

        private final PlaceService placeService;

        // =====================================================
        // BUSCAR POR NOMBRE
        // =====================================================

        @Operation(summary = "Buscar lugares por nombre", description = """
                        Busca establecimientos por coincidencia parcial
                        en el nombre. La búsqueda no distingue entre
                        mayúsculas y minúsculas.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lugares encontrados"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/search")
        public ResponseEntity<PlaceSearchResponse> searchByName(

                        @Parameter(description = "Texto a buscar en el nombre", example = "hospital") @RequestParam String name,

                        @Parameter(description = "Número de página, comenzando en 0", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Cantidad de resultados por página", example = "20") @RequestParam(defaultValue = "20") int size,

                        @Parameter(description = "Campo por el cual ordenar", example = "name") @RequestParam(defaultValue = "name") String sortBy,

                        @Parameter(description = "Dirección del ordenamiento", example = "asc") @RequestParam(defaultValue = "asc") String direction

        ) {

                PlaceSearchResponse response = placeService.searchByName(
                                name,
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // BUSCAR POR CATEGORÍA
        // =====================================================

        @Operation(summary = "Buscar lugares por categoría", description = """
                        Busca lugares según su categoría general.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lugares encontrados"),
                        @ApiResponse(responseCode = "400", description = "Categoría inválida"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/category")
        public ResponseEntity<PlaceSearchResponse> searchByCategory(

                        @Parameter(description = "Categoría del lugar", example = "HEALTHCARE") @RequestParam String type,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "20") int size,

                        @RequestParam(defaultValue = "name") String sortBy,

                        @RequestParam(defaultValue = "asc") String direction

        ) {

                PlaceSearchResponse response = placeService.searchByType(
                                type,
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // LUGARES PARA EL MAPA
        // =====================================================

        @Operation(summary = "Obtener lugares para el mapa", description = """
                        Obtiene los lugares activos dentro del área
                        geográfica indicada.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lugares obtenidos correctamente"),
                        @ApiResponse(responseCode = "400", description = "Parámetros geográficos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/map")
        public ResponseEntity<List<PlaceMapResponse>> getPlacesForMap(

                        @Parameter(description = "Latitud mínima", example = "-12.15") @RequestParam Double minLatitude,

                        @Parameter(description = "Latitud máxima", example = "-12.00") @RequestParam Double maxLatitude,

                        @Parameter(description = "Longitud mínima", example = "-77.15") @RequestParam Double minLongitude,

                        @Parameter(description = "Longitud máxima", example = "-76.95") @RequestParam Double maxLongitude,

                        @Parameter(description = "Nivel de zoom actual del mapa", example = "12") @RequestParam(defaultValue = "5") int zoom

        ) {

                List<PlaceMapResponse> response = placeService.getPlacesForMap(
                                minLatitude,
                                maxLatitude,
                                minLongitude,
                                maxLongitude,
                                zoom);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // BUSCAR ESTABLECIMIENTOS DISPONIBLES
        // =====================================================

        @Operation(summary = "Buscar establecimientos disponibles", description = """
                        Busca establecimientos activos que todavía
                        no tienen propietario y que pueden ser
                        solicitados por un usuario.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Establecimientos disponibles encontrados"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/available")
        public ResponseEntity<PlaceSearchResponse> searchAvailablePlaces(

                        @Parameter(description = "Nombre del establecimiento a buscar", example = "Clínica San José") @RequestParam String name,

                        @Parameter(description = "Número de página, comenzando en 0", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Cantidad de resultados por página", example = "10") @RequestParam(defaultValue = "10") int size

        ) {

                PlaceSearchResponse response = placeService.searchAvailablePlaces(
                                name,
                                page,
                                size);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // MIS ESTABLECIMIENTOS
        // SOLO ESTABLISHMENT
        // =====================================================

        @Operation(summary = "Obtener mis establecimientos", description = """
                        Obtiene los establecimientos asociados
                        al usuario autenticado.

                        Requiere el rol ESTABLISHMENT.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Establecimientos obtenidos correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ESTABLISHMENT")
        })
        @GetMapping("/me")
        @PreAuthorize("hasRole('ESTABLISHMENT')")
        public ResponseEntity<List<PlaceMapResponse>> getMyPlaces(
                        Authentication authentication) {

                List<PlaceMapResponse> response = placeService.getMyPlaces(authentication);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // SITIOS FLM / NOC
        // =====================================================

        @Operation(summary = "Obtener sitios FLM/NOC", description = """
                        Obtiene los sitios que tienen información
                        FLM/NOC registrada en el sistema.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios FLM/NOC obtenidos correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/flm-noc")
        public ResponseEntity<List<FlmNocSiteResponse>> getFlmNocSites() {

                List<FlmNocSiteResponse> response = placeService.getFlmNocSites();

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // DETALLE DE LUGAR
        // =====================================================

        @Operation(summary = "Consultar detalle de un lugar", description = """
                        Obtiene la información detallada de un lugar
                        registrado en AccesoYa mediante su identificador.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Detalle del lugar obtenido correctamente"),
                        @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
        })
        @GetMapping("/{id}")
        public ResponseEntity<PlaceDetailResponse> getPlaceById(

                        @Parameter(description = "UUID del lugar", example = "3fbc3723-f78e-4b6a-8869-4c152fd9eaa5") @PathVariable UUID id

        ) {

                PlaceDetailResponse response = placeService.getPlaceById(id);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // ACTUALIZAR ACCESIBILIDAD
        // =====================================================

        @Operation(summary = "Actualizar características de accesibilidad", description = """
                        Permite al propietario de un establecimiento
                        actualizar las características de accesibilidad
                        asociadas a su lugar.

                        El usuario debe tener rol ESTABLISHMENT y ser
                        propietario del lugar que intenta modificar.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Características de accesibilidad actualizadas correctamente"),
                        @ApiResponse(responseCode = "400", description = "Datos de accesibilidad inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos sobre este establecimiento"),
                        @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
        })
        @PutMapping("/{id}/accessibility")
        @PreAuthorize("hasRole('ESTABLISHMENT')")
        public ResponseEntity<PlaceAccessibilityResponse> updateAccessibility(

                        @Parameter(description = "UUID del lugar", example = "3fbc3723-f78e-4b6a-8869-4c152fd9eaa5") @PathVariable UUID id,

                        @Valid @RequestBody UpdatePlaceAccessibilityRequest request,

                        Authentication authentication

        ) {

                PlaceAccessibilityResponse response = placeService.updateAccessibility(
                                id,
                                request,
                                authentication);

                return ResponseEntity.ok(response);
        }

        @PostMapping
        @PreAuthorize("hasRole('ESTABLISHMENT')")
        @Operation(summary = "Registrar establecimiento", description = """
                        Registra un nuevo establecimiento en AccesoYa.
                        El establecimiento queda asociado automáticamente
                        al usuario autenticado.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Establecimiento registrado correctamente"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de establecimiento")
        })
        public ResponseEntity<PlaceResponse> createPlace(
                        @RequestBody CreatePlaceRequest request) {

                PlaceResponse response = placeService.createPlace(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        // =====================================================
        // BÚSQUEDA GLOBAL PARA EL MAPA
        // =====================================================

        @Operation(summary = "Buscar lugares globalmente para el mapa", description = """
                        Busca lugares independientemente
                        de la posición actual del mapa.
                        Para FLM/NOC considera:
                        NOMBRE_DEL_LOCAL,
                        NOMBRE_EN_CAL y
                        NOMBRE_CONTROL_CENTRAL.
                        """)
        @GetMapping("/map/search")
        public ResponseEntity<List<PlaceMapResponse>> searchPlacesForMap(
                        @RequestParam String query) {

                List<PlaceMapResponse> response = placeService.searchPlacesForMap(query);

                return ResponseEntity.ok(response);
        }
}