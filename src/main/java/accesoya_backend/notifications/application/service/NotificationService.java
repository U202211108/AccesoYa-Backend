package accesoya_backend.notifications.application.service;

import accesoya_backend.iam.domain.model.Role;
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
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

        private final NotificationRepository notificationRepository;

        private final UserRepository userRepository;

        // =====================================================
        // CREAR NOTIFICACIÓN PARA UN USUARIO
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
        // NOTIFICAR A VARIOS ROLES
        // =====================================================

        public void notifyRoles(

                        Set<Role> roles,

                        String title,

                        String message,

                        NotificationType type,

                        UUID relatedEntityId,

                        String relatedEntityType

        ) {

                List<User> users = userRepository.findAll();

                users.stream()

                                .filter(user -> user.getRole() != null
                                                &&
                                                roles.contains(
                                                                user.getRole()))

                                .filter(user -> user.getStatus() != null
                                                &&
                                                user.getStatus().name()
                                                                .equals("ACTIVE"))

                                .forEach(user ->

                                createNotification(

                                                user.getId(),

                                                title,

                                                message,

                                                type,

                                                relatedEntityId,

                                                relatedEntityType));
        }

        // =====================================================
        // NOTIFICAR CAMBIO DE ROL
        // =====================================================

        public void notifyRoleChanged(

                        UUID userId,

                        Role newRole

        ) {

                createNotification(

                                userId,

                                "Rol actualizado",

                                "Tu rol en AccesoYa ha sido actualizado a "
                                                + formatRole(newRole)
                                                + ".",

                                NotificationType.USER_ROLE_CHANGED,

                                userId,

                                "USER");
        }

        // =====================================================
        // NOTIFICAR CAMBIO DE ESTADO
        // =====================================================

        public void notifyStatusChanged(

                        UUID userId,

                        accesoya_backend.iam.domain.model.UserStatus newStatus

        ) {

                String statusLabel = newStatus.name().equals("ACTIVE")
                                ? "activo"
                                : "inactivo";

                createNotification(

                                userId,

                                "Estado de cuenta actualizado",

                                "El estado de tu cuenta en AccesoYa ahora es "
                                                + statusLabel
                                                + ".",

                                NotificationType.USER_STATUS_CHANGED,

                                userId,

                                "USER");
        }

        // =====================================================
        // NOTIFICACIÓN DEL SISTEMA
        // =====================================================

        public void notifySystem(

                        UUID userId,

                        String title,

                        String message

        ) {

                createNotification(

                                userId,

                                title,

                                message,

                                NotificationType.SYSTEM,

                                null,

                                "SYSTEM");
        }

        // =====================================================
        // MIS NOTIFICACIONES
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
        // NO LEÍDAS
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
        // MARCAR UNA COMO LEÍDA
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

        // =====================================================
        // FORMATEAR ROL
        // =====================================================

        private String formatRole(
                        Role role) {

                return switch (role) {

                        case CONSULTOR ->
                                "Consultor";

                        case OPERADOR_FLNOC ->
                                "Operador FLM/NOC";

                        case SUPERVISOR ->
                                "Supervisor";

                        case ADMIN ->
                                "Administrador";
                };
        }
}