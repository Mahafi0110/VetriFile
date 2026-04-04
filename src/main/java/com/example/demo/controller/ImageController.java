package com.example.demo.controller;

import com.example.demo.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // ── COMPRESS IMAGE ────────────────────────
    @PostMapping("/compress")
    public ResponseEntity<ByteArrayResource> compressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality",
                defaultValue = "0.7") float quality) {
        try {
            byte[] result = imageService.compressImage(file, quality);
            return buildDownloadResponse(
                result,
                "compressed_" + file.getOriginalFilename(),
                "image/jpeg"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── RESIZE IMAGE ──────────────────────────
    @PostMapping("/resize")
    public ResponseEntity<ByteArrayResource> resizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("width") int width,
            @RequestParam("height") int height) {
        try {
            byte[] result = imageService.resizeImage(file, width, height);
            return buildDownloadResponse(
                result,
                "resized_" + file.getOriginalFilename(),
                "image/jpeg"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── CROP IMAGE ────────────────────────────
    @PostMapping("/crop")
    public ResponseEntity<ByteArrayResource> cropImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("x") int x,
            @RequestParam("y") int y,
            @RequestParam("width") int width,
            @RequestParam("height") int height) {
        try {
            byte[] result =
                imageService.cropImage(file, x, y, width, height);
            return buildDownloadResponse(
                result,
                "cropped_" + file.getOriginalFilename(),
                "image/jpeg"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── CONVERT IMAGE ─────────────────────────
    @PostMapping("/convert")
    public ResponseEntity<ByteArrayResource> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) {
        try {
            byte[] result = imageService.convertImage(file, format);
            return buildDownloadResponse(
                result,
                "converted_" + file.getOriginalFilename()
                    .replaceAll("\\.[^.]+$", "") + "." + format,
                "image/" + format
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── ADD WATERMARK ─────────────────────────
    @PostMapping("/watermark")
    public ResponseEntity<ByteArrayResource> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text) {
        try {
            byte[] result = imageService.addWatermark(file, text);
            return buildDownloadResponse(
                result,
                "watermarked_" + file.getOriginalFilename(),
                "image/jpeg"
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
    @PostMapping("/remove-background")
public ResponseEntity<ByteArrayResource> removeBackground(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value="bgOption",
            defaultValue="transparent") String bgOption,
        @RequestParam(value="bgColor",
            defaultValue="#ffffff") String bgColor,
        @RequestParam(value="format",
            defaultValue="png") String format) {
    try {
        byte[] result = imageService
            .removeBackground(file, bgOption, bgColor, format);
        return buildDownloadResponse(
            result,
            "no_bg_" + file.getOriginalFilename()
                .replaceAll("\\.[^.]+$","") + "." + format,
            "image/" + format
        );
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}
