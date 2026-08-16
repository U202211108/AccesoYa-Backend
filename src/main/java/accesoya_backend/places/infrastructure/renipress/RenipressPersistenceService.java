package accesoya_backend.places.infrastructure.renipress;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RenipressPersistenceService {

    private final PlaceRepository placeRepository;

    public boolean exists(
            String externalId,
            PlaceSource source) {

        return placeRepository
                .findByExternalIdAndSource(
                        externalId,
                        source)
                .isPresent();
    }

    public Place save(Place place) {

        return placeRepository.save(place);
    }
}