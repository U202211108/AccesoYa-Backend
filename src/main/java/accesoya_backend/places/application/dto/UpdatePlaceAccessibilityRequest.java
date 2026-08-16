package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.AccessibilityStatus;

public record UpdatePlaceAccessibilityRequest(

        AccessibilityStatus wheelchairAccess,

        AccessibilityStatus accessibleEntrance,

        AccessibilityStatus accessibleParking,

        AccessibilityStatus accessibleBathroom,

        AccessibilityStatus elevator,

        AccessibilityStatus accessibleRoute,

        AccessibilityStatus signage,

        AccessibilityStatus assistance

) {
}