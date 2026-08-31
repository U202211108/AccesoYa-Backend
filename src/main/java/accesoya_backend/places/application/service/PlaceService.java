package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;

import accesoya_backend.places.application.dto.CreatePlaceRequest;
import accesoya_backend.places.application.dto.FlmNocFilterResponse;
import accesoya_backend.places.application.dto.FlmNocSiteResponse;
import accesoya_backend.places.application.dto.PlaceDetailResponse;
import accesoya_backend.places.application.dto.PlaceMapResponse;
import accesoya_backend.places.application.dto.PlaceResponse;
import accesoya_backend.places.application.dto.PlaceSearchResponse;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {

        // =====================================================
        // REPOSITORIO
        // =====================================================

        private final PlaceRepository placeRepository;

        // =====================================================
        // BÚSQUEDA POR NOMBRE
        // SOLO FLM / NOC
        // =====================================================

        @Transactional(readOnly = true)
        public PlaceSearchResponse searchByName(
                        String name,
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                if (name == null || name.trim().isBlank()) {
                        throw new IllegalArgumentException(
                                        "El nombre de búsqueda es obligatorio");
                }

                validatePagination(page, size);

                String normalizedSortBy = normalizeSortField(sortBy);

                Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                sortDirection,
                                                normalizedSortBy));

                List<Place> places = placeRepository
                                .findByNameContainingIgnoreCaseAndFlmNocDataIsNotNull(
                                                name.trim(),
                                                pageable);

                return toSearchResponse(
                                places,
                                page,
                                size);
        }

        // =====================================================
        // FILTROS COMBINADOS FLM / NOC
        // =====================================================

        @Transactional(readOnly = true)
        public List<FlmNocSiteResponse> filterFlmNocSites(
                        String tipoEstacion,
                        String zonal,
                        String tecnologia,
                        int page,
                        int size) {

                validatePagination(page, size);

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                Sort.Direction.ASC,
                                                "name"));

                return placeRepository
                                .findFlmNocByFilters(
                                                normalize(tipoEstacion),
                                                normalize(zonal),
                                                normalize(tecnologia),
                                                pageable)
                                .stream()
                                .map(FlmNocSiteResponse::from)
                                .toList();
        }

        // =====================================================
        // BÚSQUEDA POR TIPO
        // SOLO FLM / NOC
        // =====================================================

        @Transactional(readOnly = true)
        public PlaceSearchResponse searchByType(
                        String type,
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                if (type == null || type.trim().isBlank()) {
                        throw new IllegalArgumentException(
                                        "El tipo de lugar es obligatorio");
                }

                validatePagination(page, size);

                PlaceType placeType;

                try {
                        placeType = PlaceType.valueOf(
                                        type.trim().toUpperCase());

                } catch (IllegalArgumentException exception) {

                        throw new IllegalArgumentException(
                                        "Tipo de lugar no válido: " + type);
                }

                String normalizedSortBy = normalizeSortField(sortBy);

                Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                sortDirection,
                                                normalizedSortBy));

                List<Place> places = placeRepository
                                .findByTypeAndFlmNocDataIsNotNull(
                                                placeType,
                                                pageable);

                return toSearchResponse(
                                places,
                                page,
                                size);
        }

        // =====================================================
        // BÚSQUEDA DISPONIBLE
        // =====================================================

        @Transactional(readOnly = true)
        public PlaceSearchResponse searchAvailablePlaces(
                        String name,
                        int page,
                        int size) {

                return searchByName(
                                name,
                                page,
                                size,
                                "name",
                                "asc");
        }

        // =====================================================
        // OBTENER DETALLE POR ID
        // =====================================================

        @Transactional(readOnly = true)
        public PlaceDetailResponse getPlaceById(
                        UUID id,
                        Authentication authentication) {

                if (id == null) {
                        throw new IllegalArgumentException(
                                        "El ID del lugar es obligatorio");
                }

                User authenticatedUser = getAuthenticatedUser(authentication);

                Place place = placeRepository
                                .findById(id)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Lugar no encontrado: " + id));

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser.getRole());

                return PlaceDetailResponse.from(
                                place,
                                includeOperationalData);
        }

        // =====================================================
        // OBTENER LUGARES PARA EL MAPA
        //
        // IMPORTANTE:
        // Se obtiene Authentication desde SecurityContextHolder
        // para que el Controller pueda llamar:
        //
        // getPlacesForMap(
        // minLatitude,
        // maxLatitude,
        // minLongitude,
        // maxLongitude,
        // zoom
        // )
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getPlacesForMap(
                        Double minLatitude,
                        Double maxLatitude,
                        Double minLongitude,
                        Double maxLongitude,
                        int zoom) {

                validateBoundingBox(
                                minLatitude,
                                maxLatitude,
                                minLongitude,
                                maxLongitude);

                validateZoom(zoom);

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                int limit = calculateMapLimit(zoom);

                Pageable pageable = PageRequest.of(
                                0,
                                limit);

                List<Place> places = placeRepository
                                .findPlacesInBoundingBox(
                                                minLatitude,
                                                maxLatitude,
                                                minLongitude,
                                                maxLongitude,
                                                pageable);

                return places
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // BÚSQUEDA GLOBAL PARA EL MAPA
        //
        // IMPORTANTE:
        // El Controller puede llamar simplemente:
        //
        // searchPlacesForMap(query)
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> searchPlacesForMap(
                        String query) {

                if (query == null ||
                                query.trim().isBlank()) {

                        return List.of();
                }

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                Pageable pageable = PageRequest.of(
                                0,
                                50);

                List<Place> places = placeRepository
                                .searchPlacesForMap(
                                                query.trim(),
                                                pageable);

                return places
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // MIS SITIOS
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getMyPlaces(
                        Authentication authentication) {

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                List<Place> places = placeRepository
                                .findByOwnerId(
                                                authenticatedUser.getId());

                return places
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // SITIOS FLM / NOC
        // =====================================================

        @Transactional(readOnly = true)
        public List<FlmNocSiteResponse> getFlmNocSites() {

                return placeRepository
                                .findByFlmNocDataIsNotNull()
                                .stream()
                                .map(FlmNocSiteResponse::from)
                                .toList();
        }

        // =====================================================
        // FILTROS DISPONIBLES FLM / NOC
        // =====================================================

        @Transactional(readOnly = true)
        public FlmNocFilterResponse getFlmNocFilters() {

                return new FlmNocFilterResponse(

                                placeRepository
                                                .findDistinctTiposEstacion(),

                                placeRepository
                                                .findDistinctZonales(),

                                placeRepository
                                                .findDistinctTecnologias());
        }

        // =====================================================
        // FILTRO POR TIPO DE ESTACIÓN
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getPlacesByTipoEstacion(
                        String tipoEstacion,
                        Authentication authentication) {

                if (tipoEstacion == null ||
                                tipoEstacion.trim().isBlank()) {

                        return List.of();
                }

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                Pageable pageable = PageRequest.of(
                                0,
                                1000);

                return placeRepository
                                .findByTipoEstacion(
                                                tipoEstacion.trim(),
                                                pageable)
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // FILTRO POR TECNOLOGÍA
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getPlacesByTecnologia(
                        String tecnologia,
                        Authentication authentication) {

                if (tecnologia == null ||
                                tecnologia.trim().isBlank()) {

                        return List.of();
                }

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                Pageable pageable = PageRequest.of(
                                0,
                                1000);

                return placeRepository
                                .findByTecnologia(
                                                tecnologia.trim(),
                                                pageable)
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // FILTRO POR ZONAL
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getPlacesByZonal(
                        String zonal,
                        Authentication authentication) {

                if (zonal == null ||
                                zonal.trim().isBlank()) {

                        return List.of();
                }

                User authenticatedUser = getAuthenticatedUser(authentication);

                boolean includeOperationalData = canViewOperationalData(
                                authenticatedUser);

                Pageable pageable = PageRequest.of(
                                0,
                                1000);

                return placeRepository
                                .findByZonal(
                                                zonal.trim(),
                                                pageable)
                                .stream()
                                .map(place -> PlaceMapResponse.from(
                                                place,
                                                includeOperationalData))
                                .toList();
        }

        // =====================================================
        // CREAR SITIO
        // =====================================================

        @Transactional
        public PlaceResponse createPlace(
                        CreatePlaceRequest request) {

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                User authenticatedUser = getAuthenticatedUser(authentication);

                // =================================================
                // VALIDAR ROL
                // =================================================

                if (authenticatedUser.getRole() != Role.OPERADOR_FLNOC
                                &&
                                authenticatedUser.getRole() != Role.SUPERVISOR
                                &&
                                authenticatedUser.getRole() != Role.ADMIN) {

                        throw new AccessDeniedException(
                                        "El usuario no tiene permisos para registrar sitios");
                }

                // =================================================
                // VALIDAR REQUEST
                // =================================================

                if (request == null) {

                        throw new IllegalArgumentException(
                                        "Los datos del sitio son obligatorios");
                }

                if (request.name() == null ||
                                request.name().isBlank()) {

                        throw new IllegalArgumentException(
                                        "El nombre del sitio es obligatorio");
                }

                if (request.address() == null ||
                                request.address().isBlank()) {

                        throw new IllegalArgumentException(
                                        "La dirección del sitio es obligatoria");
                }

                if (request.latitude() == null ||
                                request.longitude() == null) {

                        throw new IllegalArgumentException(
                                        "La ubicación del sitio es obligatoria");
                }

                if (request.type() == null) {

                        throw new IllegalArgumentException(
                                        "El tipo de sitio es obligatorio");
                }

                // =================================================
                // CREAR SITIO
                // =================================================

                Place place = Place.builder()

                                .externalId(
                                                UUID.randomUUID()
                                                                .toString())

                                .source(
                                                PlaceSource.ACCESOYA)

                                .name(
                                                request.name()
                                                                .trim())

                                .description(
                                                normalize(
                                                                request.description()))

                                .address(
                                                request.address()
                                                                .trim())

                                .department(
                                                normalize(
                                                                request.department()))

                                .province(
                                                normalize(
                                                                request.province()))

                                .district(
                                                normalize(
                                                                request.district()))

                                .ubigeo(
                                                normalize(
                                                                request.ubigeo()))

                                .latitude(
                                                request.latitude())

                                .longitude(
                                                request.longitude())

                                .type(
                                                request.type())

                                .classification(
                                                normalize(
                                                                request.classification()))

                                .establishmentType(
                                                normalize(
                                                                request.establishmentType()))

                                .category(
                                                normalize(
                                                                request.category()))

                                .phone(
                                                normalize(
                                                                request.phone()))

                                .openingHours(
                                                normalize(
                                                                request.openingHours()))

                                .sourceStatus(
                                                "ACTIVO")

                                .status(
                                                PlaceStatus.ACTIVE)

                                .imageUrl1(
                                                normalize(
                                                                request.imageUrl1()))

                                .imageUrl2(
                                                normalize(
                                                                request.imageUrl2()))

                                .imageUrl3(
                                                normalize(
                                                                request.imageUrl3()))

                                .owner(
                                                authenticatedUser)

                                .build();

                Place savedPlace = placeRepository.save(place);

                return PlaceResponse.from(
                                savedPlace);
        }

        // =====================================================
        // VALIDAR ACCESO A INFORMACIÓN OPERACIONAL
        // =====================================================

        private boolean canViewOperationalData(
                        Role role) {

                return role == Role.OPERADOR_FLNOC
                                ||
                                role == Role.SUPERVISOR
                                ||
                                role == Role.ADMIN;
        }

        // =====================================================
        // VALIDAR ACCESO A INFORMACIÓN OPERACIONAL
        // =====================================================

        private boolean canViewOperationalData(
                        User user) {

                if (user == null ||
                                user.getRole() == null) {

                        return false;
                }

                return user.getRole() == Role.OPERADOR_FLNOC
                                ||
                                user.getRole() == Role.SUPERVISOR
                                ||
                                user.getRole() == Role.ADMIN;
        }

        // =====================================================
        // OBTENER USUARIO AUTENTICADO
        // =====================================================

        private User getAuthenticatedUser(
                        Authentication authentication) {

                if (authentication == null ||
                                !authentication.isAuthenticated() ||
                                !(authentication.getPrincipal() instanceof User authenticatedUser)) {

                        throw new SecurityException(
                                        "No se pudo identificar al usuario autenticado");
                }

                return authenticatedUser;
        }

        // =====================================================
        // CONVERSIÓN PAGINADA
        // =====================================================

        private PlaceSearchResponse toSearchResponse(
                        List<Place> places,
                        int page,
                        int size) {

                List<PlaceResponse> content = places.stream()
                                .map(PlaceResponse::from)
                                .toList();

                long totalElements;

                int totalPages;

                if (places.size() < size) {

                        totalElements = ((long) page * size)
                                        + places.size();

                        totalPages = page + 1;

                } else {

                        totalElements = ((long) (page + 1) * size);

                        totalPages = page + 2;
                }

                return new PlaceSearchResponse(
                                content,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }

        // =====================================================
        // VALIDAR PAGINACIÓN
        // =====================================================

        private void validatePagination(
                        int page,
                        int size) {

                if (page < 0) {

                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa");
                }

                if (size < 1 ||
                                size > 1000) {

                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 1000");
                }
        }

        // =====================================================
        // LÍMITE DEL MAPA
        // =====================================================

        private int calculateMapLimit(
                        int zoom) {

                if (zoom <= 5) {
                        return 200;
                }

                if (zoom <= 8) {
                        return 500;
                }

                if (zoom <= 11) {
                        return 800;
                }

                return 1000;
        }

        // =====================================================
        // VALIDAR ZOOM
        // =====================================================

        private void validateZoom(
                        int zoom) {

                if (zoom < 1 ||
                                zoom > 19) {

                        throw new IllegalArgumentException(
                                        "El nivel de zoom debe estar entre 1 y 19");
                }
        }

        // =====================================================
        // VALIDAR BOUNDING BOX
        // =====================================================

        private void validateBoundingBox(
                        Double minLatitude,
                        Double maxLatitude,
                        Double minLongitude,
                        Double maxLongitude) {

                if (minLatitude == null ||
                                maxLatitude == null ||
                                minLongitude == null ||
                                maxLongitude == null) {

                        throw new IllegalArgumentException(
                                        "Todos los límites geográficos son obligatorios");
                }

                if (minLatitude < -90 ||
                                maxLatitude > 90) {

                        throw new IllegalArgumentException(
                                        "La latitud debe estar entre -90 y 90");
                }

                if (minLongitude < -180 ||
                                maxLongitude > 180) {

                        throw new IllegalArgumentException(
                                        "La longitud debe estar entre -180 y 180");
                }

                if (minLatitude > maxLatitude) {

                        throw new IllegalArgumentException(
                                        "minLatitude no puede ser mayor que maxLatitude");
                }

                if (minLongitude > maxLongitude) {

                        throw new IllegalArgumentException(
                                        "minLongitude no puede ser mayor que maxLongitude");
                }
        }

        // =====================================================
        // NORMALIZAR CAMPO DE ORDENAMIENTO
        // =====================================================

        private String normalizeSortField(
                        String sortBy) {

                if (sortBy == null ||
                                sortBy.isBlank()) {

                        return "name";
                }

                return switch (sortBy) {

                        case "name" ->
                                "name";

                        case "city" ->
                                "province";

                        case "district" ->
                                "district";

                        case "createdAt" ->
                                "createdAt";

                        case "updatedAt" ->
                                "updatedAt";

                        default ->
                                "name";
                };
        }

        // =====================================================
        // NORMALIZAR STRING
        // =====================================================

        private String normalize(
                        String value) {

                if (value == null ||
                                value.isBlank()) {

                        return null;
                }

                return value.trim();
        }
}