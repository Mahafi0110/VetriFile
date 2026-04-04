package com.example.demo.controller;

import com.example.demo.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    // ── CONVERT AUDIO ─────────────────────────
    @PostMapping("/convert")
    public ResponseEntity<ByteArrayResource> convertAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) {
        try {
            byte[] result = audioService.convertAudio(file, format);
            return buildDownloadResponse(
                result,
                "converted_" + file.getOriginalFilename()
                    .replaceAll("\\.[^.]+$", "") + "." + format,
                "audio/" + format
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── TRIM AUDIO ────────────────────────────
    @PostMapping("/trim")
    public ResponseEntity<ByteArrayResource> trimAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("start") double start,
            @RequestParam("end") double end) {
        try {
            byte[] result = audioService.trimAudio(file, start, end);
            return buildDownloadResponse(
                result,
                "trimmed_" + file.getOriginalFilename(),
                "audio/mpeg"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── MERGE AUDIO ───────────────────────────
 @PostMapping("/merge")
public ResponseEntity<ByteArrayResource> mergeAudio(
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(value="format",
            defaultValue="mp3") String format,
        @RequestParam(value="bitrate",
            defaultValue="192") int bitrate,
        @RequestParam(value="gap",
            defaultValue="0.0") double gap) {
    try {
        byte[] result = audioService.mergeAudio(
            files, format, bitrate, gap
        );
        return buildDownloadResponse(
            result,
            "merged_audio." + format,
            "audio/" + format
        );
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
    @PostMapping("/compress")
public ResponseEntity<ByteArrayResource> compressAudio(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value="bitrate",
            defaultValue="128") int bitrate,
        @RequestParam(value="format",
            defaultValue="mp3") String format,
        @RequestParam(value="sampleRate",
            defaultValue="44100") String sampleRate) {
    try {
        byte[] result = audioService.compressAudio(
            file, bitrate, format, sampleRate
        );
        return buildDownloadResponse(
            result,
            "compressed_" + file.getOriginalFilename()
                .replaceAll("\\.[^.]+$","") + "." + format,
            "audio/" + format
        );
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}
