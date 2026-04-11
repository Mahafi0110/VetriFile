package com.example.demo.controller;

import com.example.demo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // ── COMPRESS VIDEO ──────────────────────────────────────────────────────────
    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> compressVideo(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "28") int crf,
            @RequestParam(required = false) String resolution,
            @RequestParam(required = false) String format
    ) throws Exception {

        // Save ONCE here — stream already consumed after this
        File input  = videoService.saveToTempFile(file);
        File output = videoService.compressVideo(input, crf, resolution, format);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                // ✅ Cleanup AFTER stream finishes — not before
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.mp4")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }

    // ── EXTRACT AUDIO ───────────────────────────────────────────────────────────
    @PostMapping("/extract-audio")
    public ResponseEntity<StreamingResponseBody> extractAudio(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        // Save ONCE here — pass File to service (not MultipartFile)
        File input  = videoService.saveToTempFile(file);
        File output = videoService.extractAudio(input, "mp3", "128", "44100", "2");

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audio.mp3")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }

    // ── TRIM VIDEO ──────────────────────────────────────────────────────────────
    @PostMapping("/trim")
    public ResponseEntity<StreamingResponseBody> trimVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam double start,
            @RequestParam double end
    ) throws Exception {

        // Save ONCE here — pass File to service (not MultipartFile)
        File input  = videoService.saveToTempFile(file);
        File output = videoService.trimVideo(input, start, end);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trimmed.mp4")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }
}