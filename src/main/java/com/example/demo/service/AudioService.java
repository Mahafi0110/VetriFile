package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class AudioService {

    // ── CONVERT AUDIO ─────────────────────────
    public byte[] convertAudio(MultipartFile file, String format)
            throws IOException, InterruptedException {

        Path tempDir    = Files.createTempDirectory("vetri_audio_");
        Path inputFile  = tempDir.resolve(
            file.getOriginalFilename()
        );
        Path outputFile = tempDir.resolve(
            "output." + format
        );

        try {
            // Save uploaded file
            Files.write(inputFile, file.getBytes());

            // Run FFmpeg
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.toString(),
                "-y", outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return Files.readAllBytes(outputFile);

        } finally {
            // Cleanup temp files
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── TRIM AUDIO ────────────────────────────
    public byte[] trimAudio(MultipartFile file,
                             double start, double end)
            throws IOException, InterruptedException {

        Path tempDir    = Files.createTempDirectory("vetri_audio_");
        Path inputFile  = tempDir.resolve(
            file.getOriginalFilename()
        );
        Path outputFile = tempDir.resolve("trimmed.mp3");

        try {
            Files.write(inputFile, file.getBytes());

            double duration = end - start;

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i",  inputFile.toString(),
                "-ss", String.valueOf(start),
                "-t",  String.valueOf(duration),
                "-y",  outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return Files.readAllBytes(outputFile);

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── MERGE AUDIO ───────────────────────────
 public byte[] mergeAudio(MultipartFile[] files,
                          String format,
                          int bitrate,
                          double gap)
        throws IOException, InterruptedException {

    Path tempDir = Files.createTempDirectory("vetri_audio_");

    try {
        StringBuilder listContent = new StringBuilder();
        Path silencePath = null;

        // Create silence file for gap if needed
        if (gap > 0) {
            silencePath = tempDir.resolve("silence.mp3");
            ProcessBuilder silPb = new ProcessBuilder(
                "ffmpeg",
                "-f", "lavfi",
                "-i", "anullsrc=r=44100:cl=stereo",
                "-t", String.valueOf(gap),
                "-y", silencePath.toString()
            );
            silPb.redirectErrorStream(true);
            silPb.start().waitFor();
        }

        Path[] inputPaths = new Path[files.length];
        for (int i = 0; i < files.length; i++) {
            inputPaths[i] = tempDir.resolve("input_" + i + "." +
                getExtension(files[i].getOriginalFilename()));
            Files.write(inputPaths[i], files[i].getBytes());
            listContent.append("file '")
                .append(inputPaths[i].toString())
                .append("'\n");
            // Add silence between tracks
            if (gap > 0 && i < files.length - 1 &&
                silencePath != null) {
                listContent.append("file '")
                    .append(silencePath.toString())
                    .append("'\n");
            }
        }

        Path listFile   = tempDir.resolve("list.txt");
        Path outputFile = tempDir.resolve("merged." + format);
        Files.writeString(listFile, listContent.toString());

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-f",    "concat",
            "-safe", "0",
            "-i",    listFile.toString(),
            "-b:a",  bitrate + "k",
            "-y",    outputFile.toString()
        );
        pb.redirectErrorStream(true);
        pb.start().waitFor();

        return Files.readAllBytes(outputFile);

    } finally {
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(tempDir)) {
            for (Path p : stream) Files.deleteIfExists(p);
        }
        Files.deleteIfExists(tempDir);
    }
}

private String getExtension(String filename) {
    if (filename == null) return "mp3";
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot + 1) : "mp3";
}
    // ── COMPRESS AUDIO ────────────────────────────
public byte[] compressAudio(MultipartFile file,
                              int bitrate,
                              String format,
                              String sampleRate)
        throws IOException, InterruptedException {

    Path tempDir    = Files.createTempDirectory("vetri_audio_");
    Path inputFile  = tempDir.resolve(file.getOriginalFilename());
    Path outputFile = tempDir.resolve("compressed." + format);

    try {
        Files.write(inputFile, file.getBytes());

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i",  inputFile.toString(),
            "-b:a", bitrate + "k",
            "-ar",  sampleRate,
            "-y",   outputFile.toString()
        );
        pb.redirectErrorStream(true);
        pb.start().waitFor();

        return Files.readAllBytes(outputFile);
    } finally {
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
        Files.deleteIfExists(tempDir);
    }
}
}