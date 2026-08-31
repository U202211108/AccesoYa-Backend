package accesoya_backend.places.interfaces.rest;

import accesoya_backend.places.application.dto.PlanDocumentResponse;
import accesoya_backend.places.application.service.PlanDocumentService;
import accesoya_backend.places.domain.model.PlanDocument;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/places/{placeId}/plans")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Gestión de planos de sitios FLM/NOC")
@SecurityRequirement(name = "bearerAuth")
public class PlanDocumentController {

        private final PlanDocumentService planDocumentService;

        // =====================================================
        // LISTAR PLANOS
        // OPERADOR + SUPERVISOR + ADMIN
        // =====================================================

        @GetMapping
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        @Operation(summary = "Obtener planos de un sitio")
        public ResponseEntity<List<PlanDocumentResponse>> getPlans(

                        @PathVariable UUID placeId

        ) {

                return ResponseEntity.ok(
                                planDocumentService.getPlans(
                                                placeId));
        }

        // =====================================================
        // SUBIR PLANO
        // OPERADOR + SUPERVISOR + ADMIN
        // =====================================================

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        @Operation(summary = "Subir un plano")
        public ResponseEntity<PlanDocumentResponse> uploadPlan(

                        @PathVariable UUID placeId,

                        @Parameter(description = "Archivo PDF, PNG o JPEG") @RequestParam("file") MultipartFile file,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(

                                planDocumentService.uploadPlan(

                                                placeId,

                                                file,

                                                authentication));
        }

        // =====================================================
        // VISUALIZAR / DESCARGAR
        // OPERADOR + SUPERVISOR + ADMIN
        // =====================================================

        @GetMapping("/{planId}")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        @Operation(summary = "Obtener un plano")
        public ResponseEntity<byte[]> getPlan(

                        @PathVariable UUID placeId,

                        @PathVariable UUID planId

        ) {

                PlanDocument document = planDocumentService.getPlan(
                                placeId,
                                planId);

                MediaType mediaType;

                try {

                        mediaType = MediaType.parseMediaType(
                                        document.getContentType());

                } catch (Exception exception) {

                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                return ResponseEntity.ok()

                                .contentType(mediaType)

                                .contentLength(
                                                document.getFileSize())

                                .header(
                                                HttpHeaders.CONTENT_DISPOSITION,
                                                "inline; filename=\"" +
                                                                document.getFileName() +
                                                                "\"")

                                .body(
                                                document.getFileData());
        }

        // =====================================================
        // VISTA PREVIA
        // OPERADOR + SUPERVISOR + ADMIN
        // =====================================================

        @GetMapping("/{planId}/preview")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        @Operation(summary = "Generar vista previa de un plano", description = """
                        Convierte temporalmente documentos Word
                        y Excel a PDF para visualización.
                        """)
        public ResponseEntity<byte[]> previewPlan(

                        @PathVariable UUID placeId,

                        @PathVariable UUID planId,

                        Authentication authentication

        ) {

                byte[] pdf = planDocumentService.generatePreview(
                                placeId,
                                planId);

                return ResponseEntity.ok()

                                .contentType(
                                                MediaType.APPLICATION_PDF)

                                .contentLength(
                                                pdf.length)

                                .header(
                                                HttpHeaders.CONTENT_DISPOSITION,
                                                "inline; filename=\"preview.pdf\"")

                                .body(pdf);
        }

        // =====================================================
        // ELIMINAR
        // OPERADOR + SUPERVISOR + ADMIN
        // =====================================================

        @DeleteMapping("/{planId}")
        @PreAuthorize("""
                        hasAnyRole(
                                'OPERADOR_FLNOC',
                                'SUPERVISOR',
                                'ADMIN'
                        )
                        """)
        @Operation(summary = "Eliminar un plano")
        public ResponseEntity<Void> deletePlan(

                        @PathVariable UUID placeId,

                        @PathVariable UUID planId,

                        Authentication authentication

        ) {

                planDocumentService.deletePlan(

                                placeId,

                                planId,

                                authentication);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}