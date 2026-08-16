package accesoya_backend.notifications.domain.repository;

import accesoya_backend.notifications.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            UUID userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            UUID userId);

    long countByUserIdAndReadFalse(
            UUID userId);
}