package accesoya_backend.places.infrastructure.renipress;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RenipressImporter {

        private static final Logger log = LoggerFactory.getLogger(
                        RenipressImporter.class);

        private static final String FILE_PATH = "data/renipress/RENIPRESS_31-07-2026.csv";

        private static final int BATCH_SIZE = 500;

        private final RenipressBatchWriter batchWriter;

        public void importData() {

                log.info("========================================");
                log.info("INICIANDO IMPORTACIÓN RENIPRESS");
                log.info("Archivo: {}", FILE_PATH);
                log.info("========================================");

                long startTime = System.currentTimeMillis();

                int totalRecords = 0;
                int processed = 0;
                int skipped = 0;
                int errors = 0;

                try {

                        ClassPathResource resource = new ClassPathResource(FILE_PATH);

                        try (
                                        BufferedReader reader = new BufferedReader(
                                                        new InputStreamReader(
                                                                        resource.getInputStream(),
                                                                        StandardCharsets.UTF_8));

                                        CSVParser parser = CSVFormat.DEFAULT.builder()
                                                        .setDelimiter(';')
                                                        .setHeader()
                                                        .setSkipHeaderRecord(true)
                                                        .setIgnoreEmptyLines(true)
                                                        .setTrim(true)
                                                        .build()
                                                        .parse(reader)) {

                                List<Place> batch = new ArrayList<>(BATCH_SIZE);

                                for (CSVRecord record : parser) {

                                        totalRecords++;

                                        try {
                                                log.info(
                                                                "CATEGORIA={} | TIPO_ESTABLECIMIENTO={} | CLASIFICACION={}",
                                                                record.get("CATEGORIA"),
                                                                record.get("TIPO_ESTABLECIMIENTO"),
                                                                record.get("CLASIFICACION"));

                                                Optional<Place> place = mapRecord(record);

                                                if (place.isEmpty()) {

                                                        skipped++;

                                                        continue;
                                                }

                                                batch.add(place.get());

                                                if (batch.size() >= BATCH_SIZE) {

                                                        int saved = batchWriter.saveBatch(batch);

                                                        processed += saved;

                                                        log.info(
                                                                        "Procesados: {} | Omitidos: {} | Errores: {}",
                                                                        processed,
                                                                        skipped,
                                                                        errors);

                                                        batch.clear();
                                                }

                                        } catch (Exception exception) {

                                                errors++;

                                                log.error(
                                                                "Error procesando registro {}: {}",
                                                                record.getRecordNumber(),
                                                                exception.getMessage());
                                        }
                                }

                                if (!batch.isEmpty()) {

                                        int saved = batchWriter.saveBatch(batch);

                                        processed += saved;
                                }
                        }

                } catch (Exception exception) {

                        log.error(
                                        "Error durante la importación RENIPRESS",
                                        exception);

                        throw new IllegalStateException(
                                        "No se pudo importar RENIPRESS",
                                        exception);
                }

                long duration = System.currentTimeMillis()
                                - startTime;

                log.info("========================================");
                log.info("IMPORTACIÓN RENIPRESS FINALIZADA");
                log.info("========================================");
                log.info(
                                "Total registros CSV: {}",
                                totalRecords);
                log.info(
                                "Procesados: {}",
                                processed);
                log.info(
                                "Omitidos: {}",
                                skipped);
                log.info(
                                "Errores: {}",
                                errors);
                log.info(
                                "Tiempo: {} ms",
                                duration);
                log.info("========================================");
        }

        private Optional<Place> mapRecord(
                        CSVRecord record) {

                String externalId = clean(
                                record.get("COD_IPRESS"));

                String name = clean(
                                record.get("NOMBRE"));

                if (externalId == null ||
                                externalId.isBlank() ||
                                name == null ||
                                name.isBlank()) {

                        return Optional.empty();
                }

                Double latitude = parseCoordinate(
                                record.get("NORTE"));

                Double longitude = parseCoordinate(
                                record.get("ESTE"));

                if (latitude == null ||
                                longitude == null) {

                        return Optional.empty();
                }

                String sourceStatus = clean(
                                record.get("ESTADO"));

                Place place = new Place();

                place.setExternalId(
                                externalId);

                place.setSource(
                                PlaceSource.RENIPRESS);

                place.setName(
                                name);

                place.setDescription(
                                null);

                place.setAddress(
                                clean(
                                                record.get("DIRECCION")));

                place.setDepartment(
                                clean(
                                                record.get("DEPARTAMENTO")));

                place.setProvince(
                                clean(
                                                record.get("PROVINCIA")));

                place.setDistrict(
                                clean(
                                                record.get("DISTRITO")));

                place.setUbigeo(
                                clean(
                                                record.get("UBIGEO")));

                place.setLatitude(
                                latitude);

                place.setLongitude(
                                longitude);

                // ============================================
                // TIPO DE LUGAR
                // ============================================

                place.setType(
                                mapPlaceType(record));

                place.setClassification(
                                clean(
                                                record.get("CLASIFICACION")));

                place.setEstablishmentType(
                                clean(
                                                record.get(
                                                                "TIPO_ESTABLECIMIENTO")));

                place.setCategory(
                                clean(
                                                record.get("CATEGORIA")));

                place.setPhone(
                                clean(
                                                record.get("TELEFONO")));

                place.setOpeningHours(
                                clean(
                                                record.get("HORARIO")));

                place.setSourceStatus(
                                sourceStatus);

                place.setStatus(
                                mapStatus(
                                                sourceStatus));

                place.setImageUrl1(
                                clean(
                                                record.get("IMAGEN_1")));

                place.setImageUrl2(
                                clean(
                                                record.get("IMAGEN_2")));

                place.setImageUrl3(
                                clean(
                                                record.get("IMAGEN_3")));

                return Optional.of(place);
        }

        private PlaceType mapPlaceType(
                        CSVRecord record) {

                String category = clean(record.get("CATEGORIA"));

                String establishmentType = clean(record.get("TIPO_ESTABLECIMIENTO"));

                String classification = clean(record.get("CLASIFICACION"));

                String value = ((category == null
                                ? ""
                                : category) +

                                " " +

                                (establishmentType == null
                                                ? ""
                                                : establishmentType)
                                +

                                " " +

                                (classification == null
                                                ? ""
                                                : classification))
                                .toLowerCase();

                // ============================================
                // FARMACIA / BOTICA
                // ============================================

                if (value.contains("farmacia") ||
                                value.contains("botica")) {

                        return PlaceType.PHARMACY;
                }

                // ============================================
                // SALUD
                // ============================================

                return PlaceType.HEALTHCARE;
        }

        private PlaceStatus mapStatus(
                        String sourceStatus) {

                if (sourceStatus == null) {

                        return PlaceStatus.INACTIVE;
                }

                return "ACTIVO".equalsIgnoreCase(
                                sourceStatus.trim())
                                                ? PlaceStatus.ACTIVE
                                                : PlaceStatus.INACTIVE;
        }

        private Double parseCoordinate(
                        String value) {

                String cleaned = clean(value);

                if (cleaned == null ||
                                cleaned.isBlank()) {

                        return null;
                }

                try {

                        return Double.parseDouble(
                                        cleaned.replace(
                                                        ",",
                                                        "."));

                } catch (NumberFormatException exception) {

                        return null;
                }
        }

        private String clean(
                        String value) {

                if (value == null) {
                        return null;
                }

                String cleaned = value.trim();

                if (cleaned.isBlank()) {
                        return null;
                }

                return cleaned;
        }
}