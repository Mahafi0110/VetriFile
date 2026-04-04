package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // ── Main Pages ────────────────────────────
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/index")
    public String indexAlt() {
        return "index";
    }

    @GetMapping("/all-tools")
    public String allTools() {
        return "all-tools";
    }

    @GetMapping("/file-tools")
    public String fileTools() {
        return "file-tools";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // ── PDF Tool Pages ────────────────────────
    @GetMapping("/tool-convert-pdf")
    public String toolConvertPdf() {
        return "tool-convert-pdf";
    }
    @GetMapping("/tool-compress-pdf")
public String toolCompressPdf() {
    return "tool-compress-pdf"; // now has its own page!
}

    @GetMapping("/tool-add-signature")
    public String toolAddSignature() {
        return "tool-add-signature";
    }

    @GetMapping("/tool-lock-pdf")
    public String toolLockPdf() {
        return "tool-lock-pdf";
    }

    @GetMapping("/tool-unlock-pdf")
    public String toolUnlockPdf() {
        return "tool-unlock-pdf";
    }
    
    @GetMapping("/tool-remove-background")
    public String toolRemoveBackground() {
        return "tool-remove-background";
    }

    @GetMapping("/tool-merge-pdf")
    public String toolMergePdf() {
        return "tool-merge-pdf";
    }

    // @GetMapping("/tool-compress-pdf")
    // public String toolCompressPdf() {
    //     return "tool-convert-pdf";
    // }

    @GetMapping("/tool-split-pdf")
    public String toolSplitPdf() {
        return "tool-split-pdf";
    }

    // ── Image Tool Pages ──────────────────────
    @GetMapping("/tool-convert-image")
    public String toolConvertImage() {
        return "tool-convert-pdf";
    }

    @GetMapping("/tool-crop-image")
    public String toolCropImage() {
        return "tool-crop-image";
    }

    @GetMapping("/tool-resize-image")
    public String toolResizeImage() {
        return "tool-resize-image";
    }

    @GetMapping("/tool-watermark-image")
    public String toolWatermarkImage() {
        return "tool-watermark-image";
    }

    @GetMapping("/tool-merge-images")
    public String toolMergeImages() {
        return "tool-convert-pdf";
    }

    @GetMapping("/tool-compress-image")
    public String toolCompressImage() {
        return "tool-compress-image";
    }

    // ── Audio Tool Pages ──────────────────────
    @GetMapping("/tool-convert-audio")
    public String toolConvertAudio() {
        return "tool-compress-audio";
    }

    @GetMapping("/tool-trim-audio")
    public String toolTrimAudio() {
        return "tool-trim-audio";
    }

    @GetMapping("/tool-merge-audio")
    public String toolMergeAudio() {
        return "tool-merge-audio";
    }

    @GetMapping("/tool-extract-audio")
    public String toolExtractAudio() {
        return "tool-convert-pdf";
    }

    // ── Video Tool Pages ──────────────────────
    @GetMapping("/tool-compress-video")
    public String toolCompressVideo() {
        return "tool-compress-video";
    }

    @GetMapping("/tool-trim-video")
    public String toolTrimVideo() {
        return "tool-trim-video";
    }

    @GetMapping("/tool-extract-audio-video")
    public String toolExtractAudioVideo() {
        return "tool-extract-audio-video";
    }

    @GetMapping("/tool-merge-videos")
    public String toolMergeVideos() {
        return "tool-convert-pdf";
    }
}