package accesoya_backend.places.infrastructure.flm;

import accesoya_backend.places.domain.model.FlmNocData;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;
import accesoya_backend.places.domain.repository.FlmNocDataRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FlmNocImporter {

    private static final Logger log = LoggerFactory.getLogger(FlmNocImporter.class);

    private static final String FILE_PATH = "data/flm/Base_6567 Sitios_FLM_NOC_Unificado050526vf_Software.xlsx";

    private static final String SHEET_NAME = "BD_6567 Sitios_NOC_Actualizada";

    private static final int BATCH_SIZE = 500;

    private static final Pattern DMS_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\D+"
                    + "(\\d+(?:\\.\\d+)?)\\D+"
                    + "(\\d+(?:\\.\\d+)?)\\D*"
                    + "([NSEWO])?",
            Pattern.CASE_INSENSITIVE);

    private final PlaceRepository placeRepository;

    private final FlmNocDataRepository flmNocDataRepository;

    // =====================================================
    // IMPORTACIÓN PRINCIPAL
    // =====================================================

    public void importData() {

        log.info("========================================");
        log.info("INICIANDO IMPORTACIÓN FLM/NOC");
        log.info("Archivo: {}", FILE_PATH);
        log.info("Hoja: {}", SHEET_NAME);
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        int totalRecords = 0;
        int processed = 0;
        int skipped = 0;
        int errors = 0;

        try {

            ClassPathResource resource = new ClassPathResource(FILE_PATH);

            try (
                    InputStream inputStream = resource.getInputStream();

                    Workbook workbook = WorkbookFactory.create(
                            inputStream)) {

                Sheet sheet = workbook.getSheet(
                        SHEET_NAME);

                if (sheet == null) {

                    throw new IllegalStateException(
                            "No se encontró la hoja: "
                                    + SHEET_NAME);
                }

                Iterator<Row> rows = sheet.iterator();

                if (!rows.hasNext()) {

                    throw new IllegalStateException(
                            "El Excel no contiene filas.");
                }

                Row headerRow = rows.next();

                Map<String, Integer> headers = buildHeaderMap(
                        headerRow);

                validateRequiredHeaders(
                        headers);

                log.info(
                        "Columnas encontradas: {}",
                        headers.keySet());

                while (rows.hasNext()) {

                    Row row = rows.next();

                    if (isEmptyRow(row)) {
                        continue;
                    }

                    totalRecords++;

                    try {

                        FlmNocRecord record = readRecord(
                                row,
                                headers);

                        Optional<Place> mappedPlace = mapRecord(
                                record);

                        if (mappedPlace.isEmpty()) {

                            skipped++;

                            continue;
                        }

                        Place incomingPlace = mappedPlace.get();

                        Place savedPlace = saveOrUpdatePlace(
                                incomingPlace);

                        saveOrUpdateFlmData(
                                savedPlace,
                                record);

                        processed++;

                        if (processed % BATCH_SIZE == 0) {

                            log.info(
                                    "Procesados: {} | Omitidos: {} | Errores: {}",
                                    processed,
                                    skipped,
                                    errors);
                        }

                    } catch (Exception exception) {

                        errors++;

                        log.error(
                                "Error procesando fila {}: {}",
                                row.getRowNum() + 1,
                                exception.getMessage());
                    }
                }
            }

        } catch (Exception exception) {

            log.error(
                    "Error durante la importación FLM/NOC",
                    exception);

            throw new IllegalStateException(
                    "No se pudo importar FLM/NOC",
                    exception);
        }

        long duration = System.currentTimeMillis()
                - startTime;

        log.info("========================================");
        log.info("IMPORTACIÓN FLM/NOC FINALIZADA");
        log.info("Total registros: {}", totalRecords);
        log.info("Procesados: {}", processed);
        log.info("Omitidos: {}", skipped);
        log.info("Errores: {}", errors);
        log.info("Tiempo: {} ms", duration);
        log.info("========================================");
    }

    // =====================================================
    // PLACE
    // =====================================================

    private Place saveOrUpdatePlace(
            Place incoming) {

        Optional<Place> existing = placeRepository
                .findByExternalIdAndSource(
                        incoming.getExternalId(),
                        PlaceSource.FLM_NOC);

        if (existing.isPresent()) {

            Place current = existing.get();

            updateExisting(
                    current,
                    incoming);

            return placeRepository.save(
                    current);
        }

        return placeRepository.save(
                incoming);
    }

    private void updateExisting(
            Place existing,
            Place incoming) {

        existing.setName(
                incoming.getName());

        existing.setDescription(
                incoming.getDescription());

        existing.setAddress(
                incoming.getAddress());

        existing.setDepartment(
                incoming.getDepartment());

        existing.setProvince(
                incoming.getProvince());

        existing.setDistrict(
                incoming.getDistrict());

        existing.setUbigeo(
                incoming.getUbigeo());

        existing.setLatitude(
                incoming.getLatitude());

        existing.setLongitude(
                incoming.getLongitude());

        existing.setType(
                incoming.getType());

        existing.setClassification(
                incoming.getClassification());

        existing.setEstablishmentType(
                incoming.getEstablishmentType());

        existing.setCategory(
                incoming.getCategory());

        existing.setSourceStatus(
                incoming.getSourceStatus());

        existing.setStatus(
                incoming.getStatus());
    }

    // =====================================================
    // FLM/NOC DATA
    // =====================================================

    private void saveOrUpdateFlmData(
            Place place,
            FlmNocRecord record) {

        FlmNocData data = flmNocDataRepository
                .findByPlaceId(
                        place.getId())
                .orElseGet(
                        FlmNocData::new);

        data.setPlace(
                place);

        data.setNombreEnCal(
                clean(
                        record.nombreEnCal()));

        data.setNombreControlCentral(
                clean(
                        record.nombreControlCentral()));

        data.setNumeroLineaComunicacion(
                clean(
                        record.numeroLineaComunicacion()));

        data.setLocalRecojoLlaves(
                clean(
                        record.localRecojoLlaves()));

        data.setCodigoEmplazamiento(
                clean(
                        record.codigoEmplazamiento()));

        data.setZonal(
                clean(
                        record.zonal()));

        data.setPropietarioTorre(
                clean(
                        record.propietarioTorre()));

        data.setClasificacionPropietarioTorre(
                clean(
                        record.clasificacionPropietarioTorre()));

        data.setCoberturaReaccion(
                clean(
                        record.coberturaReaccion()));

        data.setPatrullaje(
                clean(
                        record.patrullaje()));

        data.setGuardiania(
                clean(
                        record.guardiania()));

        data.setVigilancia(
                clean(
                        record.vigilancia()));

        data.setRondaDinamica(
                clean(
                        record.rondaDinamica()));

        data.setMonitoreoCsi(
                clean(
                        record.monitoreoCsi()));

        flmNocDataRepository.save(
                data);
    }

    // =====================================================
    // MAPEO EXCEL → PLACE
    // =====================================================

    private Optional<Place> mapRecord(
            FlmNocRecord record) {

        String externalId = firstNonBlank(
                record.nemonico(),
                record.codigoEmplazamiento());

        if (externalId == null) {
            return Optional.empty();
        }

        String name = firstNonBlank(
                record.nombreDelLocal(),
                record.nombreEnCal(),
                record.nombreControlCentral(),
                externalId);

        Double latitude = parseCoordinate(
                record.latitud(),
                true);

        Double longitude = parseCoordinate(
                record.longitud(),
                false);

        /*
         * Intento de corrección de coordenadas
         * invertidas.
         */
        if (!isValidLatitude(latitude)
                || !isValidLongitude(longitude)) {

            Double possibleLatitude = parseCoordinate(
                    record.longitud(),
                    true);

            Double possibleLongitude = parseCoordinate(
                    record.latitud(),
                    false);

            if (isValidLatitude(
                    possibleLatitude)
                    && isValidLongitude(
                            possibleLongitude)) {

                latitude = possibleLatitude;

                longitude = possibleLongitude;
            }
        }

        if (!isValidLatitude(latitude)
                || !isValidLongitude(longitude)) {

            return Optional.empty();
        }

        Place place = new Place();

        place.setExternalId(
                externalId);

        place.setSource(
                PlaceSource.FLM_NOC);

        place.setName(
                name);

        place.setDescription(
                clean(
                        record.nombreEnCal()));

        place.setAddress(
                clean(
                        record.direccion()));

        place.setDepartment(
                clean(
                        record.departamento()));

        place.setProvince(
                clean(
                        record.provincia()));

        place.setDistrict(
                clean(
                        record.distrito()));

        place.setUbigeo(
                clean(
                        record.ubigeo()));

        place.setLatitude(
                latitude);

        place.setLongitude(
                longitude);

        place.setType(
                PlaceType.TELECOMMUNICATION_SITE);

        place.setClassification(
                clean(
                        record.clasificacionPropietarioTorre()));

        place.setEstablishmentType(
                clean(
                        record.tipoEstacion()));

        place.setCategory(
                clean(
                        record.tecnologia()));

        place.setPhone(
                null);

        place.setOpeningHours(
                null);

        place.setSourceStatus(
                "ACTIVO");

        place.setStatus(
                PlaceStatus.ACTIVE);

        place.setImageUrl1(null);
        place.setImageUrl2(null);
        place.setImageUrl3(null);

        return Optional.of(place);
    }

    // =====================================================
    // LECTURA DEL EXCEL
    // =====================================================

    private FlmNocRecord readRecord(
            Row row,
            Map<String, Integer> headers) {

        return new FlmNocRecord(

                get(
                        row,
                        headers,
                        "NEMÓNICO"),

                get(
                        row,
                        headers,
                        "NOMBRE_EN_EL_CAL"),

                get(
                        row,
                        headers,
                        "NOMBRE_CONTROL_CENTRAL"),

                get(
                        row,
                        headers,
                        "NOMBRE_DEL_LOCAL"),

                get(
                        row,
                        headers,
                        "NO_LÍNEA_COMUNICACIÓN"),

                get(
                        row,
                        headers,
                        "LOCAL_RECOJO_LLAVES"),

                get(
                        row,
                        headers,
                        "CÓDIGO_EMPLAZAMIENTO"),

                get(
                        row,
                        headers,
                        "UBIGEO"),

                get(
                        row,
                        headers,
                        "ZONAL"),

                get(
                        row,
                        headers,
                        "DEPARTAMENTO"),

                get(
                        row,
                        headers,
                        "PROVINCIA"),

                get(
                        row,
                        headers,
                        "DISTRITO"),

                get(
                        row,
                        headers,
                        "DIRECCION"),

                get(
                        row,
                        headers,
                        "PROPIETARIO_DE_TORRE"),

                get(
                        row,
                        headers,
                        "CLASIFICACIÓN_PROPIETARIO_TORRE"),

                getFirstAvailable(
                        row,
                        headers,
                        "TIPO_ESTACIÓN_(original)",
                        "TIPO_ESTACIÓN"),

                get(
                        row,
                        headers,
                        "TECNOLOGÍA"),

                get(
                        row,
                        headers,
                        "LATITUD"),

                get(
                        row,
                        headers,
                        "LONGITUD"),

                get(
                        row,
                        headers,
                        "COBERTURA_REACCIÓN"),

                get(
                        row,
                        headers,
                        "PATRULLAJE"),

                get(
                        row,
                        headers,
                        "GUARDIANIA"),

                get(
                        row,
                        headers,
                        "VIGILANCIA"),

                get(
                        row,
                        headers,
                        "RONDA_DINAMICA"),

                get(
                        row,
                        headers,
                        "MONITOREO_CSI"));
    }

    // =====================================================
    // HEADERS
    // =====================================================

    private Map<String, Integer> buildHeaderMap(
            Row headerRow) {

        Map<String, Integer> headers = new HashMap<>();

        for (Cell cell : headerRow) {

            String header = getCellValue(cell);

            if (header == null) {
                continue;
            }

            headers.put(
                    normalizeHeader(header),
                    cell.getColumnIndex());
        }

        return headers;
    }

    private void validateRequiredHeaders(
            Map<String, Integer> headers) {

        String[] required = {

                "NEMÓNICO",
                "NOMBRE_DEL_LOCAL",
                "CÓDIGO_EMPLAZAMIENTO",
                "DEPARTAMENTO",
                "PROVINCIA",
                "DISTRITO",
                "DIRECCION",
                "LATITUD",
                "LONGITUD"
        };

        for (String requiredHeader : required) {

            if (!headers.containsKey(
                    normalizeHeader(requiredHeader))) {

                throw new IllegalStateException(
                        "Falta la columna requerida: "
                                + requiredHeader);
            }
        }
    }

    private String get(
            Row row,
            Map<String, Integer> headers,
            String name) {

        Integer index = headers.get(
                normalizeHeader(name));

        if (index == null) {
            return null;
        }

        return getCellValue(
                row.getCell(
                        index,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

    private String getFirstAvailable(
            Row row,
            Map<String, Integer> headers,
            String... names) {

        for (String name : names) {

            String value = get(
                    row,
                    headers,
                    name);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    // =====================================================
    // COORDENADAS
    // =====================================================

    private Double parseCoordinate(
            String value,
            boolean latitude) {

        String cleaned = clean(value);

        if (cleaned == null) {
            return null;
        }

        try {

            String normalized = cleaned
                    .trim()
                    .toUpperCase()
                    .replace(",", ".");

            /*
             * DMS
             *
             * Ejemplo:
             * 13°19'10.38"S
             */
            if (containsDmsNotation(
                    normalized)) {

                return parseDms(
                        normalized,
                        latitude);
            }

            /*
             * Decimal con N/S/E/W
             *
             * Ejemplo:
             * 11.233031381852925S
             */
            Matcher decimalDirection = Pattern.compile(
                    "^([+-]?\\d+(?:\\.\\d+)?)\\s*([NSEWO])$").matcher(normalized);

            if (decimalDirection.matches()) {

                double result = Double.parseDouble(
                        decimalDirection.group(1));

                String direction = decimalDirection.group(2);

                if ("S".equals(direction)
                        || "W".equals(direction)
                        || "O".equals(direction)) {

                    result = -Math.abs(result);
                }

                return normalizeScaledCoordinate(
                        result,
                        latitude);
            }

            /*
             * Decimal puro.
             */
            String decimal = normalized
                    .replace("°", "")
                    .trim();

            if (decimal.matches(
                    "[-+]?\\d+(?:\\.\\d+)?")) {

                double result = Double.parseDouble(
                        decimal);

                return normalizeScaledCoordinate(
                        result,
                        latitude);
            }

            return null;

        } catch (Exception exception) {

            return null;
        }
    }

    private Double parseDms(
            String value,
            boolean latitude) {

        Matcher matcher = DMS_PATTERN.matcher(
                value);

        if (!matcher.find()) {
            return null;
        }

        double degrees = Double.parseDouble(
                matcher.group(1));

        double minutes = Double.parseDouble(
                matcher.group(2));

        double seconds = Double.parseDouble(
                matcher.group(3));

        String direction = matcher.group(4);

        double result = degrees
                + minutes / 60.0
                + seconds / 3600.0;

        if ("S".equalsIgnoreCase(direction)
                || "W".equalsIgnoreCase(direction)
                || "O".equalsIgnoreCase(direction)) {

            result = -result;
        }

        return normalizeScaledCoordinate(
                result,
                latitude);
    }

    private Double normalizeScaledCoordinate(
            double value,
            boolean latitude) {

        /*
         * Coordenada ya válida.
         */
        if (isValidLatitude(value)
                && latitude) {

            return value;
        }

        if (isValidLongitude(value)
                && !latitude) {

            return value;
        }

        /*
         * Casos donde Excel tiene:
         *
         * -3750127
         *
         * en lugar de:
         *
         * -3.750127
         */
        double scaled = value / 1_000_000.0;

        if (latitude &&
                isValidLatitude(scaled)) {

            return scaled;
        }

        if (!latitude &&
                isValidLongitude(scaled)) {

            return scaled;
        }

        /*
         * Segundo intento para valores
         * escalados con mayor precisión.
         */
        scaled = value / 100_000_000.0;

        if (latitude &&
                isValidLatitude(scaled)) {

            return scaled;
        }

        if (!latitude &&
                isValidLongitude(scaled)) {

            return scaled;
        }

        return null;
    }

    private boolean containsDmsNotation(
            String value) {

        return value.contains("'")
                || value.contains("\"")
                || value.matches(
                        ".*\\d+\\s*[°].*");
    }

    private boolean isValidLatitude(
            Double value) {

        if (value == null) {
            return false;
        }

        return value >= -19
                && value <= 1;
    }

    private boolean isValidLongitude(
            Double value) {

        if (value == null) {
            return false;
        }

        return value >= -82
                && value <= -68;
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    private String getCellValue(
            Cell cell) {

        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter(
                Locale.US);

        String value = formatter.formatCellValue(
                cell);

        return clean(value);
    }

    private String normalizeHeader(
            String value) {

        return value
                .trim()
                .toUpperCase()
                .replace(
                        '\u00A0',
                        ' ');
    }

    private String clean(
            String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    private String firstNonBlank(
            String... values) {

        for (String value : values) {

            String cleaned = clean(value);

            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

    private boolean isEmptyRow(
            Row row) {

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {

            if (i < 0) {
                continue;
            }

            if (getCellValue(
                    row.getCell(i)) != null) {

                return false;
            }
        }

        return true;
    }
}