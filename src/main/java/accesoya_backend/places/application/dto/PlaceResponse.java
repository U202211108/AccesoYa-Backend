package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import java.time.Instant;
import java.util.UUID;

public record PlaceResponse(

        UUID id,

        String externalId,

        PlaceSource source,

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

        String classification,

        String establishmentType,

        String category,

        String phone,

        String openingHours,

        PlaceStatus status,

        String sourceStatus,

        String imageUrl1,

        String imageUrl2,

        String imageUrl3,

        Instant createdAt,

        Instant updatedAt

) {

    public static PlaceResponse from(Place place) {

        return new PlaceResponse(

                place.getId(),

                place.getExternalId(),

                place.getSource(),

                place.getName(),

                place.getDescription(),

                place.getAddress(),

                place.getDepartment(),

                place.getProvince(),

                place.getDistrict(),

                place.getUbigeo(),

                place.getLatitude(),

                place.getLongitude(),

                place.getType(),

                place.getClassification(),

                place.getEstablishmentType(),

                place.getCategory(),

                place.getPhone(),

                place.getOpeningHours(),

                place.getStatus(),

                place.getSourceStatus(),

                place.getImageUrl1(),

                place.getImageUrl2(),

                place.getImageUrl3(),

                place.getCreatedAt(),

                place.getUpdatedAt());
    }
}