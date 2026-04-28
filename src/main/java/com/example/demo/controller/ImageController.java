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

    // ── COMPRESS ─────────────────────────────────────────────────────────────
    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compressImage(
            @RequestParam("file")    MultipartFile file,
            @RequestParam(value = "quality", defaultValue = "0.7") float quality)
            throws Exception {
        byte[] result = imageService.compressImage(file, quality);
        return buildStreamResponse(result,
                "compressed_" + file.getOriginalFilename(), "image/jpeg");
    }

    // ── RESIZE ───────────────────────────────────────────────────────────────
    @PostMapping("/resize")
    public ResponseEntity<StreamingResponseBody> resizeImage(
            @RequestParam("file")   MultipartFile file,
            @RequestParam("width")  int width,
            @RequestParam("height") int height)
            throws Exception {
        byte[] result = imageService.resizeImage(file, width, height);
        return buildStreamResponse(result,
                "resized_" + file.getOriginalFilename(), "image/jpeg");
    }

    // ── CROP ─────────────────────────────────────────────────────────────────
    @PostMapping("/crop")
    public ResponseEntity<StreamingResponseBody> cropImage(
            @RequestParam("file")   MultipartFile file,
            @RequestParam("x")      int x,
            @RequestParam("y")      int y,
            @RequestParam("width")  int width,
            @RequestParam("height") int height)
            throws Exception {
        byte[] result = imageService.cropImage(file, x, y, width, height);
        return buildStreamResponse(result,
                "cropped_" + file.getOriginalFilename(), "image/jpeg");
    }

    // ── CONVERT ──────────────────────────────────────────────────────────────
    @PostMapping("/convert")
    public ResponseEntity<StreamingResponseBody> convertImage(
            @RequestParam("file")   MultipartFile file,
            @RequestParam("format") String format)
            throws Exception {
        byte[] result = imageService.convertImage(file, format);
        String outputName = file.getOriginalFilename()
                .replaceAll("\\.[^.]+$", "") + "." + format;
        return buildStreamResponse(result,
                "converted_" + outputName, "image/" + format);
    }

    // ── WATERMARK ────────────────────────────────────────────────────────────
    @PostMapping("/watermark")
    public ResponseEntity<StreamingResponseBody> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text)
            throws Exception {
        byte[] result = imageService.addWatermark(file, text);
        return buildStreamResponse(result,
                "watermarked_" + file.getOriginalFilename(), "image/jpeg");
    }

    // ── REMOVE BACKGROUND ────────────────────────────────────────────────────
    //
    //  Flow:
    //    1. If app.removebg.api-key is set  → calls remove.bg API (AI-quality result)
    //    2. Otherwise / if API fails         → uses improved local algorithm
    //    3. Composites the chosen background colour on top of the transparent PNG
    //    4. Returns the final image in the requested format
    //
    @PostMapping("/remove-background")
    public ResponseEntity<StreamingResponseBody> removeBackground(
            @RequestParam("file")                                    MultipartFile file,
            @RequestParam(value = "bgOption",   defaultValue = "transparent") String bgOption,
            @RequestParam(value = "bgColor",    defaultValue = "#ffffff")     String bgColor,
            @RequestParam(value = "refinement", defaultValue = "balanced")    String refinement,
            @RequestParam(value = "format",     defaultValue = "png")         String format)
            throws Exception {

        // Sanitise format — only allow known safe values
        if (!format.matches("^(png|jpg|jpeg|webp)$")) format = "png";
        // Sanitise bgOption
        if (!bgOption.matches("^(transparent|white|black|color)$")) bgOption = "transparent";

        byte[] result = imageService.removeBackground(file, bgOption, bgColor, refinement, format);

        String outputName = file.getOriginalFilename()
                .replaceAll("\\.[^.]+$", "") + "_no_bg." + format;

        String mimeType = "jpg".equals(format) || "jpeg".equals(format)
                ? "image/jpeg" : "image/" + format;

        return buildStreamResponse(result, outputName, mimeType);
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private ResponseEntity<StreamingResponseBody> buildStreamResponse(
            byte[] data, String filename, String contentType) {

        StreamingResponseBody stream = outputStream -> {
            try (InputStream in = new ByteArrayInputStream(data)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header("X-Accel-Buffering", "no")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(stream);
    }
}