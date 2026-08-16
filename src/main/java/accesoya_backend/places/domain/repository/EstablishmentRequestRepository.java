package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.EstablishmentRequest;
import accesoya_backend.places.domain.model.EstablishmentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstablishmentRequestRepository
        extends JpaRepository<EstablishmentRequest, UUID> {

    // =====================================================
    // SOLICITUDES DEL USUARIO
    // =====================================================

    List<EstablishmentRequest> findByUserId(
            UUID userId);

    // =====================================================
    // SOLICITUDES POR ESTADO
    // =====================================================

    List<EstablishmentRequest> findByStatus(
            EstablishmentRequestStatus status);

    // =====================================================
    // SOLICITUD PENDIENTE DEL USUARIO
    // =====================================================

    boolean existsByUserIdAndStatus(
            UUID userId,
            EstablishmentRequestStatus status);

    // =====================================================
    // SOLICITUD PENDIENTE PARA UN PLACE
    // =====================================================

    boolean existsByPlaceIdAndStatus(
            UUID placeId,
            EstablishmentRequestStatus status);

    // =====================================================
    // OBTENER SOLICITUD POR ESTADO
    // =====================================================

    Optional<EstablishmentRequest> findByIdAndStatus(
            UUID id,
            EstablishmentRequestStatus status);
}