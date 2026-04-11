package com.example.demo.controller;

import com.example.demo.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/word")
public class WordController {

    @Autowired
    private WordService wordService;

    /**
     * Convert Word (.doc / .docx) → PDF  [LibreOffice]
     */
    @PostMapping("/to-pdf")
    public ResponseEntity<ByteArrayResource> convertWordToPdf(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] pdfBytes = wordService.convertToPdf(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    /**
     * Convert Word (.doc / .docx) → HTML  [LibreOffice]
     */
    @PostMapping("/to-html")
    public ResponseEntity<ByteArrayResource> convertWordToHtml(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] htmlBytes = wordService.convertToHtml(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".html";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.TEXT_HTML)
                .contentLength(htmlBytes.length)
                .body(new ByteArrayResource(htmlBytes));
    }

    /**
     * Extract plain text (.doc / .docx)  [Apache POI]
     */
    @PostMapping("/to-text")
    public ResponseEntity<ByteArrayResource> convertWordToText(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] textBytes = wordService.extractText(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(textBytes.length)
                .body(new ByteArrayResource(textBytes));
    }

    /**
     * Word / char / paragraph count  (.doc + .docx)
     */
    @PostMapping("/word-count")
    public ResponseEntity<Map<String, Object>> wordCount(
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(wordService.getWordCount(file));
    }

    /**
     * Extract document metadata  (.doc + .docx)
     */
    @PostMapping("/metadata")
    public ResponseEntity<Map<String, Object>> metadata(
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(wordService.extractMetadata(file));
    }

    /**
     * Merge multiple .doc / .docx → single .docx
     */
    @PostMapping("/merge")
    public ResponseEntity<ByteArrayResource> mergeDocuments(
            @RequestParam("files") MultipartFile[] files) throws Exception {

        byte[] merged = wordService.mergeDocuments(files);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"merged.docx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .contentLength(merged.length)
                .body(new ByteArrayResource(merged));
    }

    // ─────────────────────────────────────────────────────────────────────────
    private static String stripExtension(String name) {
        if (name == null) return "output";
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }
}
