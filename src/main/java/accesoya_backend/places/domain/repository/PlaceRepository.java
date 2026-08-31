package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository
                extends JpaRepository<Place, UUID> {

        // =====================================================
        // BÚSQUEDA GENERAL
        // =====================================================

        Optional<Place> findByExternalIdAndSource(
                        String externalId,
                        PlaceSource source);

        // =====================================================
        // BÚSQUEDA POR NOMBRE
        // =====================================================

        List<Place> findByNameContainingIgnoreCase(
                        String name,
                        Pageable pageable);

        List<Place> findByNameContainingIgnoreCaseAndFlmNocDataIsNotNull(
                        String name,
                        Pageable pageable);

        // =====================================================
        // BÚSQUEDA POR TIPO
        // =====================================================

        List<Place> findByType(
                        PlaceType type,
                        Pageable pageable);

        List<Place> findByTypeAndFlmNocDataIsNotNull(
                        PlaceType type,
                        Pageable pageable);

        // =====================================================
        // PROPIETARIO / USUARIO GESTOR
        // =====================================================

        Optional<Place> findByIdAndOwnerId(
                        UUID placeId,
                        UUID ownerId);

        List<Place> findByOwnerId(
                        UUID ownerId);

        // =====================================================
        // FLM / NOC
        // =====================================================

        List<Place> findByFlmNocDataIsNotNull();

        // =====================================================
        // FILTROS FLM / NOC
        // =====================================================

        @Query("""
                        SELECT DISTINCT f.tipoEstacion
                        FROM FlmNocData f
                        WHERE f.tipoEstacion IS NOT NULL
                          AND TRIM(f.tipoEstacion) <> ''
                        ORDER BY f.tipoEstacion
                        """)
        List<String> findDistinctTiposEstacion();

        @Query("""
                        SELECT DISTINCT f.zonal
                        FROM FlmNocData f
                        WHERE f.zonal IS NOT NULL
                          AND TRIM(f.zonal) <> ''
                        ORDER BY f.zonal
                        """)
        List<String> findDistinctZonales();

        @Query("""
                        SELECT DISTINCT f.tecnologia
                        FROM FlmNocData f
                        WHERE f.tecnologia IS NOT NULL
                          AND TRIM(f.tecnologia) <> ''
                        ORDER BY f.tecnologia
                        """)
        List<String> findDistinctTecnologias();

        // =====================================================
        // MAPA
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        WHERE p.latitude BETWEEN :minLatitude AND :maxLatitude
                          AND p.longitude BETWEEN :minLongitude AND :maxLongitude
                          AND p.status =
                              accesoya_backend.places.domain.model.PlaceStatus.ACTIVE
                        """)
        List<Place> findPlacesInBoundingBox(
                        @Param("minLatitude") Double minLatitude,
                        @Param("maxLatitude") Double maxLatitude,
                        @Param("minLongitude") Double minLongitude,
                        @Param("maxLongitude") Double maxLongitude,
                        Pageable pageable);

        // =====================================================
        // BÚSQUEDA GLOBAL FLM / NOC
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        LEFT JOIN p.flmNocData f
                        WHERE p.status =
                            accesoya_backend.places.domain.model.PlaceStatus.ACTIVE

                          AND (
                                LOWER(p.name)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.nombreEnCal)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.nombreControlCentral)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.codigoEmplazamiento)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.zonal)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.tipoEstacion)
                                    LIKE LOWER(CONCAT('%', :query, '%'))

                             OR LOWER(f.tecnologia)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                          )

                        ORDER BY
                            CASE
                                WHEN LOWER(p.name) = LOWER(:query)
                                    THEN 1

                                WHEN LOWER(p.name)
                                    LIKE LOWER(CONCAT(:query, '%'))
                                    THEN 2

                                WHEN LOWER(p.name)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                                    THEN 3

                                WHEN LOWER(f.nombreEnCal) = LOWER(:query)
                                    THEN 4

                                WHEN LOWER(f.nombreEnCal)
                                    LIKE LOWER(CONCAT(:query, '%'))
                                    THEN 5

                                WHEN LOWER(f.nombreEnCal)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                                    THEN 6

                                WHEN LOWER(f.codigoEmplazamiento) = LOWER(:query)
                                    THEN 7

                                WHEN LOWER(f.codigoEmplazamiento)
                                    LIKE LOWER(CONCAT(:query, '%'))
                                    THEN 8

                                WHEN LOWER(f.zonal)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                                    THEN 9

                                WHEN LOWER(f.tipoEstacion)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                                    THEN 10

                                WHEN LOWER(f.tecnologia)
                                    LIKE LOWER(CONCAT('%', :query, '%'))
                                    THEN 11

                                ELSE 12
                            END,

                            p.name ASC
                        """)
        List<Place> searchPlacesForMap(
                        @Param("query") String query,
                        Pageable pageable);

        // =====================================================
        // FILTRO POR TIPO DE ESTACIÓN
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        JOIN FETCH p.flmNocData f
                        WHERE p.status =
                            accesoya_backend.places.domain.model.PlaceStatus.ACTIVE
                          AND LOWER(f.tipoEstacion) =
                              LOWER(:tipoEstacion)
                        """)
        List<Place> findByTipoEstacion(
                        @Param("tipoEstacion") String tipoEstacion,
                        Pageable pageable);

        // =====================================================
        // FILTRO POR TECNOLOGÍA
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        JOIN FETCH p.flmNocData f
                        WHERE p.status =
                            accesoya_backend.places.domain.model.PlaceStatus.ACTIVE
                          AND LOWER(f.tecnologia) =
                              LOWER(:tecnologia)
                        """)
        List<Place> findByTecnologia(
                        @Param("tecnologia") String tecnologia,
                        Pageable pageable);

        // =====================================================
        // FILTRO POR ZONAL
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        JOIN FETCH p.flmNocData f
                        WHERE p.status =
                            accesoya_backend.places.domain.model.PlaceStatus.ACTIVE
                          AND LOWER(f.zonal) =
                              LOWER(:zonal)
                        """)
        List<Place> findByZonal(
                        @Param("zonal") String zonal,
                        Pageable pageable);

        // =====================================================
        // FILTROS COMBINADOS FLM / NOC
        // =====================================================

        @Query("""
                        SELECT p
                        FROM Place p
                        JOIN FETCH p.flmNocData f
                        WHERE p.status =
                            accesoya_backend.places.domain.model.PlaceStatus.ACTIVE

                          AND (
                                :tipoEstacion IS NULL
                                OR TRIM(:tipoEstacion) = ''
                                OR LOWER(f.tipoEstacion) =
                                   LOWER(:tipoEstacion)
                              )

                          AND (
                                :zonal IS NULL
                                OR TRIM(:zonal) = ''
                                OR LOWER(f.zonal) =
                                   LOWER(:zonal)
                              )

                          AND (
                                :tecnologia IS NULL
                                OR TRIM(:tecnologia) = ''
                                OR LOWER(f.tecnologia) =
                                   LOWER(:tecnologia)
                              )
                        """)
        List<Place> findFlmNocByFilters(
                        @Param("tipoEstacion") String tipoEstacion,
                        @Param("zonal") String zonal,
                        @Param("tecnologia") String tecnologia,
                        Pageable pageable);
}