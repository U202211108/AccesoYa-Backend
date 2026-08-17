package accesoya_backend.places.domain.repository;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository
    extends JpaRepository<Place, UUID> {

  Optional<Place> findByExternalIdAndSource(
      String externalId,
      PlaceSource source);

  Page<Place> findByNameContainingIgnoreCase(
      String name,
      Pageable pageable);

  Page<Place> findByType(
      PlaceType type,
      Pageable pageable);

  Optional<Place> findByIdAndOwnerId(
      UUID placeId,
      UUID ownerId);

  boolean existsByOwnerId(
      UUID ownerId);

  List<Place> findByOwnerId(
      UUID ownerId);

  Page<Place> findByNameContainingIgnoreCaseAndOwnerIsNullAndStatus(
      String name,
      PlaceStatus status,
      Pageable pageable);

  @Query("""
      SELECT p
      FROM Place p
      LEFT JOIN FETCH p.flmNocData
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
}