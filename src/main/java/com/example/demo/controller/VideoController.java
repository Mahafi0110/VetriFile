package com.example.demo.controller;

import com.example.demo.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // ── CONVERT VIDEO ─────────────────────────
    @PostMapping("/convert")
    public ResponseEntity<ByteArrayResource> convertVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) {
        try {
            byte[] result = videoService.convertVideo(file, format);
            return buildDownloadResponse(
                result,
                "converted_" + file.getOriginalFilename()
                    .replaceAll("\\.[^.]+$", "") + "." + format,
                "video/" + format
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── TRIM VIDEO ────────────────────────────
    @PostMapping("/trim")
    public ResponseEntity<ByteArrayResource> trimVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("start") double start,
            @RequestParam("end") double end) {
        try {
            byte[] result = videoService.trimVideo(file, start, end);
            return buildDownloadResponse(
                result,
                "trimmed_" + file.getOriginalFilename(),
                "video/mp4"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── EXTRACT AUDIO FROM VIDEO ──────────────
    @PostMapping("/extract-audio")
    public ResponseEntity<ByteArrayResource> extractAudio(
            @RequestParam("file") MultipartFile file) {
        try {
            byte[] result = videoService.extractAudio(file);
            return buildDownloadResponse(
                result,
                "audio_" + file.getOriginalFilename()
                    .replaceAll("\\.[^.]+$", "") + ".mp3",
                "audio/mpeg"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── MERGE VIDEOS ──────────────────────────
    @PostMapping("/merge")
    public ResponseEntity<ByteArrayResource> mergeVideos(
            @RequestParam("files") MultipartFile[] files) {
        try {
            byte[] result = videoService.mergeVideos(files);
            return buildDownloadResponse(
                result,
                "merged_video.mp4",
                "video/mp4"
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
public ResponseEntity<ByteArrayResource> compressVideo(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value="level",
            defaultValue="medium") String level,
        @RequestParam(value="crf",
            defaultValue="28") int crf,
        @RequestParam(value="resolution",
            defaultValue="original") String resolution,
        @RequestParam(value="format",
            defaultValue="mp4") String format) {
    try {
        byte[] result = videoService.compressVideo(
            file, level, crf, resolution, format
        );
        return buildDownloadResponse(
            result,
            "compressed_" +
              file.getOriginalFilename()
                .replaceAll("\\.[^.]+$","") + "." + format,
            "video/" + format
        );
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
    // ── HEALTH CHECK FOR FFMPEG ───────────────
    @GetMapping("/health/ffmpeg")
    public ResponseEntity<String> checkFFmpeg() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean completed = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            
            if (completed && process.exitValue() == 0) {
                return ResponseEntity.ok("FFmpeg is available");
            } else {
                return ResponseEntity.status(503).body("FFmpeg check failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body("FFmpeg not found: " + e.getMessage());
        }
    }
}
