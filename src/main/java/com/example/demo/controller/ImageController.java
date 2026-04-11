package com.example.demo.controller;

import com.example.demo.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", defaultValue = "0.7") float quality) throws Exception {

        byte[] result = imageService.compressImage(file, quality);
        return buildStreamResponse(result,
                "compressed_" + file.getOriginalFilename(), "image/jpeg");
    }

    @PostMapping("/resize")
    public ResponseEntity<StreamingResponseBody> resizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("width") int width,
            @RequestParam("height") int height) throws Exception {

        byte[] result = imageService.resizeImage(file, width, height);
        return buildStreamResponse(result,
                "resized_" + file.getOriginalFilename(), "image/jpeg");
    }

    @PostMapping("/crop")
    public ResponseEntity<StreamingResponseBody> cropImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("x") int x,
            @RequestParam("y") int y,
            @RequestParam("width") int width,
            @RequestParam("height") int height) throws Exception {

        byte[] result = imageService.cropImage(file, x, y, width, height);
        return buildStreamResponse(result,
                "cropped_" + file.getOriginalFilename(), "image/jpeg");
    }

    @PostMapping("/convert")
    public ResponseEntity<StreamingResponseBody> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws Exception {

        byte[] result = imageService.convertImage(file, format);
        String outputName = file.getOriginalFilename()
                .replaceAll("\\.[^.]+$", "") + "." + format;
        return buildStreamResponse(result,
                "converted_" + outputName, "image/" + format);
    }

    @PostMapping("/watermark")
    public ResponseEntity<StreamingResponseBody> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text) throws Exception {

        byte[] result = imageService.addWatermark(file, text);
        return buildStreamResponse(result,
                "watermarked_" + file.getOriginalFilename(), "image/jpeg");
    }

    @PostMapping("/remove-background")
    public ResponseEntity<StreamingResponseBody> removeBackground(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bgOption", defaultValue = "transparent") String bgOption,
            @RequestParam(value = "bgColor", defaultValue = "#ffffff") String bgColor,
            @RequestParam(value = "format", defaultValue = "png") String format) throws Exception {

        byte[] result = imageService.removeBackground(file, bgOption, bgColor, format);
        String outputName = file.getOriginalFilename()
                .replaceAll("\\.[^.]+$", "") + "." + format;
        return buildStreamResponse(result,
                "no_bg_" + outputName, "image/" + format);
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