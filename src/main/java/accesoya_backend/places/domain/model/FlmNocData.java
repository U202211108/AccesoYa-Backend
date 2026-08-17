package accesoya_backend.places.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "place_flm_noc", indexes = {
        @Index(name = "idx_flm_noc_codigo", columnList = "codigo_emplazamiento"),
        @Index(name = "idx_flm_noc_zonal", columnList = "zonal")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlmNocData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // =====================================================
    // RELACIÓN CON PLACE
    // =====================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, unique = true)
    private Place place;

    // =====================================================
    // IDENTIFICACIÓN FLM / NOC
    // =====================================================

    @Column(name = "nombre_en_cal", length = 500)
    private String nombreEnCal;

    @Column(name = "nombre_control_central", length = 500)
    private String nombreControlCentral;

    @Column(name = "numero_linea_comunicacion", length = 200)
    private String numeroLineaComunicacion;

    @Column(name = "local_recojo_llaves", length = 500)
    private String localRecojoLlaves;

    @Column(name = "codigo_emplazamiento", length = 100)
    private String codigoEmplazamiento;

    @Column(name = "zonal", length = 100)
    private String zonal;

    // =====================================================
    // TORRE
    // =====================================================

    @Column(name = "propietario_torre", length = 300)
    private String propietarioTorre;

    @Column(name = "clasificacion_propietario_torre", length = 300)
    private String clasificacionPropietarioTorre;

    // =====================================================
    // OPERACIÓN
    // =====================================================

    @Column(name = "cobertura_reaccion", length = 100)
    private String coberturaReaccion;

    @Column(name = "patrullaje", length = 100)
    private String patrullaje;

    @Column(name = "guardiania", length = 100)
    private String guardiania;

    @Column(name = "vigilancia", length = 100)
    private String vigilancia;

    @Column(name = "ronda_dinamica", length = 100)
    private String rondaDinamica;

    @Column(name = "monitoreo_csi", length = 100)
    private String monitoreoCsi;
}