package com.example.demo.controller;

import com.example.demo.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    // ── COMPRESS PDF ──────────────────────────
    // @PostMapping("/compress")
    // public ResponseEntity<InputStreamResource> compressPdf(
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam(value = "quality", defaultValue = "70") int quality) throws IOException {

    //     InputStream resultStream = new ByteArrayInputStream(pdfService.compressPdf(file, quality));
    //     return buildStreamResponse(resultStream, "compressed_" + file.getOriginalFilename(), "application/pdf");
    // }
    @PostMapping("/compress")
public ResponseEntity<InputStreamResource> compressPdf(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "quality", defaultValue = "70") int quality) throws IOException {

    byte[] result = pdfService.compressPdf(file, quality);

    InputStreamResource resource =
            new InputStreamResource(new ByteArrayInputStream(result));

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=compressed_" + file.getOriginalFilename())
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
}


    // ── MERGE PDFs ────────────────────────────
    @PostMapping("/merge")
    public ResponseEntity<InputStreamResource> mergePdfs(@RequestParam("files") MultipartFile[] files) throws IOException {
        InputStream resultStream = new ByteArrayInputStream(pdfService.mergePdfs(files));
        return buildStreamResponse(resultStream, "merged_output.pdf", "application/pdf");
    }

    // ── LOCK PDF ──────────────────────────────
    @PostMapping("/lock")
    public ResponseEntity<InputStreamResource> lockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {

        InputStream resultStream = new ByteArrayInputStream(pdfService.lockPdf(file, password));
        return buildStreamResponse(resultStream, "locked_" + file.getOriginalFilename(), "application/pdf");
    }

    // ── UNLOCK PDF ────────────────────────────
    @PostMapping("/unlock")
    public ResponseEntity<InputStreamResource> unlockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {

        InputStream resultStream = new ByteArrayInputStream(pdfService.unlockPdf(file, password));
        return buildStreamResponse(resultStream, "unlocked_" + file.getOriginalFilename(), "application/pdf");
    }

    // ── SPLIT PDF ─────────────────────────────
    @PostMapping("/split")
    public ResponseEntity<InputStreamResource> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "range") String mode,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageRange", required = false) String pageRange,
            @RequestParam(value = "everyN", required = false) Integer everyN) throws IOException {

        if ("every".equalsIgnoreCase(mode)) {
            int n = everyN != null && everyN > 0 ? everyN : 1;
            InputStream zipStream = new ByteArrayInputStream(pdfService.splitPdfEveryNAsZip(file, n));
            return buildStreamResponse(zipStream, "split_parts.zip", "application/zip");
        }

        if (pageRange != null && !pageRange.isBlank()) {
            InputStream rangeStream = new ByteArrayInputStream(pdfService.extractPdfPagesByRange(file, pageRange));
            return buildStreamResponse(rangeStream, "split_" + file.getOriginalFilename(), "application/pdf");
        }

        if (page != null && page > 0) {
            InputStream pageStream = new ByteArrayInputStream(pdfService.splitPdf(file, page));
            return buildStreamResponse(pageStream, "split_" + file.getOriginalFilename(), "application/pdf");
        }

        return ResponseEntity.badRequest().build();
    }

    // ── ADD SIGNATURE ─────────────────────────
    @PostMapping("/add-signature")
    public ResponseEntity<InputStreamResource> addSignature(
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

        InputStream resultStream = new ByteArrayInputStream(
                pdfService.addSignature(file, signature, position, resolvedRange, size, opacity)
        );

        return buildStreamResponse(resultStream, "signed_" + file.getOriginalFilename(), "application/pdf");
    }

    // ── HELPER ───────────────────────────────
    private ResponseEntity<InputStreamResource> buildStreamResponse(InputStream stream, String filename, String contentType) {
        InputStreamResource resource = new InputStreamResource(stream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
