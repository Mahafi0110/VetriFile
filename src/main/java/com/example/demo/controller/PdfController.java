package com.example.demo.controller;

import com.example.demo.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compressPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", defaultValue = "70") int quality) throws IOException {

        byte[] result = pdfService.compressPdf(file, quality);
        return buildStreamResponse(result,
                "compressed_" + file.getOriginalFilename(), "application/pdf");
    }

    @PostMapping("/merge")
    public ResponseEntity<StreamingResponseBody> mergePdfs(
            @RequestParam("files") MultipartFile[] files) throws IOException {

        byte[] result = pdfService.mergePdfs(files);
        return buildStreamResponse(result, "merged_output.pdf", "application/pdf");
    }

    @PostMapping("/lock")
    public ResponseEntity<StreamingResponseBody> lockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {

        byte[] result = pdfService.lockPdf(file, password);
        return buildStreamResponse(result,
                "locked_" + file.getOriginalFilename(), "application/pdf");
    }

    @PostMapping("/unlock")
    public ResponseEntity<StreamingResponseBody> unlockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {

        byte[] result = pdfService.unlockPdf(file, password);
        return buildStreamResponse(result,
                "unlocked_" + file.getOriginalFilename(), "application/pdf");
    }

    @PostMapping("/split")
    public ResponseEntity<StreamingResponseBody> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "range") String mode,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageRange", required = false) String pageRange,
            @RequestParam(value = "everyN", required = false) Integer everyN) throws IOException {

        if ("every".equalsIgnoreCase(mode)) {
            int n = everyN != null && everyN > 0 ? everyN : 1;
            byte[] result = pdfService.splitPdfEveryNAsZip(file, n);
            return buildStreamResponse(result, "split_parts.zip", "application/zip");
        }

        if (pageRange != null && !pageRange.isBlank()) {
            byte[] result = pdfService.extractPdfPagesByRange(file, pageRange);
            return buildStreamResponse(result,
                    "split_" + file.getOriginalFilename(), "application/pdf");
        }

        if (page != null && page > 0) {
            byte[] result = pdfService.splitPdf(file, page);
            return buildStreamResponse(result,
                    "split_" + file.getOriginalFilename(), "application/pdf");
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/add-signature")
    public ResponseEntity<StreamingResponseBody> addSignature(
            @RequestParam("file") MultipartFile file,
            @RequestParam("signature") MultipartFile signature,
            @RequestParam(value = "position", defaultValue = "bottom-center") String position,
            @RequestParam(value = "pageRange", defaultValue = "all") String pageRange,
            @RequestParam(value = "fromPage", required = false) Integer fromPage,
            @RequestParam(value = "toPage", required = false) Integer toPage,
            @RequestParam(value = "size", defaultValue = "100") float size,
            @RequestParam(value = "opacity", defaultValue = "100") float opacity) throws IOException {

        String resolvedRange = pageRange;
        if ("custom".equals(pageRange) && fromPage != null && toPage != null) {
            resolvedRange = "custom:" + fromPage + ":" + toPage;
        }

        byte[] result = pdfService.addSignature(
                file, signature, position, resolvedRange, size, opacity);
        return buildStreamResponse(result,
                "signed_" + file.getOriginalFilename(), "application/pdf");
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private ResponseEntity<StreamingResponseBody> buildStreamResponse(
            byte[] data, String filename, String contentType) {

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new ByteArrayInputStream(data)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush(); // ✅ push each chunk immediately
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header("X-Accel-Buffering", "no") // ✅ disable Render proxy buffering
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(stream);
    }
}