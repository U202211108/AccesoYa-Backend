package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.User;
import accesoya_backend.places.application.dto.PlanDocumentResponse;
import accesoya_backend.places.domain.model.PlanDocument;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.repository.PlanDocumentRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanDocumentService {

    private final PlanDocumentRepository planDocumentRepository;

    private final PlaceRepository placeRepository;

    // =====================================================
    // CONFIGURACIÓN
    // =====================================================

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg");

    // =====================================================
    // OBTENER PLANOS DE UN SITIO
    // =====================================================

    @Transactional(readOnly = true)
    public List<PlanDocumentResponse> getPlans(
            UUID placeId) {

        validatePlaceExists(placeId);

        return planDocumentRepository
                .findByPlaceIdOrderByUploadedAtDesc(placeId)
                .stream()
                .map(PlanDocumentResponse::from)
                .toList();
    }

    // =====================================================
    // SUBIR PLANO
    // =====================================================

    @Transactional
    public PlanDocumentResponse uploadPlan(

            UUID placeId,

            MultipartFile file,

            Authentication authentication) {

        // -------------------------------------------------
        // VALIDAR AUTENTICACIÓN
        // -------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "No se pudo identificar al usuario autenticado");
        }

        // -------------------------------------------------
        // OBTENER USUARIO AUTENTICADO
        // -------------------------------------------------

        if (!(authentication.getPrincipal() instanceof User)) {

            throw new AccessDeniedException(
                    "El usuario autenticado no es válido");
        }

        User authenticatedUser = (User) authentication.getPrincipal();

        // -------------------------------------------------
        // VALIDAR SITIO
        // -------------------------------------------------

        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el sitio con id: "
                                + placeId));

        // -------------------------------------------------
        // VALIDAR ARCHIVO
        // -------------------------------------------------

        validateFile(file);

        // -------------------------------------------------
        // CREAR DOCUMENTO
        // -------------------------------------------------

        try {

            PlanDocument document = PlanDocument.builder()

                    .place(place)

                    .fileName(
                            normalizeFileName(
                                    file.getOriginalFilename()))

                    .contentType(
                            file.getContentType())

                    .fileSize(
                            file.getSize())

                    .fileData(
                            file.getBytes())

                    .uploadedBy(
                            authenticatedUser)

                    .build();

            // -------------------------------------------------
            // GUARDAR
            // -------------------------------------------------

            PlanDocument savedDocument = planDocumentRepository.save(
                    document);

            return PlanDocumentResponse.from(
                    savedDocument);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "No se pudo leer el archivo",
                    exception);
        }
    }

    // =====================================================
    // VALIDACIÓN DEL ARCHIVO
    // =====================================================

    private void validateFile(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "El archivo es obligatorio");
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "El archivo supera el tamaño máximo permitido de 20 MB");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase())) {

            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. " +
                            "Solo se permiten PDF, PNG y JPEG");
        }
    }

    // =====================================================
    // NORMALIZAR NOMBRE
    // =====================================================

    private String normalizeFileName(
            String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return "plano";
        }

        String normalized = fileName.trim();

        // Evita nombres excesivamente largos

        if (normalized.length() > 255) {

            return normalized.substring(
                    0,
                    255);
        }

        return normalized;
    }

    // =====================================================
    // VERIFICAR SITIO
    // =====================================================

    private void validatePlaceExists(
            UUID placeId) {

        if (placeId == null) {

            throw new IllegalArgumentException(
                    "El identificador del sitio es obligatorio");
        }

        if (!placeRepository.existsById(placeId)) {

            throw new IllegalArgumentException(
                    "No se encontró el sitio con id: "
                            + placeId);
        }
    }

    // =====================================================
    // OBTENER ARCHIVO
    // =====================================================

    @Transactional(readOnly = true)
    public PlanDocument getPlan(
            UUID placeId,
            UUID planId) {

        return planDocumentRepository
                .findByIdAndPlaceId(
                        planId,
                        placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el plano solicitado"));
    }

    // =====================================================
    // ELIMINAR
    // =====================================================

    @Transactional
    public void deletePlan(

            UUID placeId,

            UUID planId,

            Authentication authentication) {

        // -------------------------------------------------
        // VALIDAR AUTENTICACIÓN
        // -------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "No se pudo identificar al usuario autenticado");
        }

        // -------------------------------------------------
        // OBTENER USUARIO AUTENTICADO
        // -------------------------------------------------

        if (!(authentication.getPrincipal() instanceof User)) {

            throw new AccessDeniedException(
                    "El usuario autenticado no es válido");
        }

        User authenticatedUser = (User) authentication.getPrincipal();

        // -------------------------------------------------
        // BUSCAR PLANO
        // -------------------------------------------------

        PlanDocument document = planDocumentRepository
                .findByIdAndPlaceId(
                        planId,
                        placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el plano solicitado"));

        // -------------------------------------------------
        // ELIMINAR
        // -------------------------------------------------

        planDocumentRepository.delete(
                document);
    }
}