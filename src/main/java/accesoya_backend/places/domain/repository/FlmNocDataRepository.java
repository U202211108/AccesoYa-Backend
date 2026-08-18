package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.FlmNocData;
import accesoya_backend.places.domain.model.PlaceStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlmNocDataRepository
                extends JpaRepository<FlmNocData, UUID> {

        // =====================================================
        // PLACE
        // =====================================================

        Optional<FlmNocData> findByPlaceId(
                        UUID placeId);

        // =====================================================
        // TOTAL DE SITIOS ACTIVOS
        // =====================================================

        @Query("""
                        SELECT COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                        """)
        long countByPlaceStatus(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // ZONAL
        // =====================================================

        @Query("""
                        SELECT f.zonal, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.zonal IS NOT NULL
                          AND TRIM(f.zonal) <> ''
                        GROUP BY f.zonal
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByZonal(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // DEPARTAMENTO
        // =====================================================

        @Query("""
                        SELECT f.place.department, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.place.department IS NOT NULL
                          AND TRIM(f.place.department) <> ''
                        GROUP BY f.place.department
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByDepartment(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // PROVINCIA
        // =====================================================

        @Query("""
                        SELECT f.place.province, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.place.province IS NOT NULL
                          AND TRIM(f.place.province) <> ''
                        GROUP BY f.place.province
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByProvince(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // DISTRITO
        // =====================================================

        @Query("""
                        SELECT f.place.district, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.place.district IS NOT NULL
                          AND TRIM(f.place.district) <> ''
                        GROUP BY f.place.district
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByDistrict(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // PROPIETARIO DE TORRE
        // =====================================================

        @Query("""
                        SELECT f.propietarioTorre, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.propietarioTorre IS NOT NULL
                          AND TRIM(f.propietarioTorre) <> ''
                        GROUP BY f.propietarioTorre
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByTowerOwner(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // CLASIFICACIÓN PROPIETARIO DE TORRE
        // =====================================================

        @Query("""
                        SELECT f.clasificacionPropietarioTorre, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.clasificacionPropietarioTorre IS NOT NULL
                          AND TRIM(f.clasificacionPropietarioTorre) <> ''
                        GROUP BY f.clasificacionPropietarioTorre
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByTowerOwnerClassification(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // COBERTURA DE REACCIÓN
        // =====================================================

        @Query("""
                        SELECT f.coberturaReaccion, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.coberturaReaccion IS NOT NULL
                          AND TRIM(f.coberturaReaccion) <> ''
                        GROUP BY f.coberturaReaccion
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByReactionCoverage(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // PATRULLAJE
        // =====================================================

        @Query("""
                        SELECT f.patrullaje, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.patrullaje IS NOT NULL
                          AND TRIM(f.patrullaje) <> ''
                        GROUP BY f.patrullaje
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByPatrol(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // GUARDIANÍA
        // =====================================================

        @Query("""
                        SELECT f.guardiania, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.guardiania IS NOT NULL
                          AND TRIM(f.guardiania) <> ''
                        GROUP BY f.guardiania
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByGuard(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // VIGILANCIA
        // =====================================================

        @Query("""
                        SELECT f.vigilancia, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.vigilancia IS NOT NULL
                          AND TRIM(f.vigilancia) <> ''
                        GROUP BY f.vigilancia
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countBySurveillance(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // RONDA DINÁMICA
        // =====================================================

        @Query("""
                        SELECT f.rondaDinamica, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.rondaDinamica IS NOT NULL
                          AND TRIM(f.rondaDinamica) <> ''
                        GROUP BY f.rondaDinamica
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByDynamicRound(
                        @Param("status") PlaceStatus status);

        // =====================================================
        // MONITOREO CSI
        // =====================================================

        @Query("""
                        SELECT f.monitoreoCsi, COUNT(f)
                        FROM FlmNocData f
                        WHERE f.place.status = :status
                          AND f.monitoreoCsi IS NOT NULL
                          AND TRIM(f.monitoreoCsi) <> ''
                        GROUP BY f.monitoreoCsi
                        ORDER BY COUNT(f) DESC
                        """)
        List<Object[]> countByCsiMonitoring(
                        @Param("status") PlaceStatus status);
}