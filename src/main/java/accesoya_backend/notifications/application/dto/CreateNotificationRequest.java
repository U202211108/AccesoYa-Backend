package accesoya_backend.notifications.application.dto;

import accesoya_backend.notifications.domain.model.NotificationType;

import java.util.UUID;

public record CreateNotificationRequest(

        UUID userId,

        String title,

        String message,

        NotificationType type,

        UUID relatedEntityId,

        String relatedEntityType

) {
}