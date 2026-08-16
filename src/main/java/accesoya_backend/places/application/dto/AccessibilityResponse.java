package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.AccessibilityLevel;
import accesoya_backend.places.domain.model.AccessibilityProfile;

import java.time.Instant;
import java.util.UUID;

public record AccessibilityResponse(

        UUID id,

        UUID placeId,

        AccessibilityLevel entrance,

        AccessibilityLevel ramps,

        AccessibilityLevel elevator,

        AccessibilityLevel accessibleRestroom,

        AccessibilityLevel accessibleParking,

        AccessibilityLevel signage,

        AccessibilityLevel braille,

        AccessibilityLevel tactilePath,

        String observations,

        Instant createdAt,

        Instant updatedAt

) {

    public static AccessibilityResponse from(
            AccessibilityProfile profile) {

        return new AccessibilityResponse(

                profile.getId(),

                profile.getPlace().getId(),

                profile.getEntrance(),

                profile.getRamps(),

                profile.getElevator(),

                profile.getAccessibleRestroom(),

                profile.getAccessibleParking(),

                profile.getSignage(),

                profile.getBraille(),

                profile.getTactilePath(),

                profile.getObservations(),

                profile.getCreatedAt(),

                profile.getUpdatedAt());
    }
}