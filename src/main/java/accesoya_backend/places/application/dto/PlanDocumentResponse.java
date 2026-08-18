package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.PlanDocument;

import java.time.Instant;
import java.util.UUID;

public record PlanDocumentResponse(

        UUID id,

        UUID placeId,

        String fileName,

        String contentType,

        long fileSize,

        Instant uploadedAt,

        UUID uploadedBy

) {

    public static PlanDocumentResponse from(
            PlanDocument document) {

        return new PlanDocumentResponse(

                document.getId(),

                document.getPlace().getId(),

                document.getFileName(),

                document.getContentType(),

                document.getFileSize(),

                document.getUploadedAt(),

                document.getUploadedBy() != null
                        ? document.getUploadedBy().getId()
                        : null);
    }
}