package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.User;
import accesoya_backend.places.application.dto.PlanDocumentResponse;
import accesoya_backend.places.domain.model.PlanDocument;
import accesoya_backend.places.domain.model.Place;
import accesoya_backend.places.domain.repository.PlanDocumentRepository;
import accesoya_backend.places.domain.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PlanDocumentService {

        private final PlanDocumentRepository planDocumentRepository;

        private final PlaceRepository placeRepository;

        // =====================================================
        // CONFIGURACIÓN LIBREOFFICE
        // =====================================================

        @Value("${libreoffice.executable}")
        private String libreOfficeExecutable;

        // =====================================================
        // CONFIGURACIÓN DE ARCHIVOS
        // =====================================================

        private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

        private static final List<String> ALLOWED_CONTENT_TYPES = List.of(

                        // =================================================
                        // PDF
                        // =================================================

                        "application/pdf",

                        // =================================================
                        // IMÁGENES
                        // =================================================

                        "image/png",
                        "image/jpeg",

                        // =================================================
                        // MICROSOFT WORD
                        // =================================================

                        "application/msword",

                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",

                        // =================================================
                        // MICROSOFT EXCEL
                        // =================================================

                        "application/vnd.ms-excel",

                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

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
        // SUBIR PLANO / DOCUMENTO
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
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
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

                // -------------------------------------------------
                // ARCHIVO OBLIGATORIO
                // -------------------------------------------------

                if (file == null ||
                                file.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "El archivo es obligatorio");
                }

                // -------------------------------------------------
                // TAMAÑO MÁXIMO
                // -------------------------------------------------

                if (file.getSize() > MAX_FILE_SIZE) {

                        throw new IllegalArgumentException(
                                        "El archivo supera el tamaño máximo permitido de 20 MB");
                }

                // -------------------------------------------------
                // TIPO MIME
                // -------------------------------------------------

                String contentType = file.getContentType();

                if (contentType == null ||
                                !ALLOWED_CONTENT_TYPES.contains(
                                                contentType.toLowerCase())) {

                        throw new IllegalArgumentException(
                                        "Tipo de archivo no permitido. "
                                                        + "Solo se permiten PDF, PNG, JPG, JPEG, "
                                                        + "Word y Excel");
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

                // -------------------------------------------------
                // EVITAR NOMBRES EXCESIVAMENTE LARGOS
                // -------------------------------------------------

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

                if (!placeRepository.existsById(
                                placeId)) {

                        throw new IllegalArgumentException(
                                        "No se encontró el sitio con id: "
                                                        + placeId);
                }
        }

        // =====================================================
        // OBTENER ARCHIVO ORIGINAL
        // =====================================================

        @Transactional(readOnly = true)
        public PlanDocument getPlan(

                        UUID placeId,

                        UUID planId) {

                return planDocumentRepository
                                .findByIdAndPlaceId(
                                                planId,
                                                placeId)

                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "No se encontró el plano solicitado"));
        }

        // =====================================================
        // GENERAR VISTA PREVIA
        // =====================================================

        @Transactional(readOnly = true)
        public byte[] generatePreview(

                        UUID placeId,

                        UUID planId) {

                // -------------------------------------------------
                // OBTENER DOCUMENTO
                // -------------------------------------------------

                PlanDocument document = getPlan(
                                placeId,
                                planId);

                String contentType = document.getContentType();

                // =================================================
                // PDF
                // =================================================

                if ("application/pdf".equalsIgnoreCase(
                                contentType)) {

                        System.out.println(
                                        "========================================");

                        System.out.println(
                                        "VISTA PREVIA PDF");

                        System.out.println(
                                        "Archivo: "
                                                        + document.getFileName());

                        System.out.println(
                                        "========================================");

                        return document.getFileData();
                }

                // =================================================
                // WORD / EXCEL
                // =================================================

                if (isOfficeDocument(
                                contentType)) {

                        System.out.println(
                                        "========================================");

                        System.out.println(
                                        "VISTA PREVIA OFFICE");

                        System.out.println(
                                        "Archivo: "
                                                        + document.getFileName());

                        System.out.println(
                                        "Tipo: "
                                                        + contentType);

                        System.out.println(
                                        "LibreOffice: "
                                                        + libreOfficeExecutable);

                        System.out.println(
                                        "========================================");

                        return convertOfficeToPdf(
                                        document);
                }

                // =================================================
                // FORMATO NO COMPATIBLE
                // =================================================

                throw new IllegalArgumentException(
                                "El formato del archivo no permite generar "
                                                + "una vista previa");
        }

        // =====================================================
        // VERIFICAR DOCUMENTO OFFICE
        // =====================================================

        private boolean isOfficeDocument(
                        String contentType) {

                if (contentType == null) {

                        return false;
                }

                return contentType.equalsIgnoreCase(
                                "application/msword")

                                ||

                                contentType.equalsIgnoreCase(
                                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

                                ||

                                contentType.equalsIgnoreCase(
                                                "application/vnd.ms-excel")

                                ||

                                contentType.equalsIgnoreCase(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        // =====================================================
        // CONVERTIR WORD / EXCEL A PDF
        // =====================================================

        private byte[] convertOfficeToPdf(
                        PlanDocument document) {

                Path temporaryDirectory = null;

                Path inputFile = null;

                try {

                        // =================================================
                        // VALIDAR EJECUTABLE DE LIBREOFFICE
                        // =================================================

                        if (libreOfficeExecutable == null ||
                                        libreOfficeExecutable.isBlank()) {

                                throw new IllegalStateException(
                                                "No se configuró la ruta del ejecutable de LibreOffice");
                        }

                        Path libreOfficePath = Path.of(
                                        libreOfficeExecutable);

                        if (!Files.exists(
                                        libreOfficePath)) {

                                throw new IllegalStateException(
                                                "No se encontró LibreOffice en: "
                                                                + libreOfficeExecutable);
                        }

                        // =================================================
                        // CREAR DIRECTORIO TEMPORAL
                        // =================================================

                        temporaryDirectory = Files.createTempDirectory(
                                        "accesoya-plan-preview-");

                        // =================================================
                        // CREAR ARCHIVO TEMPORAL
                        // =================================================

                        String fileName = document.getFileName();

                        String extension = getFileExtension(
                                        fileName);

                        inputFile = temporaryDirectory.resolve(
                                        "documento" + extension);

                        Files.write(
                                        inputFile,
                                        document.getFileData());

                        // =================================================
                        // EJECUTAR LIBREOFFICE
                        // =================================================

                        ProcessBuilder processBuilder = new ProcessBuilder(

                                        libreOfficeExecutable,

                                        "--headless",

                                        "--convert-to",
                                        "pdf",

                                        "--outdir",
                                        temporaryDirectory.toString(),

                                        inputFile.toString());

                        processBuilder
                                        .redirectErrorStream(
                                                        true);

                        System.out.println(
                                        "Iniciando conversión Office → PDF...");

                        System.out.println(
                                        "Ejecutable: "
                                                        + libreOfficeExecutable);

                        System.out.println(
                                        "Archivo temporal: "
                                                        + inputFile);

                        Process process = processBuilder.start();

                        // =================================================
                        // LEER SALIDA DE LIBREOFFICE
                        // =================================================

                        String processOutput;

                        try (InputStream inputStream = process.getInputStream()) {

                                processOutput = new String(
                                                inputStream.readAllBytes(),
                                                StandardCharsets.UTF_8);
                        }

                        // =================================================
                        // ESPERAR FINALIZACIÓN
                        // =================================================

                        int exitCode = process.waitFor();

                        System.out.println(
                                        "Código de salida LibreOffice: "
                                                        + exitCode);

                        if (!processOutput.isBlank()) {

                                System.out.println(
                                                "Salida LibreOffice:");

                                System.out.println(
                                                processOutput);
                        }

                        // =================================================
                        // VALIDAR RESULTADO
                        // =================================================

                        if (exitCode != 0) {

                                throw new IllegalStateException(
                                                "LibreOffice no pudo convertir "
                                                                + "el documento. "
                                                                + processOutput);
                        }

                        // =================================================
                        // LOCALIZAR PDF
                        // =================================================

                        String baseName = removeExtension(
                                        inputFile
                                                        .getFileName()
                                                        .toString());

                        Path pdfFile = temporaryDirectory.resolve(
                                        baseName + ".pdf");

                        if (!Files.exists(
                                        pdfFile)) {

                                throw new IllegalStateException(
                                                "LibreOffice finalizó pero "
                                                                + "no generó el archivo PDF. "
                                                                + "Salida: "
                                                                + processOutput);
                        }

                        // =================================================
                        // LEER PDF
                        // =================================================

                        byte[] pdf = Files.readAllBytes(
                                        pdfFile);

                        System.out.println(
                                        "========================================");

                        System.out.println(
                                        "PDF GENERADO CORRECTAMENTE");

                        System.out.println(
                                        "Archivo original: "
                                                        + document.getFileName());

                        System.out.println(
                                        "Tamaño PDF: "
                                                        + pdf.length
                                                        + " bytes");

                        System.out.println(
                                        "========================================");

                        return pdf;

                } catch (IOException exception) {

                        throw new IllegalStateException(
                                        "No se pudo ejecutar LibreOffice "
                                                        + "para generar la vista previa.",
                                        exception);

                } catch (InterruptedException exception) {

                        Thread.currentThread().interrupt();

                        throw new IllegalStateException(
                                        "La conversión del documento "
                                                        + "fue interrumpida.",
                                        exception);

                } finally {

                        // =================================================
                        // LIMPIAR ARCHIVOS TEMPORALES
                        // =================================================

                        deleteTemporaryDirectory(
                                        temporaryDirectory);
                }
        }

        // =====================================================
        // OBTENER EXTENSIÓN
        // =====================================================

        private String getFileExtension(
                        String fileName) {

                if (fileName == null ||
                                fileName.isBlank()) {

                        return "";
                }

                int index = fileName.lastIndexOf('.');

                if (index < 0) {

                        return "";
                }

                return fileName.substring(
                                index);
        }

        // =====================================================
        // ELIMINAR EXTENSIÓN
        // =====================================================

        private String removeExtension(
                        String fileName) {

                if (fileName == null ||
                                fileName.isBlank()) {

                        return "documento";
                }

                int index = fileName.lastIndexOf('.');

                if (index < 0) {

                        return fileName;
                }

                return fileName.substring(
                                0,
                                index);
        }

        // =====================================================
        // ELIMINAR DIRECTORIO TEMPORAL
        // =====================================================

        private void deleteTemporaryDirectory(
                        Path directory) {

                if (directory == null ||
                                !Files.exists(directory)) {

                        return;
                }

                try (Stream<Path> paths = Files.walk(directory)) {

                        paths
                                        .sorted(
                                                        Comparator.reverseOrder())

                                        .forEach(
                                                        path -> {

                                                                try {

                                                                        Files.deleteIfExists(
                                                                                        path);

                                                                } catch (IOException exception) {

                                                                        System.err.println(
                                                                                        "No se pudo eliminar archivo temporal: "
                                                                                                        + path);
                                                                }
                                                        });

                } catch (IOException exception) {

                        System.err.println(
                                        "No se pudo limpiar el directorio temporal: "
                                                        + directory);
                }
        }

        // =====================================================
        // ELIMINAR DOCUMENTO
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
                // VALIDAR USUARIO
                // -------------------------------------------------

                if (!(authentication.getPrincipal() instanceof User)) {

                        throw new AccessDeniedException(
                                        "El usuario autenticado no es válido");
                }

                // -------------------------------------------------
                // BUSCAR PLANO
                // -------------------------------------------------

                PlanDocument document = planDocumentRepository
                                .findByIdAndPlaceId(
                                                planId,
                                                placeId)

                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "No se encontró el plano solicitado"));

                // -------------------------------------------------
                // ELIMINAR
                // -------------------------------------------------

                planDocumentRepository.delete(
                                document);
        }
}