package accesoya_backend.places.domain.model;

import jakarta.persistence.*;
import lombok.*;
import accesoya_backend.iam.domain.model.User;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "places", indexes = {

        @Index(name = "idx_places_name", columnList = "name"),

        @Index(name = "idx_places_external_source", columnList = "external_id, source", unique = true),

        @Index(name = "idx_places_lat_lon", columnList = "latitude, longitude"),

        @Index(name = "idx_places_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceSource source;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String address;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(length = 10)
    private String ubigeo;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceType type;

    @Column(length = 500)
    private String classification;

    @Column(length = 500)
    private String establishmentType;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String phone;

    @Column(length = 500)
    private String openingHours;

    @Column(length = 100)
    private String sourceStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceStatus status;

    @Column(length = 500)
    private String imageUrl1;

    @Column(length = 500)
    private String imageUrl2;

    @Column(length = 500)
    private String imageUrl3;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // =====================================================
    // INFORMACIÓN FLM / NOC
    // =====================================================

    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FlmNocData flmNocData;
}
