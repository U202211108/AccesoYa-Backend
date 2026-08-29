package accesoya_backend.places.application.dto;

import java.util.List;

public record FlmNocFilterResponse(

        List<String> tiposEstacion,

        List<String> zonales,

        List<String> tecnologias

) {
}