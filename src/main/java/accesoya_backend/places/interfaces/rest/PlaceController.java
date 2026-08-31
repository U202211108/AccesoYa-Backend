package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.CreatePlaceRequest;
import accesoya_backend.places.application.dto.FlmNocFilterResponse;
import accesoya_backend.places.application.dto.FlmNocSiteResponse;
import accesoya_backend.places.application.dto.PlaceDetailResponse;
import accesoya_backend.places.application.dto.PlaceMapResponse;
import accesoya_backend.places.application.dto.PlaceResponse;
import accesoya_backend.places.application.dto.PlaceSearchResponse;
import accesoya_backend.places.application.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "Consulta y gestión de sitios de telecomunicaciones FLM/NOC")
@SecurityRequirement(name = "bearerAuth")
public class PlaceController {

        private final PlaceService placeService;

        // =====================================================
        // BUSCAR POR NOMBRE
        // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Buscar sitios por nombre", description = """
                        Busca sitios de telecomunicaciones por coincidencia
                        parcial en el nombre.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios encontrados"),
                        @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/search")
        @PreAuthorize("""
                        hasAnyRole(
                                'CONSULTOR',
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<PlaceSearchResponse> searchByName(

                        @Parameter(description = "Texto a buscar en el nombre", example = "Torre") @RequestParam String name,

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
        // DETALLE DEL SITIO
        // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Obtener detalle de un sitio", description = """
                        Obtiene la información detallada de un sitio
                        de telecomunicaciones.

                        La información operacional FLM/NOC se incluye
                        únicamente para usuarios autorizados.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitio obtenido correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "404", description = "Sitio no encontrado")
        })
        @GetMapping("/{id}")
        @PreAuthorize("""
                        hasAnyRole(
                                'CONSULTOR',
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<PlaceDetailResponse> getPlaceById(
                        @PathVariable UUID id,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                placeService.getPlaceById(
                                                id,
                                                authentication));
        }

        // =====================================================
        // BUSCAR POR TIPO / CATEGORÍA
        // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Buscar sitios por categoría", description = """
                        Busca sitios según su categoría general.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios encontrados"),
                        @ApiResponse(responseCode = "400", description = "Categoría inválida"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/category")
        @PreAuthorize("""
                        hasAnyRole(
                                'CONSULTOR',
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<PlaceSearchResponse> searchByCategory(

                        @RequestParam String type,

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
        // SITIOS PARA EL MAPA
        // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Obtener sitios para el mapa", description = """
                        Obtiene los sitios activos dentro del área
                        geográfica indicada.

                        La información operacional FLM/NOC se incluye
                        únicamente para OPERADOR_FLNOC, SUPERVISOR
                        y ADMIN.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios obtenidos correctamente"),
                        @ApiResponse(responseCode = "400", description = "Parámetros geográficos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/map")
        @PreAuthorize("""
                        hasAnyRole(
                                'CONSULTOR',
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
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
        // BÚSQUEDA GLOBAL PARA EL MAPA
        // CONSULTOR / OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Buscar sitios globalmente para el mapa", description = """
                        Busca sitios independientemente de la posición
                        actual del mapa.

                        La información operacional se controla según
                        el rol del usuario autenticado.
                        """)
        @GetMapping("/map/search")
        @PreAuthorize("""
                        hasAnyRole(
                                'CONSULTOR',
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<PlaceMapResponse>> searchPlacesForMap(
                        @RequestParam String query) {

                List<PlaceMapResponse> response = placeService.searchPlacesForMap(query);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // MIS SITIOS
        // OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Obtener mis sitios", description = """
                        Obtiene los sitios asociados al usuario
                        autenticado.

                        Disponible para operadores FLM/NOC,
                        supervisores y administradores.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios obtenidos correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos")
        })
        @GetMapping("/me")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<PlaceMapResponse>> getMyPlaces(
                        Authentication authentication) {

                List<PlaceMapResponse> response = placeService.getMyPlaces(authentication);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // SITIOS FLM / NOC
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Obtener sitios FLM/NOC", description = """
                        Obtiene los sitios que tienen información
                        FLM/NOC registrada en el sistema.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios FLM/NOC obtenidos correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos")
        })
        @GetMapping("/flm-noc")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<FlmNocSiteResponse>> getFlmNocSites() {

                List<FlmNocSiteResponse> response = placeService.getFlmNocSites();

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // FILTROS DISPONIBLES FLM / NOC
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Obtener filtros disponibles FLM/NOC", description = """
                        Obtiene los valores disponibles para filtrar
                        sitios FLM/NOC.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Filtros obtenidos correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos")
        })
        @GetMapping("/flm-noc/filters")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<FlmNocFilterResponse> getFlmNocFilters() {

                return ResponseEntity.ok(
                                placeService.getFlmNocFilters());
        }

        // =====================================================
        // FILTRAR SITIOS FLM / NOC
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Filtrar sitios FLM/NOC", description = """
                        Obtiene sitios FLM/NOC activos aplicando
                        filtros opcionales por tipo de estación,
                        zonal y tecnología.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Sitios filtrados correctamente"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos")
        })
        @GetMapping("/flm-noc/filter")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<FlmNocSiteResponse>> filterFlmNocSites(

                        @RequestParam(required = false) String tipoEstacion,

                        @RequestParam(required = false) String zonal,

                        @RequestParam(required = false) String tecnologia,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "500") int size

        ) {

                List<FlmNocSiteResponse> response = placeService.filterFlmNocSites(
                                tipoEstacion,
                                zonal,
                                tecnologia,
                                page,
                                size);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // FILTRO POR TIPO DE ESTACIÓN
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Filtrar sitios por tipo de estación", description = """
                        Obtiene sitios FLM/NOC activos según
                        el tipo de estación seleccionado.
                        """)
        @GetMapping("/flm-noc/tipo-estacion")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<PlaceMapResponse>> getPlacesByTipoEstacion(

                        @RequestParam String tipoEstacion,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(
                                placeService.getPlacesByTipoEstacion(
                                                tipoEstacion,
                                                authentication));
        }

        // =====================================================
        // FILTRO POR TECNOLOGÍA
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Filtrar sitios por tecnología", description = """
                        Obtiene sitios FLM/NOC activos según
                        la tecnología seleccionada.
                        """)
        @GetMapping("/flm-noc/tecnologia")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<PlaceMapResponse>> getPlacesByTecnologia(

                        @RequestParam String tecnologia,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(
                                placeService.getPlacesByTecnologia(
                                                tecnologia,
                                                authentication));
        }

        // =====================================================
        // FILTRO POR ZONAL
        // SOLO OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Filtrar sitios por zonal", description = """
                        Obtiene sitios FLM/NOC activos según
                        el zonal seleccionado.
                        """)
        @GetMapping("/flm-noc/zonal")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<List<PlaceMapResponse>> getPlacesByZonal(

                        @RequestParam String zonal,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(
                                placeService.getPlacesByZonal(
                                                zonal,
                                                authentication));
        }

        // =====================================================
        // REGISTRAR SITIO FLM / NOC
        // OPERADOR / SUPERVISOR / ADMIN
        // =====================================================

        @Operation(summary = "Registrar sitio FLM/NOC", description = """
                        Registra un nuevo sitio de telecomunicaciones
                        en AccesoYa.

                        El sitio queda asociado automáticamente
                        al usuario autenticado.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Sitio registrado correctamente"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para registrar sitios")
        })
        @PostMapping
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        public ResponseEntity<PlaceResponse> createPlace(
                        @RequestBody CreatePlaceRequest request) {

                PlaceResponse response = placeService.createPlace(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }
}