package accesoya_backend.places.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accessibility_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_accessibility_place", columnNames = "place_id")
}, indexes = {
        @Index(name = "idx_accessibility_place", columnList = "place_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessibilityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // =====================================================
    // ESTABLECIMIENTO
    // =====================================================

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // =====================================================
    // ACCESIBILIDAD
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel entrance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel ramps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel elevator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel accessibleRestroom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel accessibleParking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel signage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel braille;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessibilityLevel tactilePath;

    // =====================================================
    // OBSERVACIONES
    // =====================================================

    @Column(length = 1000)
    private String observations;

    // =====================================================
    // AUDITORÍA
    // =====================================================

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

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