package com.example.demo.controller;

import com.example.demo.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/word")
public class WordController {

    @Autowired
    private WordService wordService;

    @PostMapping("/to-pdf")
    public ResponseEntity<StreamingResponseBody> convertWordToPdf(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] result = wordService.convertToPdf(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".pdf";
        return buildStreamResponse(result, outputName, "application/pdf");
    }

    @PostMapping("/to-html")
    public ResponseEntity<StreamingResponseBody> convertWordToHtml(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] result = wordService.convertToHtml(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".html";
        return buildStreamResponse(result, outputName, "text/html");
    }

    @PostMapping("/to-text")
    public ResponseEntity<StreamingResponseBody> convertWordToText(
            @RequestParam("file") MultipartFile file) throws Exception {

        byte[] result = wordService.extractText(file);
        String outputName = stripExtension(file.getOriginalFilename()) + ".txt";
        return buildStreamResponse(result, outputName, "text/plain");
    }

    @PostMapping("/word-count")
    public ResponseEntity<Map<String, Object>> wordCount(
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(wordService.getWordCount(file));
    }

    @PostMapping("/metadata")
    public ResponseEntity<Map<String, Object>> metadata(
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(wordService.extractMetadata(file));
    }

    @PostMapping("/merge")
    public ResponseEntity<StreamingResponseBody> mergeDocuments(
            @RequestParam("files") MultipartFile[] files) throws Exception {

        byte[] result = wordService.mergeDocuments(files);
        return buildStreamResponse(result, "merged.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
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

    private static String stripExtension(String name) {
        if (name == null) return "output";
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }
}