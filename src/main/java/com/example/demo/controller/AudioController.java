package com.example.demo.controller;

import com.example.demo.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @PostMapping("/convert")
    public ResponseEntity<StreamingResponseBody> convertAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws Exception {

        File result = audioService.convertAudio(file, format);
        return buildStreamResponse(result, result.getName());
    }

    @PostMapping("/trim")
    public ResponseEntity<StreamingResponseBody> trimAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("start") double start,
            @RequestParam("end") double end) throws Exception {

        File result = audioService.trimAudio(file, start, end);
        return buildStreamResponse(result, result.getName());
    }

    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compressAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bitrate", defaultValue = "128") int bitrate,
            @RequestParam(value = "format", defaultValue = "mp3") String format,
            @RequestParam(value = "sampleRate", defaultValue = "44100") String sampleRate) throws Exception {

        File result = audioService.compressAudio(file, bitrate, format, sampleRate);
        return buildStreamResponse(result, result.getName());
    }

    @PostMapping("/merge")
    public ResponseEntity<StreamingResponseBody> mergeAudio(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "format", defaultValue = "mp3") String format,
            @RequestParam(value = "bitrate", defaultValue = "192") int bitrate,
            @RequestParam(value = "gap", defaultValue = "0.0") double gap) throws Exception {

        File result = audioService.mergeAudio(files, format, bitrate, gap);
        return buildStreamResponse(result, result.getName());
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private ResponseEntity<StreamingResponseBody> buildStreamResponse(File file, String filename) {
        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush(); // ✅ push each chunk immediately
                }
            } finally {
                file.delete(); // ✅ cleanup after stream finishes
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Accel-Buffering", "no") // ✅ disable Render proxy buffering
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(stream);
    }
}