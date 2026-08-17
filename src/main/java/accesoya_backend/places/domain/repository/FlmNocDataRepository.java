package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.FlmNocData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FlmNocDataRepository
        extends JpaRepository<FlmNocData, UUID> {

    Optional<FlmNocData> findByPlaceId(
            UUID placeId);
}