package accesoya_backend.places.infrastructure.openstreetmap;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenStreetMapImporter {

    private static final Logger log = LoggerFactory.getLogger(OpenStreetMapImporter.class);

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    /*
     * Lima Metropolitana aproximada.
     *
     * south, west, north, east
     */
    private static final double MIN_LATITUDE = -12.35;
    private static final double MIN_LONGITUDE = -77.20;
    private static final double MAX_LATITUDE = -11.70;
    private static final double MAX_LONGITUDE = -76.60;

    private final RestClient restClient = RestClient.builder()
            .baseUrl(OVERPASS_URL)
            .build();

    private final OpenStreetMapBatchWriter batchWriter;

    // =====================================================
    // IMPORTACIÓN
    // =====================================================

    public void importData() {

        log.info("========================================");
        log.info("INICIANDO IMPORTACIÓN OPENSTREETMAP");
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        String query = buildQuery();

        try {

            OpenStreetMapResponse response = restClient
                    .post()
                    .contentType(
                            MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                            "data=" + encodeQuery(query))
                    .retrieve()
                    .body(OpenStreetMapResponse.class);

            if (response == null ||
                    response.elements() == null) {

                log.warn(
                        "OpenStreetMap no devolvió elementos.");

                return;
            }

            List<Place> places = new ArrayList<>();

            int skipped = 0;

            for (OpenStreetMapResponse.OpenStreetMapElement element : response.elements()) {

                try {

                    Place place = mapElement(element);

                    if (place == null) {

                        skipped++;

                        continue;
                    }

                    places.add(place);

                } catch (Exception exception) {

                    skipped++;

                    log.warn(
                            "No se pudo procesar OSM {}: {}",
                            element.id(),
                            exception.getMessage());
                }
            }

            int processed = batchWriter.saveBatch(places);

            long duration = System.currentTimeMillis()
                    - startTime;

            log.info("========================================");
            log.info(
                    "IMPORTACIÓN OPENSTREETMAP FINALIZADA");
            log.info(
                    "Elementos recibidos: {}",
                    response.elements().size());
            log.info(
                    "Procesados: {}",
                    processed);
            log.info(
                    "Omitidos: {}",
                    skipped);
            log.info(
                    "Tiempo: {} ms",
                    duration);
            log.info("========================================");

        } catch (Exception exception) {

            log.error(
                    "Error durante la importación OpenStreetMap",
                    exception);

            throw new IllegalStateException(
                    "No se pudo importar OpenStreetMap",
                    exception);
        }
    }

    // =====================================================
    // OVERPASS QUERY
    // =====================================================

    private String buildQuery() {

        String bbox = String.format(
                "%s,%s,%s,%s",
                MIN_LATITUDE,
                MIN_LONGITUDE,
                MAX_LATITUDE,
                MAX_LONGITUDE);

        return """
                [out:json][timeout:120];

                (
                  nwr["amenity"="hospital"](%s);
                  nwr["amenity"="clinic"](%s);
                  nwr["amenity"="doctors"](%s);
                  nwr["amenity"="dentist"](%s);

                  nwr["amenity"="restaurant"](%s);
                  nwr["amenity"="fast_food"](%s);
                  nwr["amenity"="cafe"](%s);

                  nwr["amenity"="pharmacy"](%s);

                  nwr["amenity"="bank"](%s);

                  nwr["amenity"="school"](%s);
                  nwr["amenity"="university"](%s);
                  nwr["amenity"="college"](%s);

                  nwr["tourism"="hotel"](%s);
                  nwr["tourism"="guest_house"](%s);
                  nwr["tourism"="hostel"](%s);

                  nwr["shop"="supermarket"](%s);

                  nwr["shop"="mall"](%s);
                  nwr["shop"="department_store"](%s);

                  nwr["leisure"="sports_centre"](%s);
                  nwr["leisure"="stadium"](%s);
                  nwr["leisure"="swimming_pool"](%s);

                  nwr["amenity"="place_of_worship"](%s);

                  nwr["amenity"="library"](%s);
                  nwr["tourism"="museum"](%s);
                  nwr["amenity"="theatre"](%s);
                  nwr["amenity"="arts_centre"](%s);

                  nwr["amenity"="townhall"](%s);
                  nwr["amenity"="post_office"](%s);
                  nwr["amenity"="police"](%s);
                  nwr["amenity"="fire_station"](%s);

                  nwr["amenity"="bus_station"](%s);
                  nwr["amenity"="ferry_terminal"](%s);
                  nwr["railway"="station"](%s);
                );

                out center;
                """.formatted(
                bbox, bbox, bbox, bbox,
                bbox, bbox, bbox,
                bbox,
                bbox,
                bbox, bbox, bbox,
                bbox, bbox, bbox,
                bbox,
                bbox, bbox,
                bbox, bbox, bbox,
                bbox,
                bbox, bbox, bbox, bbox,
                bbox, bbox, bbox, bbox,
                bbox, bbox, bbox);
    }

    /*
     * Bounding box reutilizable.
     *
     * Overpass:
     * south, west, north, east
     */
    private static final String BBOX_VALUES = "%s,%s,%s,%s".formatted(
            MIN_LATITUDE,
            MIN_LONGITUDE,
            MAX_LATITUDE,
            MAX_LONGITUDE);

    // =====================================================
    // MAPPING
    // =====================================================

    private Place mapElement(
            OpenStreetMapResponse.OpenStreetMapElement element) {

        Map<String, String> tags = element.tags();

        if (tags == null ||
                tags.isEmpty()) {

            return null;
        }

        String name = clean(tags.get("name"));

        if (name == null) {

            return null;
        }

        Double latitude = getLatitude(element);

        Double longitude = getLongitude(element);

        if (latitude == null ||
                longitude == null) {

            return null;
        }

        PlaceType type = mapPlaceType(tags);

        if (type == null) {

            return null;
        }

        Place place = new Place();

        place.setExternalId(
                "OSM-" +
                        element.type() +
                        "-" +
                        element.id());

        place.setSource(
                PlaceSource.OPENSTREETMAP);

        place.setName(name);

        place.setDescription(
                clean(
                        tags.get("description")));

        place.setAddress(
                buildAddress(tags));

        place.setDepartment("Lima");

        place.setProvince("Lima");

        place.setDistrict(
                firstNonBlank(
                        tags.get("addr:district"),
                        tags.get("addr:suburb")));

        place.setUbigeo(null);

        place.setLatitude(latitude);

        place.setLongitude(longitude);

        place.setType(type);

        place.setClassification(
                getClassification(tags));

        place.setEstablishmentType(
                firstNonBlank(
                        tags.get("shop"),
                        tags.get("tourism"),
                        tags.get("leisure")));

        place.setCategory(
                getCategory(tags));

        place.setPhone(
                firstNonBlank(
                        tags.get("phone"),
                        tags.get("contact:phone")));

        place.setOpeningHours(
                clean(
                        tags.get("opening_hours")));

        place.setSourceStatus(
                "ACTIVE");

        place.setStatus(
                PlaceStatus.ACTIVE);

        place.setImageUrl1(null);
        place.setImageUrl2(null);
        place.setImageUrl3(null);

        return place;
    }

    // =====================================================
    // PLACE TYPE
    // =====================================================

    private PlaceType mapPlaceType(
            Map<String, String> tags) {

        String amenity = clean(tags.get("amenity"));

        String shop = clean(tags.get("shop"));

        String tourism = clean(tags.get("tourism"));

        String leisure = clean(tags.get("leisure"));

        String railway = clean(tags.get("railway"));

        // -----------------------------------------
        // SALUD
        // -----------------------------------------

        if (equalsAny(
                amenity,
                "hospital",
                "clinic",
                "doctors",
                "dentist")) {

            return PlaceType.HEALTHCARE;
        }

        // -----------------------------------------
        // FARMACIA
        // -----------------------------------------

        if ("pharmacy".equalsIgnoreCase(
                amenity)) {

            return PlaceType.PHARMACY;
        }

        // -----------------------------------------
        // RESTAURANTES
        // -----------------------------------------

        if (equalsAny(
                amenity,
                "restaurant",
                "fast_food",
                "cafe")) {

            return PlaceType.RESTAURANT;
        }

        // -----------------------------------------
        // BANCOS
        // -----------------------------------------

        if ("bank".equalsIgnoreCase(
                amenity)) {

            return PlaceType.BANK;
        }

        // -----------------------------------------
        // EDUCACIÓN
        // -----------------------------------------

        if ("university".equalsIgnoreCase(
                amenity)) {

            return PlaceType.UNIVERSITY;
        }

        if (equalsAny(
                amenity,
                "school",
                "college")) {

            return PlaceType.SCHOOL;
        }

        // -----------------------------------------
        // HOTELES
        // -----------------------------------------

        if (equalsAny(
                tourism,
                "hotel",
                "guest_house",
                "hostel")) {

            return PlaceType.HOTEL;
        }

        // -----------------------------------------
        // SUPERMERCADOS
        // -----------------------------------------

        if ("supermarket".equalsIgnoreCase(
                shop)) {

            return PlaceType.SUPERMARKET;
        }

        // -----------------------------------------
        // CENTROS COMERCIALES
        // -----------------------------------------

        if (equalsAny(
                shop,
                "mall",
                "department_store")) {

            return PlaceType.SHOPPING_CENTER;
        }

        // -----------------------------------------
        // DEPORTES
        // -----------------------------------------

        if (equalsAny(
                leisure,
                "sports_centre",
                "stadium",
                "swimming_pool")) {

            return PlaceType.SPORTS;
        }

        // -----------------------------------------
        // RELIGIOSOS
        // -----------------------------------------

        if ("place_of_worship".equalsIgnoreCase(
                amenity)) {

            return PlaceType.RELIGIOUS;
        }

        // -----------------------------------------
        // CULTURA
        // -----------------------------------------

        if (equalsAny(
                amenity,
                "library",
                "theatre",
                "arts_centre")) {

            return PlaceType.CULTURAL;
        }

        if ("museum".equalsIgnoreCase(
                tourism)) {

            return PlaceType.CULTURAL;
        }

        // -----------------------------------------
        // SERVICIOS PÚBLICOS
        // -----------------------------------------

        if (equalsAny(
                amenity,
                "townhall",
                "post_office",
                "police",
                "fire_station")) {

            return PlaceType.PUBLIC_SERVICE;
        }

        // -----------------------------------------
        // TRANSPORTE
        // -----------------------------------------

        if (equalsAny(
                amenity,
                "bus_station",
                "ferry_terminal")) {

            return PlaceType.TRANSPORTATION;
        }

        if ("station".equalsIgnoreCase(
                railway)) {

            return PlaceType.TRANSPORTATION;
        }

        return PlaceType.OTHER;
    }

    // =====================================================
    // CATEGORY
    // =====================================================

    private String getCategory(
            Map<String, String> tags) {

        return firstNonBlank(
                tags.get("amenity"),
                tags.get("shop"),
                tags.get("tourism"),
                tags.get("leisure"),
                tags.get("railway"));
    }

    private String getClassification(
            Map<String, String> tags) {

        return firstNonBlank(
                tags.get("amenity"),
                tags.get("shop"),
                tags.get("tourism"),
                tags.get("leisure"),
                tags.get("railway"));
    }

    // =====================================================
    // COORDINATES
    // =====================================================

    private Double getLatitude(
            OpenStreetMapResponse.OpenStreetMapElement element) {

        if (element.lat() != null) {

            return element.lat();
        }

        if (element.center() != null) {

            return element.center().lat();
        }

        return null;
    }

    private Double getLongitude(
            OpenStreetMapResponse.OpenStreetMapElement element) {

        if (element.lon() != null) {

            return element.lon();
        }

        if (element.center() != null) {

            return element.center().lon();
        }

        return null;
    }

    // =====================================================
    // ADDRESS
    // =====================================================

    private String buildAddress(
            Map<String, String> tags) {

        String street = clean(tags.get("addr:street"));

        String houseNumber = clean(tags.get("addr:housenumber"));

        if (street == null &&
                houseNumber == null) {

            return null;
        }

        if (street == null) {

            return houseNumber;
        }

        if (houseNumber == null) {

            return street;
        }

        return street +
                " " +
                houseNumber;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean equalsAny(
            String value,
            String... options) {

        if (value == null) {

            return false;
        }

        for (String option : options) {

            if (option.equalsIgnoreCase(
                    value)) {

                return true;
            }
        }

        return false;
    }

    private String firstNonBlank(
            String first,
            String second) {

        String value = clean(first);

        if (value != null) {

            return value;
        }

        return clean(second);
    }

    private String firstNonBlank(
            String first,
            String second,
            String third) {

        String value = clean(first);

        if (value != null) {

            return value;
        }

        value = clean(second);

        if (value != null) {

            return value;
        }

        return clean(third);
    }

    private String firstNonBlank(
            String first,
            String second,
            String third,
            String fourth,
            String fifth) {

        String[] values = {
                first,
                second,
                third,
                fourth,
                fifth
        };

        for (String value : values) {

            String cleaned = clean(value);

            if (cleaned != null) {

                return cleaned;
            }
        }

        return null;
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

    private String encodeQuery(
            String query) {

        return URLEncoder.encode(
                query,
                StandardCharsets.UTF_8);
    }
}