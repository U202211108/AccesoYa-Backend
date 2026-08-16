package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;
import accesoya_backend.places.application.dto.AccessibilityResponse;
import accesoya_backend.places.application.dto.UpdateAccessibilityRequest;
import accesoya_backend.places.domain.model.AccessibilityLevel;
import accesoya_backend.places.domain.model.AccessibilityProfile;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.repository.AccessibilityProfileRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessibilityService {

    private final AccessibilityProfileRepository accessibilityRepository;

    private final PlaceRepository placeRepository;

    // =====================================================
    // OBTENER ACCESIBILIDAD
    // =====================================================

    @Transactional
    public AccessibilityResponse getAccessibility(
            UUID placeId) {

        Place place = findPlace(placeId);

        AccessibilityProfile profile = accessibilityRepository
                .findByPlaceId(placeId)
                .orElseGet(() -> createDefaultProfile(place));

        return AccessibilityResponse.from(
                profile);
    }

    // =====================================================
    // ACTUALIZAR ACCESIBILIDAD
    // =====================================================

    @Transactional
    public AccessibilityResponse updateAccessibility(
            UUID placeId,
            UpdateAccessibilityRequest request,
            Authentication authentication) {

        User authenticatedUser = getAuthenticatedUser(authentication);

        // =================================================
        // VERIFICAR PERMISOS
        // =================================================

        Place place = getPlaceForUpdate(
                placeId,
                authenticatedUser);

        // =================================================
        // OBTENER O CREAR PERFIL
        // =================================================

        AccessibilityProfile profile = accessibilityRepository
                .findByPlaceId(placeId)
                .orElseGet(() -> createDefaultProfile(place));

        // =================================================
        // ACTUALIZAR DATOS
        // =================================================

        profile.setEntrance(
                request.entrance());

        profile.setRamps(
                request.ramps());

        profile.setElevator(
                request.elevator());

        profile.setAccessibleRestroom(
                request.accessibleRestroom());

        profile.setAccessibleParking(
                request.accessibleParking());

        profile.setSignage(
                request.signage());

        profile.setBraille(
                request.braille());

        profile.setTactilePath(
                request.tactilePath());

        profile.setObservations(
                request.observations());

        // =================================================
        // GUARDAR
        // =================================================

        AccessibilityProfile saved = accessibilityRepository.save(
                profile);

        return AccessibilityResponse.from(
                saved);
    }

    // =====================================================
    // OBTENER ESTABLECIMIENTO PARA ACTUALIZAR
    // =====================================================

    private Place getPlaceForUpdate(
            UUID placeId,
            User authenticatedUser) {

        Role role = authenticatedUser.getRole();

        // =================================================
        // ADMINISTRADOR
        // =================================================

        if (role == Role.ADMIN) {

            return findPlace(placeId);
        }

        // =================================================
        // MODERADOR
        // =================================================

        if (role == Role.MODERATOR) {

            return findPlace(placeId);
        }

        // =================================================
        // ESTABLECIMIENTO
        // =================================================

        if (role == Role.ESTABLISHMENT) {

            return placeRepository
                    .findByIdAndOwnerId(
                            placeId,
                            authenticatedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException(
                            "No tienes permisos para modificar " +
                                    "la accesibilidad de este establecimiento."));
        }

        // =================================================
        // CUALQUIER OTRO ROL
        // =================================================

        throw new AccessDeniedException(
                "No tienes permisos para modificar " +
                        "la accesibilidad de los establecimientos.");
    }

    // =====================================================
    // CREAR PERFIL POR DEFECTO
    // =====================================================

    private AccessibilityProfile createDefaultProfile(
            Place place) {

        AccessibilityProfile profile = AccessibilityProfile.builder()

                .place(place)

                .entrance(
                        AccessibilityLevel.UNKNOWN)

                .ramps(
                        AccessibilityLevel.UNKNOWN)

                .elevator(
                        AccessibilityLevel.UNKNOWN)

                .accessibleRestroom(
                        AccessibilityLevel.UNKNOWN)

                .accessibleParking(
                        AccessibilityLevel.UNKNOWN)

                .signage(
                        AccessibilityLevel.UNKNOWN)

                .braille(
                        AccessibilityLevel.UNKNOWN)

                .tactilePath(
                        AccessibilityLevel.UNKNOWN)

                .observations(null)

                .build();

        return accessibilityRepository.save(
                profile);
    }

    // =====================================================
    // BUSCAR ESTABLECIMIENTO
    // =====================================================

    private Place findPlace(
            UUID placeId) {

        return placeRepository
                .findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Establecimiento no encontrado."));
    }

    // =====================================================
    // USUARIO AUTENTICADO
    // =====================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {

            throw new AccessDeniedException(
                    "No se pudo identificar al usuario autenticado.");
        }

        return user;
    }
}