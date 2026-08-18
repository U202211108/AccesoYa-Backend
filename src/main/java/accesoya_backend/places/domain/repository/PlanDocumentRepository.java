package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.PlanDocument;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanDocumentRepository
        extends JpaRepository<PlanDocument, UUID> {

    List<PlanDocument> findByPlaceIdOrderByUploadedAtDesc(
            UUID placeId);

    Optional<PlanDocument> findByIdAndPlaceId(
            UUID id,
            UUID placeId);

    boolean existsByIdAndPlaceId(
            UUID id,
            UUID placeId);
}