package accesoya_backend.places.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "place_accessibility", indexes = {
        @Index(name = "idx_place_accessibility_place", columnList = "place_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceAccessibility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, unique = true)
    private Place place;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus wheelchairAccess;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus accessibleEntrance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus accessibleParking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus accessibleBathroom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus elevator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus accessibleRoute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus signage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityStatus assistance;

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
}