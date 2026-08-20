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
        // =====================================================

        @GetMapping
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
        // =====================================================

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Subir un plano")
        public ResponseEntity<PlanDocumentResponse> uploadPlan(

                        @PathVariable UUID placeId,

                        @Parameter(description = "Archivo PDF, PNG o JPEG") @RequestParam("file") MultipartFile file,

                        Authentication authentication

        ) {

                System.out.println(
                                "========================================");

                System.out.println(
                                "UPLOAD PLAN");

                System.out.println(
                                "Usuario autenticado: "
                                                + authentication.getName());

                System.out.println(
                                "Principal: "
                                                + authentication.getPrincipal());

                System.out.println(
                                "Authorities: "
                                                + authentication.getAuthorities());

                System.out.println(
                                "========================================");

                return ResponseEntity.ok(

                                planDocumentService.uploadPlan(

                                                placeId,

                                                file,

                                                authentication));
        }

        // =====================================================
        // VISUALIZAR / DESCARGAR
        // =====================================================

        @GetMapping("/{planId}")
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

                                .contentType(
                                                mediaType)

                                .contentLength(
                                                document.getFileSize())

                                .header(

                                                HttpHeaders.CONTENT_DISPOSITION,

                                                "inline; filename=\"" +
                                                                document.getFileName() +
                                                                "\""

                                )

                                .body(
                                                document.getFileData());
        }

        // =====================================================
        // VISTA PREVIA
        // =====================================================

        @GetMapping("/{planId}/preview")
        @Operation(summary = "Generar vista previa de un plano", description = "Convierte temporalmente documentos Word y Excel a PDF para visualización")
        public ResponseEntity<byte[]> previewPlan(

                        @PathVariable UUID placeId,

                        @PathVariable UUID planId,

                        Authentication authentication

        ) {

                System.out.println("========================================");
                System.out.println("PREVIEW PLAN");
                System.out.println("Usuario autenticado: "
                                + (authentication != null
                                                ? authentication.getName()
                                                : "NULL"));

                System.out.println("Authenticated: "
                                + (authentication != null
                                                && authentication.isAuthenticated()));

                System.out.println("Principal: "
                                + (authentication != null
                                                ? authentication.getPrincipal()
                                                : "NULL"));

                System.out.println("Authorities: "
                                + (authentication != null
                                                ? authentication.getAuthorities()
                                                : "NULL"));

                System.out.println("Place: " + placeId);
                System.out.println("Plan: " + planId);
                System.out.println("========================================");

                byte[] pdf = planDocumentService.generatePreview(
                                placeId,
                                planId);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_PDF)
                                .contentLength(pdf.length)
                                .header(
                                                HttpHeaders.CONTENT_DISPOSITION,
                                                "inline; filename=\"preview.pdf\"")
                                .body(pdf);
        }

        // =====================================================
        // ELIMINAR
        // =====================================================

        @DeleteMapping("/{planId}")
        @Operation(summary = "Eliminar un plano")
        public ResponseEntity<Void> deletePlan(

                        @PathVariable UUID placeId,

                        @PathVariable UUID planId,

                        Authentication authentication

        ) {

                System.out.println(
                                "========================================");

                System.out.println(
                                "DELETE PLAN");

                System.out.println(
                                "Usuario autenticado: "
                                                + authentication.getName());

                System.out.println(
                                "Principal: "
                                                + authentication.getPrincipal());

                System.out.println(
                                "Authorities: "
                                                + authentication.getAuthorities());

                System.out.println(
                                "========================================");

                planDocumentService.deletePlan(

                                placeId,

                                planId,

                                authentication);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}