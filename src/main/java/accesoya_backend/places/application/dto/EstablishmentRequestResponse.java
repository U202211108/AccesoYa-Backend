package accesoya_backend.places.application.dto;

import accesoya_backend.places.domain.model.EstablishmentRequest;
import accesoya_backend.places.domain.model.EstablishmentRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record EstablishmentRequestResponse(

                UUID id,

                UUID userId,

                String userName,

                String userEmail,

                UUID placeId,

                String businessName,

                String businessAddress,

                String businessPhone,

                String businessType,

                String description,

                EstablishmentRequestStatus status,

                UUID reviewedBy,

                String reviewComment,

                Instant reviewedAt,

                Instant createdAt,

                Instant updatedAt

) {

        public static EstablishmentRequestResponse from(
                        EstablishmentRequest request) {

                return new EstablishmentRequestResponse(

                                request.getId(),

                                request.getUser().getId(),

                                request.getUser().getFirstName()
                                                + " "
                                                + request.getUser().getLastName(),

                                request.getUser().getEmail(),

                                request.getPlace().getId(),

                                request.getBusinessName(),

                                request.getBusinessAddress(),

                                request.getBusinessPhone(),

                                request.getBusinessType(),

                                request.getDescription(),

                                request.getStatus(),

                                request.getReviewedBy() != null
                                                ? request.getReviewedBy().getId()
                                                : null,

                                request.getReviewComment(),

                                request.getReviewedAt(),

                                request.getCreatedAt(),

                                request.getUpdatedAt());
        }
}