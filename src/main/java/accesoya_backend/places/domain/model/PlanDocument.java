package accesoya_backend.places.domain.model;

import accesoya_backend.iam.domain.model.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plan_documents", indexes = {
        @Index(name = "idx_plan_documents_place", columnList = "place_id"),
        @Index(name = "idx_plan_documents_uploaded_at", columnList = "uploaded_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // =====================================================
    // SITIO FLM / NOC
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // =====================================================
    // ARCHIVO
    // =====================================================

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;

    // =====================================================
    // AUDITORÍA
    // =====================================================

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    // =====================================================
    // CREACIÓN
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }
}