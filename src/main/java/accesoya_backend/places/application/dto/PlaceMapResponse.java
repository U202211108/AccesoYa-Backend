package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import java.util.UUID;

public record PlaceMapResponse(

                UUID id,

                String name,

                Double latitude,

                Double longitude,

                PlaceType type,

                PlaceStatus status,

                PlaceSource source,

                String address,

                String phone,

                String openingHours,

                String category,

                String description,

                String establishmentType

) {
}