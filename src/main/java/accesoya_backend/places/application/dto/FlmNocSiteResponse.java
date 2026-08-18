package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.FlmNocData;
import accesoya_backend.places.domain.model.Place;

import java.util.UUID;

public record FlmNocSiteResponse(

        // =====================================================
        // PLACE
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
        // FLM / NOC
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

    public static FlmNocSiteResponse from(
            Place place) {

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

                flm != null
                        ? flm.getNombreEnCal()
                        : null,

                flm != null
                        ? flm.getNombreControlCentral()
                        : null,

                flm != null
                        ? flm.getNumeroLineaComunicacion()
                        : null,

                flm != null
                        ? flm.getLocalRecojoLlaves()
                        : null,

                flm != null
                        ? flm.getCodigoEmplazamiento()
                        : null,

                flm != null
                        ? flm.getZonal()
                        : null,

                flm != null
                        ? flm.getPropietarioTorre()
                        : null,

                flm != null
                        ? flm.getClasificacionPropietarioTorre()
                        : null,

                flm != null
                        ? flm.getCoberturaReaccion()
                        : null,

                flm != null
                        ? flm.getPatrullaje()
                        : null,

                flm != null
                        ? flm.getGuardiania()
                        : null,

                flm != null
                        ? flm.getVigilancia()
                        : null,

                flm != null
                        ? flm.getRondaDinamica()
                        : null,

                flm != null
                        ? flm.getMonitoreoCsi()
                        : null);
    }
}