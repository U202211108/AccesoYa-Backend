package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.AccessibilityLevel;

import jakarta.validation.constraints.NotNull;

public record UpdateAccessibilityRequest(

        @NotNull AccessibilityLevel entrance,

        @NotNull AccessibilityLevel ramps,

        @NotNull AccessibilityLevel elevator,

        @NotNull AccessibilityLevel accessibleRestroom,

        @NotNull AccessibilityLevel accessibleParking,

        @NotNull AccessibilityLevel signage,

        @NotNull AccessibilityLevel braille,

        @NotNull AccessibilityLevel tactilePath,

        String observations

) {
}