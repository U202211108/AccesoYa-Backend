package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.PlaceType;

public record CreatePlaceRequest(

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

        String imageUrl1,

        String imageUrl2,

        String imageUrl3

) {
}