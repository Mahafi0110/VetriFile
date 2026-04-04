package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoService {

    private static void drainProcessOutput(Process process) throws IOException {
        try (InputStream in = process.getInputStream()) {
            in.readAllBytes();
        }
    }

    private static String safeInputName(String original) {
        if (original == null || original.isBlank()) {
            return "input.mp4";
        }
        String name = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!name.contains(".")) {
            name = name + ".mp4";
        }
        return name;
    }

    /**
     * Runs ffmpeg; returns true if exit code 0 and output exists and is non-empty.
     */
    private boolean runFfmpegTrim(
            Path inputFile,
            Path outputFile,
            double startSec,
            double durationSec,
            boolean streamCopy) throws IOException, InterruptedException {

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-hide_banner");
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(inputFile.toAbsolutePath().toString());
        cmd.add("-ss");
        cmd.add(Double.toString(startSec));
        cmd.add("-t");
        cmd.add(Double.toString(durationSec));
        if (streamCopy) {
            cmd.add("-c");
            cmd.add("copy");
            cmd.add("-avoid_negative_ts");
            cmd.add("make_zero");
        } else {
            cmd.add("-c:v");
            cmd.add("libx264");
            cmd.add("-preset");
            cmd.add("fast");
            cmd.add("-crf");
            cmd.add("23");
            cmd.add("-c:a");
            cmd.add("aac");
            cmd.add("-movflags");
            cmd.add("+faststart");
        }
        cmd.add(outputFile.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        drainProcessOutput(process);
        int code = process.waitFor();
        return code == 0
                && Files.exists(outputFile)
                && Files.size(outputFile) > 0;
    }

    // ── CONVERT VIDEO ─────────────────────────
    public byte[] convertVideo(MultipartFile file, String format)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path inputFile = tempDir.resolve(
                file.getOriginalFilename());
        Path outputFile = tempDir.resolve("output." + format);

        try {
            Files.write(inputFile, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.toString(),
                    "-y", outputFile.toString());
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

    // ── TRIM VIDEO ────────────────────────────
    public byte[] trimVideo(MultipartFile file,
            double start, double end)
            throws IOException, InterruptedException {

        if (end <= start) {
            throw new IllegalArgumentException("end must be greater than start");
        }

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path inputFile = tempDir.resolve(safeInputName(file.getOriginalFilename()));
        Path outputFile = tempDir.resolve("trimmed.mp4");

        double duration = end - start;

        try {
            Files.write(inputFile, file.getBytes());

            Files.deleteIfExists(outputFile);
            boolean ok = runFfmpegTrim(inputFile, outputFile, start, duration, true);
            if (!ok) {
                Files.deleteIfExists(outputFile);
                ok = runFfmpegTrim(inputFile, outputFile, start, duration, false);
            }
            if (!ok) {
                throw new IOException(
                        "ffmpeg failed (is ffmpeg installed and on PATH?). "
                                + "Stream-copy failed and re-encode fallback failed.");
            }
            return Files.readAllBytes(outputFile);

        } finally {
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(tempDir);
        }
    }

    // ── EXTRACT AUDIO ─────────────────────────
    public byte[] extractAudio(MultipartFile file)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");
        Path inputFile = tempDir.resolve(
                file.getOriginalFilename());
        Path outputFile = tempDir.resolve("audio.mp3");

        try {
            Files.write(inputFile, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.toString(),
                    "-q:a", "0",
                    "-map", "a",
                    "-y", outputFile.toString());
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

    // ── MERGE VIDEOS ──────────────────────────
    public byte[] mergeVideos(MultipartFile[] files)
            throws IOException, InterruptedException {

        Path tempDir = Files.createTempDirectory("vetri_video_");

        try {
            StringBuilder listContent = new StringBuilder();
            Path[] inputPaths = new Path[files.length];

            for (int i = 0; i < files.length; i++) {
                inputPaths[i] = tempDir.resolve("input_" + i + ".mp4");
                Files.write(inputPaths[i], files[i].getBytes());
                listContent.append("file '")
                        .append(inputPaths[i].toString())
                        .append("'\n");
            }

            Path listFile = tempDir.resolve("list.txt");
            Path outputFile = tempDir.resolve("merged.mp4");
            Files.writeString(listFile, listContent.toString());

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", listFile.toString(),
                    "-c", "copy",
                    "-y", outputFile.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return Files.readAllBytes(outputFile);

        } finally {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir)) {
                for (Path p : stream)
                    Files.deleteIfExists(p);
            }
            Files.deleteIfExists(tempDir);
        }
    }

    // ── COMPRESS VIDEO ─────────────────────────────
// ── COMPRESS VIDEO ─────────────────────────────
public byte[] compressVideo(MultipartFile file,
                              String level,
                              int crf,
                              String resolution,
                              String format)
        throws IOException, InterruptedException {

    Path tempDir    =
      Files.createTempDirectory("vetri_video_");
    Path inputFile  =
      tempDir.resolve(file.getOriginalFilename());
    Path outputFile =
      tempDir.resolve("compressed." + format);

    try {
        Files.write(inputFile, file.getBytes());

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(inputFile.toString());

        // CRF controls quality/size
        cmd.add("-crf");
        cmd.add(String.valueOf(crf));

        // Resolution scaling
        if (!resolution.equals("original")) {
            cmd.add("-vf");
            cmd.add("scale=-2:" + resolution);
        }

        // Audio quality
        cmd.add("-b:a");
        cmd.add("128k");

        // Fast encoding preset
        cmd.add("-preset");
        cmd.add("fast");

        cmd.add("-y");
        cmd.add(outputFile.toString());

        ProcessBuilder pb =
          new ProcessBuilder(cmd);
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
