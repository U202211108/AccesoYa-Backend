package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.PlaceAccessibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlaceAccessibilityRepository
        extends JpaRepository<PlaceAccessibility, UUID> {

    Optional<PlaceAccessibility> findByPlaceId(UUID placeId);

}