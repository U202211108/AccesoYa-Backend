package accesoya_backend.places.domain.model;

import accesoya_backend.iam.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "establishment_requests", indexes = {

        @Index(name = "idx_establishment_request_user", columnList = "user_id"),

        @Index(name = "idx_establishment_request_status", columnList = "status"),

        @Index(name = "idx_establishment_request_place", columnList = "place_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // =====================================================
    // USUARIO SOLICITANTE
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // =====================================================
    // PLACE SOLICITADO
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // =====================================================
    // INFORMACIÓN DEL ESTABLECIMIENTO
    // =====================================================

    @Column(nullable = false, length = 200)
    private String businessName;

    @Column(nullable = false, length = 1000)
    private String businessAddress;

    @Column(length = 50)
    private String businessPhone;

    @Column(length = 100)
    private String businessType;

    @Column(length = 1000)
    private String description;

    // =====================================================
    // ESTADO
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstablishmentRequestStatus status;

    // =====================================================
    // AUDITORÍA
    // =====================================================

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = Instant.now();
    }

    // =====================================================
    // REVISIÓN
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(length = 1000)
    private String reviewComment;

    private Instant reviewedAt;
}