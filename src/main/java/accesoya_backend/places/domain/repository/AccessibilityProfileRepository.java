package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.AccessibilityProfile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccessibilityProfileRepository
        extends JpaRepository<AccessibilityProfile, UUID> {

    Optional<AccessibilityProfile> findByPlaceId(
            UUID placeId);

    boolean existsByPlaceId(
            UUID placeId);
}