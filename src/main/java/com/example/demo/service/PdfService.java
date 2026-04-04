package com.example.demo.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


@Service
public class PdfService {

    // ── COMPRESS PDF ──────────────────────────
    public byte[] compressPdf(MultipartFile file, int quality) throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    // ── MERGE PDFs ────────────────────────────
    public byte[] mergePdfs(MultipartFile[] files) throws IOException {
        try (PDDocument merged = new PDDocument()) {
            for (MultipartFile file : files) {
                byte[] fileBytes = file.getBytes();
                try (PDDocument doc = Loader.loadPDF(fileBytes)) {
                    for (PDPage page : doc.getPages()) {
                        merged.addPage(page);
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            merged.save(out);
            return out.toByteArray();
        }
    }

    // ── LOCK PDF ──────────────────────────────
    public byte[] lockPdf(MultipartFile file, String password)
            throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(false);
            ap.setCanModify(false);
            ap.setCanExtractContent(false);

            StandardProtectionPolicy policy =
                new StandardProtectionPolicy(
                    password, password, ap
                );
            policy.setEncryptionKeyLength(128);

            document.protect(policy);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    // ── UNLOCK PDF ────────────────────────────
    public byte[] unlockPdf(MultipartFile file, String password)
            throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(
                fileBytes, password)) {
            document.setAllSecurityToBeRemoved(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    // ── SPLIT PDF ─────────────────────────────
    public byte[] splitPdf(MultipartFile file, int pageNumber)
            throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            int totalPages = document.getNumberOfPages();

            if (pageNumber < 1 || pageNumber > totalPages) {
                throw new RuntimeException(
                    "Invalid page number: " + pageNumber
                );
            }

            try (PDDocument split = new PDDocument()) {
                for (int i = pageNumber - 1; i < totalPages; i++) {
                    split.addPage(document.getPage(i));
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                split.save(out);
                return out.toByteArray();
            }
        }
    }

    // ── SPLIT PDF EVERY N PAGES AS ZIP ───────
    public byte[] splitPdfEveryNAsZip(MultipartFile file, int n) throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            int totalPages = document.getNumberOfPages();
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                for (int i = 0; i < totalPages; i += n) {
                    try (PDDocument part = new PDDocument()) {
                        for (int j = i; j < Math.min(i + n, totalPages); j++) {
                            part.addPage(document.getPage(j));
                        }
                        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
                        part.save(pdfOut);
                        ZipEntry entry = new ZipEntry("part_" + (i / n + 1) + ".pdf");
                        zos.putNextEntry(entry);
                        zos.write(pdfOut.toByteArray());
                        zos.closeEntry();
                    }
                }
            }
            return zipOut.toByteArray();
        }
    }

    // ── EXTRACT PDF PAGES BY RANGE ────────────
    public byte[] extractPdfPagesByRange(MultipartFile file, String pageRange) throws IOException {
        byte[] fileBytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            try (PDDocument extracted = new PDDocument()) {
                String[] ranges = pageRange.split(",");
                for (String range : ranges) {
                    if (range.contains("-")) {
                        String[] parts = range.split("-");
                        int start = Integer.parseInt(parts[0].trim()) - 1;
                        int end = Integer.parseInt(parts[1].trim()) - 1;
                        for (int i = start; i <= end; i++) {
                            if (i < document.getNumberOfPages()) {
                                extracted.addPage(document.getPage(i));
                            }
                        }
                    } else {
                        int page = Integer.parseInt(range.trim()) - 1;
                        if (page < document.getNumberOfPages()) {
                            extracted.addPage(document.getPage(page));
                        }
                    }
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                extracted.save(out);
                return out.toByteArray();
            }
        }
    }
    // ── ADD SIGNATURE ─────────────────────────────
public byte[] addSignature(MultipartFile pdfFile,
                            MultipartFile signatureFile,
                            String position)
        throws IOException {

    byte[] pdfBytes = pdfFile.getBytes();
    byte[] sigBytes = signatureFile.getBytes();

    try (PDDocument document = Loader.loadPDF(pdfBytes)) {

        BufferedImage sigImage = ImageIO.read(
            new ByteArrayInputStream(sigBytes)
        );

        for (PDPage page : document.getPages()) {
            PDPageContentStream cs =
                new PDPageContentStream(
                    document, page,
                    PDPageContentStream.AppendMode.APPEND,
                    true, true
                );

            PDImageXObject pdImage =
                LosslessFactory.createFromImage(
                    document, sigImage
                );

            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();
            float sigW  = 150f;
            float sigH  = 50f;

            float x, y;
            switch (position) {
                case "top-left":
                    x = 30; y = pageH - sigH - 30; break;
                case "top-center":
                    x = (pageW - sigW) / 2;
                    y = pageH - sigH - 30; break;
                case "top-right":
                    x = pageW - sigW - 30;
                    y = pageH - sigH - 30; break;
                case "bottom-left":
                    x = 30; y = 30; break;
                case "bottom-right":
                    x = pageW - sigW - 30; y = 30; break;
                case "center":
                    x = (pageW - sigW) / 2;
                    y = (pageH - sigH) / 2; break;
                default:
                    x = (pageW - sigW) / 2; y = 30;
            }

            cs.drawImage(pdImage, x, y, sigW, sigH);
            cs.close();
        }

        ByteArrayOutputStream out =
            new ByteArrayOutputStream();
        document.save(out);
        return out.toByteArray();
    }
}
}