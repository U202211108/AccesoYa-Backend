package accesoya_backend.notifications.application.service;

import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.repository.UserRepository;
import accesoya_backend.notifications.application.dto.NotificationResponse;
import accesoya_backend.notifications.domain.model.Notification;
import accesoya_backend.notifications.domain.model.NotificationType;
import accesoya_backend.notifications.domain.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    // =====================================================
    // CREAR NOTIFICACIÓN
    // =====================================================

    public NotificationResponse createNotification(

            UUID userId,

            String title,

            String message,

            NotificationType type,

            UUID relatedEntityId,

            String relatedEntityType

    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuario no encontrado."));

        Notification notification = Notification.builder()

                .user(user)

                .title(title)

                .message(message)

                .type(type)

                .read(false)

                .relatedEntityId(
                        relatedEntityId)

                .relatedEntityType(
                        relatedEntityType)

                .build();

        Notification saved = notificationRepository.save(
                notification);

        return NotificationResponse.from(
                saved);
    }

    // =====================================================
    // OBTENER MIS NOTIFICACIONES
    // =====================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(
            UUID userId) {

        return notificationRepository

                .findByUserIdOrderByCreatedAtDesc(
                        userId)

                .stream()

                .map(NotificationResponse::from)

                .toList();
    }

    // =====================================================
    // OBTENER NO LEÍDAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            UUID userId) {

        return notificationRepository

                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId)

                .stream()

                .map(NotificationResponse::from)

                .toList();
    }

    // =====================================================
    // CONTADOR
    // =====================================================

    @Transactional(readOnly = true)
    public long countUnread(
            UUID userId) {

        return notificationRepository
                .countByUserIdAndReadFalse(
                        userId);
    }

    // =====================================================
    // MARCAR COMO LEÍDA
    // =====================================================

    public void markAsRead(

            UUID notificationId,

            UUID userId

    ) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Notificación no encontrada."));

        if (!notification
                .getUser()
                .getId()
                .equals(userId)) {

            throw new SecurityException(
                    "No tienes permiso para modificar esta notificación.");
        }

        notification.setRead(true);

        notificationRepository.save(
                notification);
    }

    // =====================================================
    // MARCAR TODAS COMO LEÍDAS
    // =====================================================

    public void markAllAsRead(
            UUID userId) {

        List<Notification> notifications = notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId);

        notifications.forEach(
                notification -> notification.setRead(true));

        notificationRepository.saveAll(
                notifications);
    }
}