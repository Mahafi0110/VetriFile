package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class VideoService {

    private static final String FFMPEG = "ffmpeg"; // ✅ FIXED: use PATH

    private void logProcess(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[FFmpeg] " + line);
        }
    }

    // ── CONVERT VIDEO ─────────────────────────
    public byte[] convertVideo(MultipartFile file, String format)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path input = tempDir.resolve("input.mp4");
        Path output = tempDir.resolve("output." + format);

        try {
            Files.write(input, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG,
                    "-i", input.toString(),
                    "-preset", "ultrafast",
                    "-y", output.toString());

            pb.redirectErrorStream(true);
            Process process = pb.start();
            logProcess(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Convert failed");
            }

            return Files.readAllBytes(output);

        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── TRIM VIDEO ────────────────────────────
    public byte[] trimVideo(MultipartFile file,
            double start,
            double end)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path input = tempDir.resolve("input.mp4");
        Path output = tempDir.resolve("trimmed.mp4");

        try {
            Files.write(input, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG,
                    "-ss", String.valueOf(start),
                    "-i", input.toString(),
                    "-t", String.valueOf(end - start),
                    "-vcodec", "libx264",
                    "-preset", "ultrafast",
                    "-crf", "30",
                    "-acodec", "aac",
                    "-y", output.toString());

            pb.redirectErrorStream(true);
            Process process = pb.start();
            logProcess(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Trim failed");
            }

            return Files.readAllBytes(output);

        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── EXTRACT AUDIO ─────────────────────────
    // ✅ FIXED: now accepts format, bitrate, sampleRate, channels params
    public byte[] extractAudio(MultipartFile file,
            String format,
            String bitrate,
            String sampleRate,
            String channels)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path input = tempDir.resolve("input.mp4");
        Path output = tempDir.resolve("audio." + format); // ✅ use chosen format

        try {
            Files.write(input, file.getBytes());

            List<String> cmd = new ArrayList<>(Arrays.asList(
                    FFMPEG,
                    "-i", input.toString(),
                    "-vn", // remove video
                    "-acodec", getAudioCodec(format), // ✅ correct codec per format
                    "-b:a", bitrate + "k", // ✅ use chosen bitrate
                    "-ar", sampleRate, // ✅ use chosen sample rate
                    "-ac", channels, // ✅ use chosen channels
                    "-y", output.toString()));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            logProcess(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Audio extract failed");
            }

            return Files.readAllBytes(output);

        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── MERGE VIDEOS ──────────────────────────
    public byte[] mergeVideos(MultipartFile[] files)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");

        try {
            StringBuilder list = new StringBuilder();

            for (int i = 0; i < files.length; i++) {
                Path p = tempDir.resolve("input_" + i + ".mp4");
                Files.write(p, files[i].getBytes());
                list.append("file '").append(p).append("'\n");
            }

            Path listFile = tempDir.resolve("list.txt");
            Path output = tempDir.resolve("merged.mp4");

            Files.writeString(listFile, list.toString());

            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG,
                    "-f", "concat",
                    "-safe", "0",
                    "-i", listFile.toString(),
                    "-c", "copy",
                    "-y", output.toString());

            pb.redirectErrorStream(true);
            Process process = pb.start();
            logProcess(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Merge failed");
            }

            return Files.readAllBytes(output);

        } finally {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    // ── COMPRESS VIDEO ─────────────────────────
    // ✅ FIXED: now uses actual crf param and applies resolution scaling
    public byte[] compressVideo(MultipartFile file,
            String level,
            int crf,
            String resolution,
            String format)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path input = tempDir.resolve("input.mp4");
        Path output = tempDir.resolve("compressed." + format);

        try {
            Files.write(input, file.getBytes());

            // List<String> cmd = new ArrayList<>(Arrays.asList(
            // FFMPEG,
            // "-i", input.toString(),
            // "-vcodec", "libx264",
            // "-crf", String.valueOf(crf), // ✅ use actual crf from request
            // "-preset", "fast",
            // "-acodec", "aac"
            // ));
            List<String> cmd = new ArrayList<>(Arrays.asList(
                    FFMPEG,
                    "-i", input.toString(),
                    "-vcodec", "libx264",
                    "-crf", String.valueOf(crf), // 18=low, 28=medium, 36=high
                    "-preset", "fast", // ← change from ultrafast to fast
                    "-acodec", "aac",
                    "-b:a", "128k", // ← fix audio bitrate
                    "-movflags", "+faststart" // ← better MP4 compatibility
            ));

            // ✅ Apply resolution scaling only if not "original"
            if (resolution != null && !resolution.equals("original")) {
                cmd.add("-vf");
                cmd.add("scale=-2:" + resolution); // e.g. scale=-2:720
            }

            cmd.add("-y");
            cmd.add(output.toString());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            logProcess(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Compress failed");
            }

            return Files.readAllBytes(output);

        } finally {
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── HELPER: map format to correct FFmpeg audio codec ──
    private String getAudioCodec(String format) {
        if (format == null)
            return "libmp3lame";
        switch (format.toLowerCase()) {
            case "mp3":
                return "libmp3lame";
            case "aac":
                return "aac";
            case "ogg":
                return "libvorbis";
            case "m4a":
                return "aac";
            case "wav":
                return "pcm_s16le";
            case "flac":
                return "flac";
            default:
                return "libmp3lame";
        }
    }
}
