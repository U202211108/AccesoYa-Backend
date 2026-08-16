package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.repository.UserRepository;
import accesoya_backend.notifications.application.service.NotificationService;
import accesoya_backend.notifications.domain.model.NotificationType;

import accesoya_backend.places.application.dto.CreateEstablishmentRequest;
import accesoya_backend.places.application.dto.EstablishmentRequestResponse;
import accesoya_backend.places.application.dto.ReviewEstablishmentRequest;

import accesoya_backend.places.domain.model.EstablishmentRequest;
import accesoya_backend.places.domain.model.EstablishmentRequestStatus;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceStatus;

import accesoya_backend.places.domain.repository.EstablishmentRequestRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstablishmentRequestService {

        private final EstablishmentRequestRepository requestRepository;

        private final PlaceRepository placeRepository;

        private final UserRepository userRepository;

        private final NotificationService notificationService;

        // =====================================================
        // CREAR SOLICITUD
        // =====================================================

        @Transactional
        public EstablishmentRequestResponse createRequest(

                        Authentication authentication,

                        CreateEstablishmentRequest request) {

                User authenticatedUser = getAuthenticatedUser(
                                authentication);

                // =================================================
                // 1. VALIDAR ROL
                // =================================================

                if (authenticatedUser.getRole() != Role.USER) {

                        throw new AccessDeniedException(
                                        "Solo los usuarios pueden solicitar " +
                                                        "el registro de un establecimiento");
                }

                // =================================================
                // 2. EVITAR SOLICITUDES PENDIENTES DEL USUARIO
                // =================================================

                boolean hasPendingRequest = requestRepository
                                .existsByUserIdAndStatus(
                                                authenticatedUser.getId(),
                                                EstablishmentRequestStatus.PENDING);

                if (hasPendingRequest) {

                        throw new IllegalArgumentException(
                                        "Ya tienes una solicitud de establecimiento pendiente");
                }

                // =================================================
                // 3. BUSCAR PLACE
                // =================================================

                Place place = placeRepository
                                .findById(request.placeId())
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "El establecimiento seleccionado no existe"));

                // =================================================
                // 4. VALIDAR ESTADO DEL PLACE
                // =================================================

                if (place.getStatus() != PlaceStatus.ACTIVE) {

                        throw new IllegalArgumentException(
                                        "El establecimiento seleccionado no está disponible");
                }

                // =================================================
                // 5. VALIDAR OWNERSHIP
                // =================================================

                if (place.getOwner() != null) {

                        throw new IllegalArgumentException(
                                        "El establecimiento seleccionado " +
                                                        "ya tiene un propietario");
                }

                // =================================================
                // 6. EVITAR SOLICITUDES DUPLICADAS PARA EL PLACE
                // =================================================

                boolean hasPendingPlaceRequest = requestRepository
                                .existsByPlaceIdAndStatus(
                                                place.getId(),
                                                EstablishmentRequestStatus.PENDING);

                if (hasPendingPlaceRequest) {

                        throw new IllegalArgumentException(
                                        "Este establecimiento ya tiene una " +
                                                        "solicitud pendiente de revisión");
                }

                // =================================================
                // 7. CREAR SOLICITUD
                // =================================================

                EstablishmentRequest establishmentRequest = EstablishmentRequest.builder()

                                .user(
                                                authenticatedUser)

                                .place(
                                                place)

                                .businessName(
                                                request.businessName().trim())

                                .businessAddress(
                                                request.businessAddress().trim())

                                .businessPhone(
                                                normalize(
                                                                request.businessPhone()))

                                .businessType(
                                                normalize(
                                                                request.businessType()))

                                .description(
                                                normalize(
                                                                request.description()))

                                .status(
                                                EstablishmentRequestStatus.PENDING)

                                .build();

                EstablishmentRequest saved = requestRepository.save(
                                establishmentRequest);

                // =================================================
                // 8. RESPUESTA
                // =================================================

                return EstablishmentRequestResponse.from(
                                saved);
        }

        // =====================================================
        // MIS SOLICITUDES
        // =====================================================

        @Transactional(readOnly = true)
        public List<EstablishmentRequestResponse> getMyRequests(

                        Authentication authentication) {

                User authenticatedUser = getAuthenticatedUser(
                                authentication);

                return requestRepository

                                .findByUserId(
                                                authenticatedUser.getId())

                                .stream()

                                .map(
                                                EstablishmentRequestResponse::from)

                                .toList();
        }

        // =====================================================
        // SOLICITUDES PENDIENTES
        // =====================================================

        @Transactional(readOnly = true)
        public List<EstablishmentRequestResponse> getPendingRequests() {

                return requestRepository

                                .findByStatus(
                                                EstablishmentRequestStatus.PENDING)

                                .stream()

                                .map(
                                                EstablishmentRequestResponse::from)

                                .toList();
        }

        // =====================================================
        // REVISAR SOLICITUD
        // =====================================================

        @Transactional
        public EstablishmentRequestResponse reviewRequest(

                        UUID requestId,

                        ReviewEstablishmentRequest review,

                        Authentication authentication

        ) {

                // =====================================================
                // 1. OBTENER MODERADOR AUTENTICADO
                // =====================================================

                User moderator = getAuthenticatedUser(
                                authentication);

                // =====================================================
                // 2. VALIDAR ROL
                // =====================================================

                if (moderator.getRole() != Role.MODERATOR &&
                                moderator.getRole() != Role.ADMIN) {

                        throw new AccessDeniedException(
                                        "Solo los moderadores y administradores pueden revisar solicitudes");
                }

                // =====================================================
                // 3. BUSCAR SOLICITUD
                // =====================================================

                EstablishmentRequest request = requestRepository

                                .findById(requestId)

                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Solicitud no encontrada"));

                // =====================================================
                // 4. VERIFICAR QUE ESTÉ PENDIENTE
                // =====================================================

                if (request.getStatus() != EstablishmentRequestStatus.PENDING) {

                        throw new IllegalArgumentException(
                                        "La solicitud ya fue procesada");
                }

                // =====================================================
                // 5. VALIDAR DECISIÓN
                // =====================================================

                if (review.status() == EstablishmentRequestStatus.PENDING) {

                        throw new IllegalArgumentException(
                                        "La solicitud debe ser aprobada o rechazada");
                }

                // =====================================================
                // 6. OBTENER USUARIO Y PLACE
                // =====================================================

                User applicant = request.getUser();

                Place place = request.getPlace();

                // =====================================================
                // 7. APROBACIÓN
                // =====================================================

                if (review.status() == EstablishmentRequestStatus.APPROVED) {

                        // ---------------------------------------------
                        // Validar usuario
                        // ---------------------------------------------

                        if (applicant.getRole() != Role.USER) {

                                throw new IllegalArgumentException(
                                                "El usuario ya no puede convertirse " +
                                                                "en establecimiento");
                        }

                        // ---------------------------------------------
                        // Validar propietario actual
                        // ---------------------------------------------

                        if (place.getOwner() != null &&
                                        !place.getOwner()
                                                        .getId()
                                                        .equals(
                                                                        applicant.getId())) {

                                throw new IllegalArgumentException(
                                                "El establecimiento ya pertenece " +
                                                                "a otro usuario");
                        }

                        // ---------------------------------------------
                        // ASIGNAR PROPIETARIO
                        // ---------------------------------------------

                        place.setOwner(
                                        applicant);

                        // ---------------------------------------------
                        // CAMBIAR ROL
                        // ---------------------------------------------

                        applicant.setRole(
                                        Role.ESTABLISHMENT);

                        placeRepository.save(place);

                        userRepository.save(applicant);
                }

                // =====================================================
                // 8. ACTUALIZAR SOLICITUD
                // =====================================================

                request.setStatus(
                                review.status());

                request.setReviewedBy(
                                moderator);

                request.setReviewComment(
                                normalize(
                                                review.comment()));

                request.setReviewedAt(
                                Instant.now());

                // =====================================================
                // 9. GUARDAR
                // =====================================================

                EstablishmentRequest saved = requestRepository.save(
                                request);

                // =====================================================
                // 10. CREAR NOTIFICACIÓN
                // =====================================================

                if (saved.getStatus() == EstablishmentRequestStatus.APPROVED) {

                        notificationService.createNotification(

                                        applicant.getId(),

                                        "Solicitud aprobada",

                                        "Tu solicitud para registrar \""
                                                        + saved.getBusinessName()
                                                        + "\" ha sido aprobada. "
                                                        + "Ahora puedes administrar "
                                                        + "tu establecimiento.",

                                        NotificationType.ESTABLISHMENT_APPROVED,

                                        saved.getId(),

                                        "ESTABLISHMENT_REQUEST");

                } else if (saved.getStatus() == EstablishmentRequestStatus.REJECTED) {

                        String message = "Tu solicitud para registrar \""
                                        + saved.getBusinessName()
                                        + "\" ha sido rechazada.";

                        String comment = saved.getReviewComment();

                        if (comment != null &&
                                        !comment.isBlank()) {

                                message += " Motivo: "
                                                + comment;
                        }

                        notificationService.createNotification(

                                        applicant.getId(),

                                        "Solicitud rechazada",

                                        message,

                                        NotificationType.ESTABLISHMENT_REJECTED,

                                        saved.getId(),

                                        "ESTABLISHMENT_REQUEST");
                }

                // =====================================================
                // 11. RESPUESTA
                // =====================================================

                return EstablishmentRequestResponse.from(
                                saved);
        }

        // =====================================================
        // OBTENER USUARIO AUTENTICADO
        // =====================================================

        private User getAuthenticatedUser(

                        Authentication authentication) {

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User user)) {

                        throw new AccessDeniedException(
                                        "No se pudo identificar al usuario autenticado");
                }

                return user;
        }

        // =====================================================
        // NORMALIZAR CAMPOS OPCIONALES
        // =====================================================

        private String normalize(
                        String value) {

                if (value == null) {

                        return null;
                }

                String normalized = value.trim();

                return normalized.isBlank()
                                ? null
                                : normalized;
        }
}