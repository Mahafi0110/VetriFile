package com.example.demo.controller;

import com.example.demo.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @PostMapping("/convert")
    public ResponseEntity<FileSystemResource> convertAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws Exception {

        File result = audioService.convertAudio(file, format);
        return buildFileResponse(result);
    }

    @PostMapping("/trim")
    public ResponseEntity<FileSystemResource> trimAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("start") double start,
            @RequestParam("end") double end) throws Exception {

        File result = audioService.trimAudio(file, start, end);
        return buildFileResponse(result);
    }

    @PostMapping("/compress")
    public ResponseEntity<FileSystemResource> compressAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bitrate", defaultValue = "128") int bitrate,
            @RequestParam(value = "format", defaultValue = "mp3") String format,
            @RequestParam(value = "sampleRate", defaultValue = "44100") String sampleRate) throws Exception {

        File result = audioService.compressAudio(file, bitrate, format, sampleRate);
        return buildFileResponse(result);
    }

    @PostMapping("/merge")
    public ResponseEntity<FileSystemResource> mergeAudio(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "format", defaultValue = "mp3") String format,
            @RequestParam(value = "bitrate", defaultValue = "192") int bitrate,
            @RequestParam(value = "gap", defaultValue = "0.0") double gap) throws Exception {

        File result = audioService.mergeAudio(files, format, bitrate, gap);
        return buildFileResponse(result);
    }

    // ── HELPER ───────────────────────────────
    private ResponseEntity<FileSystemResource> buildFileResponse(File file) {
        String filename = file.getName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
