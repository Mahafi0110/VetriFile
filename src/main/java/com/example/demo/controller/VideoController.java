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

    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> compressVideo(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "28") int crf,
            @RequestParam(required = false) String resolution,
            @RequestParam(required = false) String format
    ) throws Exception {

        File input  = videoService.saveToTempFile(file);
        File output = videoService.compressVideo(input, crf, resolution, format);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } finally {
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.mp4")
                .header("X-Accel-Buffering", "no")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }

    @PostMapping("/extract-audio")
    public ResponseEntity<StreamingResponseBody> extractAudio(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        File input  = videoService.saveToTempFile(file);
        File output = videoService.extractAudio(input, "mp3", "128", "44100", "2");

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } finally {
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audio.mp3")
                .header("X-Accel-Buffering", "no")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }

    @PostMapping("/trim")
    public ResponseEntity<StreamingResponseBody> trimVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam double start,
            @RequestParam double end
    ) throws Exception {

        File input  = videoService.saveToTempFile(file);
        File output = videoService.trimVideo(input, start, end);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new FileInputStream(output)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } finally {
                input.delete();
                output.delete();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trimmed.mp4")
                .header("X-Accel-Buffering", "no")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(output.length())
                .body(stream);
    }
}