package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.FlmNocData;
import accesoya_backend.places.domain.model.Place;

import java.util.UUID;

public record FlmNocSiteResponse(

                // =====================================================
                // PLACE - INFORMACIÓN GENERAL
                // =====================================================

                UUID id,

                String externalId,

                String name,

                String address,

                String department,

                String province,

                String district,

                String ubigeo,

                Double latitude,

                Double longitude,

                String category,

                String classification,

                String establishmentType,

                String sourceStatus,

                String status,

                // =====================================================
                // FLM / NOC - INFORMACIÓN OPERATIVA
                // =====================================================

                String nombreEnCal,

                String nombreControlCentral,

                String numeroLineaComunicacion,

                String localRecojoLlaves,

                String codigoEmplazamiento,

                String zonal,

                String propietarioTorre,

                String clasificacionPropietarioTorre,

                String coberturaReaccion,

                String patrullaje,

                String guardiania,

                String vigilancia,

                String rondaDinamica,

                String monitoreoCsi

) {

        // =====================================================
        // CONVERSIÓN SEGURA
        // =====================================================

        public static FlmNocSiteResponse from(
                        Place place,
                        boolean includeOperationalData) {

                if (place == null) {
                        return null;
                }

                FlmNocData flm = place.getFlmNocData();

                return new FlmNocSiteResponse(

                                // =================================================
                                // PLACE
                                // =================================================

                                place.getId(),

                                place.getExternalId(),

                                place.getName(),

                                place.getAddress(),

                                place.getDepartment(),

                                place.getProvince(),

                                place.getDistrict(),

                                place.getUbigeo(),

                                place.getLatitude(),

                                place.getLongitude(),

                                place.getCategory(),

                                place.getClassification(),

                                place.getEstablishmentType(),

                                place.getSourceStatus(),

                                place.getStatus() != null
                                                ? place.getStatus().name()
                                                : null,

                                // =================================================
                                // FLM / NOC
                                // =================================================
                                // Si el usuario no tiene permisos operativos,
                                // TODOS estos campos permanecen en null.

                                includeOperationalData && flm != null
                                                ? flm.getNombreEnCal()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getNombreControlCentral()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getNumeroLineaComunicacion()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getLocalRecojoLlaves()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getCodigoEmplazamiento()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getZonal()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getPropietarioTorre()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getClasificacionPropietarioTorre()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getCoberturaReaccion()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getPatrullaje()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getGuardiania()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getVigilancia()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getRondaDinamica()
                                                : null,

                                includeOperationalData && flm != null
                                                ? flm.getMonitoreoCsi()
                                                : null);
        }

        // =====================================================
        // COMPATIBILIDAD
        // =====================================================

        public static FlmNocSiteResponse from(
                        Place place) {

                return from(place, true);
        }
}