package com.example.demo.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageController {

    // ══════════════════════════════════════════════════
    // MAIN PAGES
    // ══════════════════════════════════════════════════

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/index")
    public String indexAlt() {
        return "redirect:/";
    }

    @GetMapping("/all-tools")
    public String allTools() {
        return "all-tools";
    }

    @GetMapping("/file-tools")
    public String fileTools() {
        return "file-tools";
    }

    @GetMapping("/Feature")
    public String Feature() {
        return "Feature";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public void favicon() {
        // stops the 500 error silently
    }

    // ══════════════════════════════════════════════════
    // PDF TOOL PAGES
    // ══════════════════════════════════════════════════

    @GetMapping("/tool-convert-pdf")
    public String toolConvertPdf() {
        return "tool-convert-pdf";
    }

    @GetMapping("/tool-compress-pdf")
    public String toolCompressPdf() {
        return "tool-compress-pdf";
    }

    @GetMapping("/tool-merge-pdf")
    public String toolMergePdf() {
        return "tool-merge-pdf";
    }

    @GetMapping("/tool-split-pdf")
    public String toolSplitPdf() {
        return "tool-split-pdf";
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

    // ══════════════════════════════════════════════════
    // IMAGE TOOL PAGES
    // ══════════════════════════════════════════════════

    @GetMapping("/tool-convert-image")
    public String toolConvertImage() {
        return "tool-convert-image";
    }

    @GetMapping("/tool-crop-image")
    public String toolCropImage() {
        return "tool-crop-image";
    }

    @GetMapping("/tool-resize-image")
    public String toolResizeImage() {
        return "tool-resize-image";
    }

    @GetMapping("/tool-compress-image")
    public String toolCompressImage() {
        return "tool-compress-image";
    }

    @GetMapping("/tool-watermark-image")
    public String toolWatermarkImage() {
        return "tool-watermark-image";
    }

    @GetMapping("/tool-merge-images")
    public String toolMergeImages() {
        return "tool-merge-images";
    }

    @GetMapping("/tool-remove-background")
    public String toolRemoveBackground() {
        return "tool-remove-background";
    }

    // ══════════════════════════════════════════════════
    // AUDIO TOOL PAGES
    // ══════════════════════════════════════════════════

    @GetMapping("/tool-convert-audio")
    public String toolConvertAudio() {
        return "tool-convert-audio";
    }

    @GetMapping("/tool-trim-audio")
    public String toolTrimAudio() {
        return "tool-trim-audio";
    }

    @GetMapping("/tool-merge-audio")
    public String toolMergeAudio() {
        return "tool-merge-audio";
    }

    @GetMapping("/tool-compress-audio")
    public String toolCompressAudio() {
        return "tool-compress-audio";
    }

    @GetMapping("/tool-extract-audio")
    public String toolExtractAudio() {
        return "tool-extract-audio";
    }

    // ══════════════════════════════════════════════════
    // VIDEO TOOL PAGES
    // ══════════════════════════════════════════════════

    @GetMapping("/tool-compress-video")
    public String toolCompressVideo() {
        return "tool-compress-video";
    }

    @GetMapping("/tool-trim-video")
    public String toolTrimVideo() {
        return "tool-trim-video";
    }

    @GetMapping("/tool-merge-videos")
    public String toolMergeVideos() {
        return "tool-merge-videos";
    }

    @GetMapping("/tool-extract-audio-video")
    public String toolExtractAudioVideo() {
        return "tool-extract-audio-video";
    }

    @GetMapping("/tool-word-pdf")
    public String wordPdfPage() {
        return "tool-word-pdf";
    }
    @GetMapping("/tool-word-html")
    public String wordHtmlPage() {
        return "tool-word-html";
    }
    @GetMapping("/tool-word-text")
    public String wordTextPage() {
        return "tool-word-text";
    }

}