package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.model.PlaceSource;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.model.PlaceType;

import java.util.UUID;

public record PlaceMapResponse(

                UUID id,

                String externalId,

                String name,

                Double latitude,

                Double longitude,

                PlaceType type,

                PlaceStatus status,

                PlaceSource source,

                String address,

                String department,

                String province,

                String district,

                String ubigeo,

                String phone,

                String openingHours,

                String category,

                String description,

                String establishmentType,

                FlmNocResponse flmNoc

) {

        // =====================================================
        // CONVERSIÓN PLACE → RESPUESTA DEL MAPA
        // =====================================================

        public static PlaceMapResponse from(
                        Place place,
                        boolean includeOperationalData) {

                FlmNocResponse flmNoc = null;

                // =================================================
                // INFORMACIÓN OPERACIONAL FLM / NOC
                // =================================================
                //
                // CONSULTOR:
                // flmNoc = null
                //
                // OPERADOR_FLNOC / SUPERVISOR / ADMIN:
                // se incluye la información FLM/NOC cuando existe.
                // =================================================

                if (includeOperationalData
                                && place.getFlmNocData() != null) {

                        flmNoc = FlmNocResponse.from(
                                        place.getFlmNocData());
                }

                return new PlaceMapResponse(

                                // =================================================
                                // INFORMACIÓN DEL PLACE
                                // =================================================

                                place.getId(),

                                place.getExternalId(),

                                place.getName(),

                                place.getLatitude(),

                                place.getLongitude(),

                                place.getType(),

                                place.getStatus(),

                                place.getSource(),

                                place.getAddress(),

                                place.getDepartment(),

                                place.getProvince(),

                                place.getDistrict(),

                                place.getUbigeo(),

                                place.getPhone(),

                                place.getOpeningHours(),

                                place.getCategory(),

                                place.getDescription(),

                                place.getEstablishmentType(),

                                // =================================================
                                // INFORMACIÓN OPERACIONAL
                                // =================================================

                                flmNoc);
        }
}