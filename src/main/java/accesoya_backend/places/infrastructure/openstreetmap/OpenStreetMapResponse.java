package accesoya_backend.places.infrastructure.openstreetmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenStreetMapResponse(
        List<OpenStreetMapElement> elements) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenStreetMapElement(
            String type,
            Long id,
            Double lat,
            Double lon,
            Center center,
            Map<String, String> tags) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Center(
            Double lat,
            Double lon) {
    }
}