package accesoya_backend.places.infrastructure.openstreetmap;

import accesoya_backend.places.domain.model.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpenStreetMapBatchWriter {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
            INSERT INTO places (
                id,
                external_id,
                source,
                name,
                description,
                address,
                department,
                province,
                district,
                ubigeo,
                latitude,
                longitude,
                type,
                classification,
                establishment_type,
                category,
                phone,
                opening_hours,
                source_status,
                status,
                image_url1,
                image_url2,
                image_url3,
                created_at,
                updated_at
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
            ON CONFLICT (external_id, source)
            DO UPDATE SET
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                address = EXCLUDED.address,
                department = EXCLUDED.department,
                province = EXCLUDED.province,
                district = EXCLUDED.district,
                latitude = EXCLUDED.latitude,
                longitude = EXCLUDED.longitude,
                type = EXCLUDED.type,
                classification = EXCLUDED.classification,
                establishment_type = EXCLUDED.establishment_type,
                category = EXCLUDED.category,
                phone = EXCLUDED.phone,
                opening_hours = EXCLUDED.opening_hours,
                source_status = EXCLUDED.source_status,
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at
            """;

    @Transactional
    public int saveBatch(
            List<Place> places) {

        if (places.isEmpty()) {
            return 0;
        }

        jdbcTemplate.batchUpdate(
                UPSERT_SQL,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(
                            PreparedStatement ps,
                            int index)
                            throws SQLException {

                        Place place = places.get(index);

                        UUID id = UUID.randomUUID();

                        Instant now = Instant.now();

                        ps.setObject(1, id);

                        ps.setString(
                                2,
                                place.getExternalId());

                        ps.setString(
                                3,
                                place.getSource()
                                        .name());

                        ps.setString(
                                4,
                                place.getName());

                        ps.setString(
                                5,
                                place.getDescription());

                        ps.setString(
                                6,
                                place.getAddress());

                        ps.setString(
                                7,
                                place.getDepartment());

                        ps.setString(
                                8,
                                place.getProvince());

                        ps.setString(
                                9,
                                place.getDistrict());

                        ps.setString(
                                10,
                                place.getUbigeo());

                        ps.setObject(
                                11,
                                place.getLatitude());

                        ps.setObject(
                                12,
                                place.getLongitude());

                        ps.setString(
                                13,
                                place.getType()
                                        .name());

                        ps.setString(
                                14,
                                place.getClassification());

                        ps.setString(
                                15,
                                place.getEstablishmentType());

                        ps.setString(
                                16,
                                place.getCategory());

                        ps.setString(
                                17,
                                place.getPhone());

                        ps.setString(
                                18,
                                place.getOpeningHours());

                        ps.setString(
                                19,
                                place.getSourceStatus());

                        ps.setString(
                                20,
                                place.getStatus()
                                        .name());

                        ps.setString(
                                21,
                                place.getImageUrl1());

                        ps.setString(
                                22,
                                place.getImageUrl2());

                        ps.setString(
                                23,
                                place.getImageUrl3());

                        ps.setTimestamp(
                                24,
                                Timestamp.from(now));

                        ps.setTimestamp(
                                25,
                                Timestamp.from(now));
                    }

                    @Override
                    public int getBatchSize() {
                        return places.size();
                    }
                });

        return places.size();
    }
}