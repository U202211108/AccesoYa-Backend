package accesoya_backend.notifications.interfaces.rest;

import accesoya_backend.iam.domain.model.User;
import accesoya_backend.notifications.application.dto.NotificationResponse;
import accesoya_backend.notifications.application.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // =====================================================
    // MIS NOTIFICACIONES
    // =====================================================

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(

            @AuthenticationPrincipal User user

    ) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        user.getId()));
    }

    // =====================================================
    // NO LEÍDAS
    // =====================================================

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(

            @AuthenticationPrincipal User user

    ) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(
                        user.getId()));
    }

    // =====================================================
    // CONTADOR
    // =====================================================

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread(

            @AuthenticationPrincipal User user

    ) {

        return ResponseEntity.ok(
                notificationService.countUnread(
                        user.getId()));
    }

    // =====================================================
    // MARCAR UNA
    // =====================================================

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(

            @PathVariable UUID id,

            @AuthenticationPrincipal User user

    ) {

        notificationService.markAsRead(
                id,
                user.getId());

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // MARCAR TODAS
    // =====================================================

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(

            @AuthenticationPrincipal User user

    ) {

        notificationService.markAllAsRead(
                user.getId());

        return ResponseEntity.noContent().build();
    }
}