package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.Place;
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

        String tipoEstacion,

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

        Instant createdAt,

        Instant updatedAt

) {

    // =====================================================
    // CONVERTIR PLACE → PLACE DETAIL RESPONSE
    // =====================================================

    public static PlaceDetailResponse from(Place place) {

        String tipoEstacion = null;

        if (place.getFlmNocData() != null) {
            tipoEstacion = place.getFlmNocData().getTipoEstacion();
        }

        return new PlaceDetailResponse(

                place.getId(),

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

                tipoEstacion,

                place.getSource(),

                place.getStatus(),

                place.getClassification(),

                place.getEstablishmentType(),

                place.getCategory(),

                place.getPhone(),

                place.getOpeningHours(),

                place.getSourceStatus(),

                place.getImageUrl1(),

                place.getImageUrl2(),

                place.getImageUrl3(),

                place.getCreatedAt(),

                place.getUpdatedAt());
    }
}