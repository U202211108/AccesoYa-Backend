package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import java.time.Instant;
import java.util.UUID;

public record PlaceDetailResponse(

                UUID id,

                String name,

                String description,

                String address,

                String department,

                String province,

                String district,

                String ubigeo,

                Double latitude,

                Double longitude,

                PlaceType type,

                PlaceSource source,

                PlaceStatus status,

                String classification,

                String establishmentType,

                String category,

                String phone,

                String openingHours,

                String sourceStatus,

                String imageUrl1,

                String imageUrl2,

                String imageUrl3,

                PlaceAccessibilityResponse accessibility,

                Instant createdAt,

                Instant updatedAt

) {
}