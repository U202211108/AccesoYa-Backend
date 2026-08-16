package accesoya_backend.places.infrastructure.openstreetmap;

public record OpenStreetMapRecord(

        String externalId,

        String name,

        Double latitude,

        Double longitude,

        String address,

        String phone,

        String openingHours,

        String category,

        String description

) {
}