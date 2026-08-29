package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.FlmNocData;

public record FlmNocResponse(

        String nombreEnCal,

        String nombreControlCentral,

        String numeroLineaComunicacion,

        String localRecojoLlaves,

        String codigoEmplazamiento,

        String zonal,

        String tipoEstacion,

        String tecnologia,

        String propietarioTorre,

        String clasificacionPropietarioTorre,

        String coberturaReaccion,

        String patrullaje,

        String guardiania,

        String vigilancia,

        String rondaDinamica,

        String monitoreoCsi

) {

    public static FlmNocResponse from(
            FlmNocData data) {

        if (data == null) {
            return null;
        }

        return new FlmNocResponse(

                data.getNombreEnCal(),

                data.getNombreControlCentral(),

                data.getNumeroLineaComunicacion(),

                data.getLocalRecojoLlaves(),

                data.getCodigoEmplazamiento(),

                data.getZonal(),

                data.getTipoEstacion(),

                data.getTecnologia(),

                data.getPropietarioTorre(),

                data.getClasificacionPropietarioTorre(),

                data.getCoberturaReaccion(),

                data.getPatrullaje(),

                data.getGuardiania(),

                data.getVigilancia(),

                data.getRondaDinamica(),

                data.getMonitoreoCsi());
    }
}