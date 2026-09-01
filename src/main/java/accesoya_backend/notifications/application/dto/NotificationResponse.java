package accesoya_backend.notifications.application.dto;

import accesoya_backend.notifications.domain.model.Notification;
import accesoya_backend.notifications.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(

                UUID id,

                String title,

                String message,

                NotificationType type,

                boolean read,

                UUID relatedEntityId,

                String relatedEntityType,

                LocalDateTime createdAt

) {

        public static NotificationResponse from(
                        Notification notification) {

                return new NotificationResponse(

                                notification.getId(),

                                notification.getTitle(),

                                notification.getMessage(),

                                notification.getType(),

                                notification.isRead(),

                                notification.getRelatedEntityId(),

                                notification.getRelatedEntityType(),

                                notification.getCreatedAt());
        }
}