package com.example.demo.controller;

// import com.example.demo.dto.ApiResponse;
import com.example.demo.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    // ── COMPRESS PDF ──────────────────────────
    @PostMapping("/compress")
    public ResponseEntity<ByteArrayResource> compressPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", defaultValue = "70") int quality) {

        try {
            byte[] result = pdfService.compressPdf(file, quality);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"compressed_" + file.getOriginalFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(result.length)
                    .body(new ByteArrayResource(result));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── MERGE PDFs ────────────────────────────
    @PostMapping("/merge")
    public ResponseEntity<ByteArrayResource> mergePdfs(
            @RequestParam("files") MultipartFile[] files) {
        try {
            byte[] result = pdfService.mergePdfs(files);
            return buildDownloadResponse(
                    result,
                    "merged_output.pdf",
                    "application/pdf");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── LOCK PDF ──────────────────────────────
    @PostMapping("/lock")
    public ResponseEntity<ByteArrayResource> lockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try {
            byte[] result = pdfService.lockPdf(file, password);
            return buildDownloadResponse(
                    result,
                    "locked_" + file.getOriginalFilename(),
                    "application/pdf");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── UNLOCK PDF ────────────────────────────
    @PostMapping("/unlock")
    public ResponseEntity<ByteArrayResource> unlockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try {
            byte[] result = pdfService.unlockPdf(file, password);
            return buildDownloadResponse(
                    result,
                    "unlocked_" + file.getOriginalFilename(),
                    "application/pdf");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── SPLIT PDF ─────────────────────────────
    @PostMapping("/split")
    public ResponseEntity<ByteArrayResource> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "range") String mode,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageRange", required = false) String pageRange,
            @RequestParam(value = "everyN", required = false) Integer everyN) {
        try {
            if ("every".equalsIgnoreCase(mode)) {
                int n = everyN != null && everyN > 0 ? everyN : 1;
                byte[] zip = pdfService.splitPdfEveryNAsZip(file, n);
                ByteArrayResource resource = new ByteArrayResource(zip);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"split_parts.zip\"")
                        .contentType(MediaType.parseMediaType("application/zip"))
                        .contentLength(zip.length)
                        .body(resource);
            }
            if (pageRange != null && !pageRange.isBlank()) {
                byte[] pdf = pdfService.extractPdfPagesByRange(file, pageRange);
                return buildDownloadResponse(
                        pdf,
                        "split_" + file.getOriginalFilename(),
                        "application/pdf");
            }
            if (page != null && page > 0) {
                byte[] pdf = pdfService.splitPdf(file, page);
                return buildDownloadResponse(
                        pdf,
                        "split_" + file.getOriginalFilename(),
                        "application/pdf");
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── HELPER ───────────────────────────────
    private ResponseEntity<ByteArrayResource> buildDownloadResponse(
            byte[] data, String filename, String contentType) {
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(resource);
    }

    @PostMapping("/add-signature")
    public ResponseEntity<ByteArrayResource> addSignature(
            @RequestParam("file") MultipartFile file,
            @RequestParam("signature") MultipartFile signature,
            @RequestParam(value = "position", defaultValue = "bottom-center") String position) {
        try {
            byte[] result = pdfService.addSignature(
                    file, signature, position);
            return buildDownloadResponse(
                    result,
                    "signed_" + file.getOriginalFilename(),
                    "application/pdf");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}