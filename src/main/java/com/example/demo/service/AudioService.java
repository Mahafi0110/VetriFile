package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class AudioService {

    private static final String FFMPEG = "ffmpeg";

    // ✅ FIXED: drains FFmpeg output to prevent deadlock/hang
    private void drainProcess(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
            }
        }
    }

    // ── CONVERT AUDIO ─────────────────────────
    public byte[] convertAudio(MultipartFile file, String format)
            throws IOException, InterruptedException {

        Path tempDir    = Files.createTempDirectory("vetri_audio_");
        Path inputFile  = tempDir.resolve("input_" + file.getOriginalFilename());
        Path outputFile = tempDir.resolve("output." + format);

        try {
            Files.write(inputFile, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG,
                "-i", inputFile.toString(),
                "-y", outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            drainProcess(process); // ✅ drain output — prevents deadlock

            if (process.waitFor() != 0) {
                throw new RuntimeException("Convert audio failed");
            }

            return Files.readAllBytes(outputFile);

        } finally {
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
        Path inputFile  = tempDir.resolve("input_" + file.getOriginalFilename());
        Path outputFile = tempDir.resolve("trimmed.mp3");

        try {
            Files.write(inputFile, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG,
                "-ss", String.valueOf(start),  // fast seek BEFORE -i
                "-i",  inputFile.toString(),
                "-t",  String.valueOf(end - start),
                "-acodec", "libmp3lame",
                "-ab", "192k",
                "-y",  outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            drainProcess(process); // ✅ drain output — prevents deadlock

            if (process.waitFor() != 0) {
                throw new RuntimeException("Trim audio failed");
            }

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
            Path[] inputPaths = new Path[files.length];

            // ✅ Normalize all inputs to same format first (prevents concat codec mismatch)
            for (int i = 0; i < files.length; i++) {
                Path raw = tempDir.resolve("raw_" + i + "." +
                        getExtension(files[i].getOriginalFilename()));
                Files.write(raw, files[i].getBytes());

                inputPaths[i] = tempDir.resolve("norm_" + i + ".mp3");

                ProcessBuilder normPb = new ProcessBuilder(
                    FFMPEG,
                    "-i",      raw.toString(),
                    "-acodec", "libmp3lame",
                    "-ar",     "44100",
                    "-ac",     "2",
                    "-ab",     bitrate + "k",
                    "-y",      inputPaths[i].toString()
                );
                normPb.redirectErrorStream(true);
                Process normProcess = normPb.start();
                drainProcess(normProcess); // ✅ drain
                if (normProcess.waitFor() != 0) {
                    throw new RuntimeException("Normalize failed for file " + i);
                }
            }

            // ✅ Create silence in mp3 format (not raw PCM)
            Path silencePath = null;
            if (gap > 0) {
                silencePath = tempDir.resolve("silence.mp3");
                ProcessBuilder silPb = new ProcessBuilder(
                    FFMPEG,
                    "-f", "lavfi",
                    "-i", "anullsrc=r=44100:cl=stereo",
                    "-t", String.valueOf(gap),
                    "-acodec", "libmp3lame",
                    "-y", silencePath.toString()
                );
                silPb.redirectErrorStream(true);
                Process silProcess = silPb.start();
                drainProcess(silProcess); // ✅ drain
                silProcess.waitFor();
            }

            // Build concat list
            StringBuilder listContent = new StringBuilder();
            for (int i = 0; i < inputPaths.length; i++) {
                listContent.append("file '")
                           .append(inputPaths[i].toString())
                           .append("'\n");
                if (gap > 0 && i < inputPaths.length - 1 && silencePath != null) {
                    listContent.append("file '")
                               .append(silencePath.toString())
                               .append("'\n");
                }
            }

            Path listFile   = tempDir.resolve("list.txt");
            Path outputFile = tempDir.resolve("merged." + format);
            Files.writeString(listFile, listContent.toString());

            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG,
                "-f",    "concat",
                "-safe", "0",
                "-i",    listFile.toString(),
                "-b:a",  bitrate + "k",
                "-y",    outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            drainProcess(process); // ✅ drain

            if (process.waitFor() != 0) {
                throw new RuntimeException("Merge audio failed");
            }

            return Files.readAllBytes(outputFile);

        } finally {
            try (DirectoryStream<Path> stream =
                    Files.newDirectoryStream(tempDir)) {
                for (Path p : stream) Files.deleteIfExists(p);
            }
            Files.deleteIfExists(tempDir);
        }
    }

    // ── COMPRESS AUDIO ────────────────────────
    public byte[] compressAudio(MultipartFile file,
                                  int bitrate,
                                  String format,
                                  String sampleRate)
            throws IOException, InterruptedException {

        Path tempDir    = Files.createTempDirectory("vetri_audio_");
        Path inputFile  = tempDir.resolve("input_" + file.getOriginalFilename());
        Path outputFile = tempDir.resolve("compressed." + format);

        try {
            Files.write(inputFile, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG,
                "-i",   inputFile.toString(),
                "-b:a", bitrate + "k",
                "-ar",  sampleRate,
                "-y",   outputFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            drainProcess(process); // ✅ drain output — prevents deadlock

            if (process.waitFor() != 0) {
                throw new RuntimeException("Compress audio failed");
            }

            return Files.readAllBytes(outputFile);

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── HELPER ───────────────────────────────
    private String getExtension(String filename) {
        if (filename == null) return "mp3";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "mp3";
    }
}
