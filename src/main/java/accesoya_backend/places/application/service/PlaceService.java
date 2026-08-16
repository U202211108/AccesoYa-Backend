package accesoya_backend.places.application.service;

import accesoya_backend.places.application.dto.PlaceDetailResponse;
import accesoya_backend.places.application.dto.PlaceMapResponse;
import accesoya_backend.places.application.dto.PlaceResponse;
import accesoya_backend.places.application.dto.PlaceSearchResponse;
import accesoya_backend.places.application.dto.UpdatePlaceAccessibilityRequest;
import accesoya_backend.places.domain.model.AccessibilityStatus;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceType;
import accesoya_backend.places.domain.model.PlaceAccessibility;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.application.dto.CreatePlaceRequest;
import accesoya_backend.places.application.dto.PlaceAccessibilityResponse;
import accesoya_backend.places.domain.repository.PlaceAccessibilityRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import accesoya_backend.iam.domain.model.User;
import org.springframework.security.core.Authentication;
import accesoya_backend.iam.domain.model.Role;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {

        private final PlaceRepository placeRepository;

        private final PlaceAccessibilityRepository placeAccessibilityRepository;

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

                if (page < 0) {
                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa");
                }

                if (size < 1 || size > 100) {
                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 100");
                }

                String normalizedSortBy = normalizeSortField(sortBy);

                Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(sortDirection, normalizedSortBy));

                Page<Place> places = placeRepository.findByNameContainingIgnoreCase(
                                name.trim(),
                                pageable);

                return new PlaceSearchResponse(
                                places.getContent()
                                                .stream()
                                                .map(PlaceResponse::from)
                                                .toList(),
                                places.getNumber(),
                                places.getSize(),
                                places.getTotalElements(),
                                places.getTotalPages());
        }

        @Transactional(readOnly = true)
        public PlaceSearchResponse searchByType(
                        String type,
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                if (type == null || type.trim().isBlank()) {
                        throw new IllegalArgumentException(
                                        "La categoría es obligatoria");
                }

                if (page < 0) {
                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa");
                }

                if (size < 1 || size > 100) {
                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 100");
                }

                PlaceType placeType;

                try {

                        placeType = PlaceType.valueOf(
                                        type.trim().toUpperCase());

                } catch (IllegalArgumentException exception) {

                        throw new IllegalArgumentException(
                                        "Categoría de lugar no válida: " + type);
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

                Page<Place> places = placeRepository.findByType(
                                placeType,
                                pageable);

                return new PlaceSearchResponse(
                                places.getContent()
                                                .stream()
                                                .map(PlaceResponse::from)
                                                .toList(),
                                places.getNumber(),
                                places.getSize(),
                                places.getTotalElements(),
                                places.getTotalPages());
        }

        @Transactional(readOnly = true)
        public PlaceDetailResponse getPlaceById(UUID id) {

                if (id == null) {

                        throw new IllegalArgumentException(
                                        "El identificador del lugar es obligatorio");
                }

                Place place = placeRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el lugar con id: " + id));

                PlaceAccessibility accessibility = placeAccessibilityRepository
                                .findByPlaceId(id)
                                .orElse(null);

                return new PlaceDetailResponse(

                                place.getId(),

                                place.getName(),

                                place.getDescription(),

                                place.getAddress(),

                                place.getDepartment(),

                                place.getProvince(),

                                place.getDistrict(),

                                place.getUbigeo(),

                                place.getLatitude(),

                                place.getLongitude(),

                                place.getType(),

                                place.getSource(),

                                place.getStatus(),

                                place.getClassification(),

                                place.getEstablishmentType(),

                                place.getCategory(),

                                place.getPhone(),

                                place.getOpeningHours(),

                                place.getSourceStatus(),

                                place.getImageUrl1(),

                                place.getImageUrl2(),

                                place.getImageUrl3(),

                                mapAccessibility(accessibility),

                                place.getCreatedAt(),

                                place.getUpdatedAt());
        }

        private PlaceAccessibilityResponse mapAccessibility(
                        PlaceAccessibility accessibility) {

                if (accessibility == null) {
                        return PlaceAccessibilityResponse.unknown();
                }

                return new PlaceAccessibilityResponse(

                                accessibility.getWheelchairAccess(),

                                accessibility.getAccessibleEntrance(),

                                accessibility.getAccessibleParking(),

                                accessibility.getAccessibleBathroom(),

                                accessibility.getElevator(),

                                accessibility.getAccessibleRoute(),

                                accessibility.getSignage(),

                                accessibility.getAssistance()

                );
        }

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

                int limit = calculateMapLimit(zoom);

                Pageable pageable = PageRequest.of(
                                0,
                                limit);

                List<Place> places = placeRepository.findPlacesInBoundingBox(
                                minLatitude,
                                maxLatitude,
                                minLongitude,
                                maxLongitude,
                                pageable);

                return places.stream()
                                .map(place -> new PlaceMapResponse(

                                                place.getId(),

                                                place.getName(),

                                                place.getLatitude(),

                                                place.getLongitude(),

                                                place.getType(),

                                                place.getStatus(),

                                                place.getSource(),

                                                place.getAddress(),

                                                place.getPhone(),

                                                place.getOpeningHours(),

                                                place.getCategory(),

                                                place.getDescription(),

                                                place.getEstablishmentType()

                                ))
                                .toList();
        }

        private int calculateMapLimit(int zoom) {

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

        private void validateZoom(int zoom) {

                if (zoom < 1 || zoom > 19) {

                        throw new IllegalArgumentException(
                                        "El nivel de zoom debe estar entre 1 y 19");
                }
        }

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

        private String normalizeSortField(String sortBy) {

                if (sortBy == null || sortBy.isBlank()) {
                        return "name";
                }

                return switch (sortBy) {
                        case "name" -> "name";
                        case "city" -> "province";
                        case "district" -> "district";
                        case "createdAt" -> "createdAt";
                        case "updatedAt" -> "updatedAt";
                        default -> "name";
                };
        }

        @Transactional
        public PlaceResponse createPlace(
                        CreatePlaceRequest request) {

                // =====================================================
                // 1. OBTENER USUARIO AUTENTICADO
                // =====================================================

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User authenticatedUser)) {

                        throw new SecurityException(
                                        "No se pudo identificar al usuario autenticado");
                }

                // =====================================================
                // 2. VERIFICAR ROL
                // =====================================================

                if (authenticatedUser.getRole() != Role.ESTABLISHMENT) {

                        throw new AccessDeniedException(
                                        "Solo los establecimientos pueden registrar un lugar");
                }

                // =====================================================
                // 3. VALIDAR DATOS
                // =====================================================

                if (request == null) {

                        throw new IllegalArgumentException(
                                        "Los datos del establecimiento son obligatorios");
                }

                if (request.name() == null ||
                                request.name().isBlank()) {

                        throw new IllegalArgumentException(
                                        "El nombre del establecimiento es obligatorio");
                }

                if (request.address() == null ||
                                request.address().isBlank()) {

                        throw new IllegalArgumentException(
                                        "La dirección del establecimiento es obligatoria");
                }

                if (request.latitude() == null ||
                                request.longitude() == null) {

                        throw new IllegalArgumentException(
                                        "La ubicación del establecimiento es obligatoria");
                }

                if (request.type() == null) {

                        throw new IllegalArgumentException(
                                        "El tipo de establecimiento es obligatorio");
                }

                // =====================================================
                // 4. VERIFICAR QUE NO TENGA OTRO ESTABLECIMIENTO
                // =====================================================

                if (placeRepository.existsByOwnerId(
                                authenticatedUser.getId())) {

                        throw new IllegalArgumentException(
                                        "El usuario ya tiene un establecimiento registrado");
                }

                // =====================================================
                // 5. CREAR LUGAR
                // =====================================================

                Place place = Place.builder()

                                .externalId(
                                                UUID.randomUUID().toString())

                                .source(
                                                PlaceSource.ACCESOYA)

                                .name(
                                                request.name().trim())

                                .description(
                                                normalize(request.description()))

                                .address(
                                                request.address().trim())

                                .department(
                                                normalize(request.department()))

                                .province(
                                                normalize(request.province()))

                                .district(
                                                normalize(request.district()))

                                .ubigeo(
                                                normalize(request.ubigeo()))

                                .latitude(
                                                request.latitude())

                                .longitude(
                                                request.longitude())

                                .type(
                                                request.type())

                                .classification(
                                                normalize(request.classification()))

                                .establishmentType(
                                                normalize(request.establishmentType()))

                                .category(
                                                normalize(request.category()))

                                .phone(
                                                normalize(request.phone()))

                                .openingHours(
                                                normalize(request.openingHours()))

                                .sourceStatus(
                                                "ACTIVO")

                                .status(
                                                PlaceStatus.ACTIVE)

                                .imageUrl1(
                                                normalize(request.imageUrl1()))

                                .imageUrl2(
                                                normalize(request.imageUrl2()))

                                .imageUrl3(
                                                normalize(request.imageUrl3()))

                                // =================================================
                                // PROPIETARIO
                                // =================================================

                                .owner(
                                                authenticatedUser)

                                .build();

                // =====================================================
                // 6. GUARDAR LUGAR
                // =====================================================

                Place savedPlace = placeRepository.save(place);

                // =====================================================
                // 7. CREAR ACCESIBILIDAD INICIAL
                // =====================================================

                PlaceAccessibility accessibility = PlaceAccessibility.builder()

                                .place(savedPlace)

                                .wheelchairAccess(
                                                AccessibilityStatus.UNKNOWN)

                                .accessibleEntrance(
                                                AccessibilityStatus.UNKNOWN)

                                .accessibleParking(
                                                AccessibilityStatus.UNKNOWN)

                                .accessibleBathroom(
                                                AccessibilityStatus.UNKNOWN)

                                .elevator(
                                                AccessibilityStatus.UNKNOWN)

                                .accessibleRoute(
                                                AccessibilityStatus.UNKNOWN)

                                .signage(
                                                AccessibilityStatus.UNKNOWN)

                                .assistance(
                                                AccessibilityStatus.UNKNOWN)

                                .build();

                placeAccessibilityRepository.save(
                                accessibility);

                // =====================================================
                // 8. RESPUESTA
                // =====================================================

                return PlaceResponse.from(
                                savedPlace);
        }

        private String normalize(String value) {

                if (value == null) {
                        return null;
                }

                String normalized = value.trim();

                return normalized.isBlank()
                                ? null
                                : normalized;
        }

        @Transactional
        public PlaceAccessibilityResponse updateAccessibility(

                        UUID placeId,

                        UpdatePlaceAccessibilityRequest request,

                        Authentication authentication

        ) {

                // =====================================================
                // 1. VALIDAR AUTENTICACIÓN
                // =====================================================

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User authenticatedUser)) {

                        throw new AccessDeniedException(
                                        "No se pudo identificar al usuario autenticado");
                }

                // =====================================================
                // 2. VALIDAR IDENTIFICADOR
                // =====================================================

                if (placeId == null) {

                        throw new IllegalArgumentException(
                                        "El identificador del lugar es obligatorio");
                }

                // =====================================================
                // 3. VALIDAR REQUEST
                // =====================================================

                if (request == null) {

                        throw new IllegalArgumentException(
                                        "Los datos de accesibilidad son obligatorios");
                }

                // =====================================================
                // 4. VERIFICAR ROL
                // =====================================================

                if (authenticatedUser.getRole() != Role.ESTABLISHMENT) {

                        throw new AccessDeniedException(
                                        "Solo los establecimientos pueden actualizar " +
                                                        "la información de accesibilidad");
                }

                // =====================================================
                // 5. OBTENER ID DEL PROPIETARIO
                // =====================================================

                UUID ownerId = authenticatedUser.getId();

                // =====================================================
                // 6. VERIFICAR PROPIEDAD DEL PLACE
                // =====================================================

                Place place = placeRepository
                                .findByIdAndOwnerId(
                                                placeId,
                                                ownerId)
                                .orElseThrow(() -> new AccessDeniedException(
                                                "No tienes permisos para modificar " +
                                                                "este establecimiento"));

                // =====================================================
                // 7. BUSCAR INFORMACIÓN DE ACCESIBILIDAD
                // =====================================================

                PlaceAccessibility accessibility = placeAccessibilityRepository
                                .findByPlaceId(placeId)
                                .orElseGet(() -> {

                                        PlaceAccessibility newAccessibility = PlaceAccessibility.builder()

                                                        .place(place)

                                                        .wheelchairAccess(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .accessibleEntrance(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .accessibleParking(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .accessibleBathroom(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .elevator(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .accessibleRoute(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .signage(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .assistance(
                                                                        AccessibilityStatus.UNKNOWN)

                                                        .build();

                                        return placeAccessibilityRepository
                                                        .save(newAccessibility);
                                });

                // =====================================================
                // 8. ACTUALIZAR CARACTERÍSTICAS
                // =====================================================

                accessibility.setWheelchairAccess(
                                request.wheelchairAccess());

                accessibility.setAccessibleEntrance(
                                request.accessibleEntrance());

                accessibility.setAccessibleParking(
                                request.accessibleParking());

                accessibility.setAccessibleBathroom(
                                request.accessibleBathroom());

                accessibility.setElevator(
                                request.elevator());

                accessibility.setAccessibleRoute(
                                request.accessibleRoute());

                accessibility.setSignage(
                                request.signage());

                accessibility.setAssistance(
                                request.assistance());

                // =====================================================
                // 9. GUARDAR
                // =====================================================

                PlaceAccessibility saved = placeAccessibilityRepository.save(
                                accessibility);

                // =====================================================
                // 10. RESPUESTA
                // =====================================================

                return new PlaceAccessibilityResponse(

                                saved.getWheelchairAccess(),

                                saved.getAccessibleEntrance(),

                                saved.getAccessibleParking(),

                                saved.getAccessibleBathroom(),

                                saved.getElevator(),

                                saved.getAccessibleRoute(),

                                saved.getSignage(),

                                saved.getAssistance());
        }

        // =====================================================
        // BUSCAR ESTABLECIMIENTOS DISPONIBLES
        // =====================================================

        @Transactional(readOnly = true)
        public PlaceSearchResponse searchAvailablePlaces(
                        String name,
                        int page,
                        int size) {

                // =================================================
                // VALIDAR BÚSQUEDA
                // =================================================

                if (name == null || name.trim().isBlank()) {

                        throw new IllegalArgumentException(
                                        "El nombre de búsqueda es obligatorio");
                }

                // =================================================
                // VALIDAR PAGINACIÓN
                // =================================================

                if (page < 0) {

                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa");
                }

                if (size < 1 || size > 50) {

                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 50");
                }

                // =================================================
                // PAGINACIÓN
                // =================================================

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                Sort.Direction.ASC,
                                                "name"));

                // =================================================
                // BUSCAR SOLO PLACES DISPONIBLES
                // =================================================

                Page<Place> places = placeRepository
                                .findByNameContainingIgnoreCaseAndOwnerIsNullAndStatus(
                                                name.trim(),
                                                PlaceStatus.ACTIVE,
                                                pageable);

                // =================================================
                // CONSTRUIR RESPUESTA
                // =================================================

                return new PlaceSearchResponse(

                                places.getContent()
                                                .stream()
                                                .map(PlaceResponse::from)
                                                .toList(),

                                places.getNumber(),

                                places.getSize(),

                                places.getTotalElements(),

                                places.getTotalPages());
        }

        // =====================================================
        // MIS ESTABLECIMIENTOS
        // =====================================================

        @Transactional(readOnly = true)
        public List<PlaceMapResponse> getMyPlaces(
                        Authentication authentication) {

                // =================================================
                // 1. VALIDAR AUTENTICACIÓN
                // =================================================

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User authenticatedUser)) {

                        throw new AccessDeniedException(
                                        "No se pudo identificar al usuario autenticado");
                }

                // =================================================
                // 2. VALIDAR ROL
                // =================================================

                if (authenticatedUser.getRole() != Role.ESTABLISHMENT) {

                        throw new AccessDeniedException(
                                        "Solo los establecimientos pueden consultar sus lugares");
                }

                // =================================================
                // 3. OBTENER ESTABLECIMIENTOS DEL USUARIO
                // =================================================

                List<Place> places = placeRepository.findByOwnerId(
                                authenticatedUser.getId());

                // =================================================
                // 4. CONSTRUIR RESPUESTA
                // =================================================

                return places.stream()
                                .map(place -> new PlaceMapResponse(

                                                place.getId(),

                                                place.getName(),

                                                place.getLatitude(),

                                                place.getLongitude(),

                                                place.getType(),

                                                place.getStatus(),

                                                place.getSource(),

                                                place.getAddress(),

                                                place.getPhone(),

                                                place.getOpeningHours(),

                                                place.getCategory(),

                                                place.getDescription(),

                                                place.getEstablishmentType()

                                ))
                                .toList();
        }
}