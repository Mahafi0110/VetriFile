package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Service
public class AudioService {

    private static final String FFMPEG = "C:\\ffmpeg\\bin\\ffmpeg.exe"; // adjust path

    // ── CONVERT AUDIO ─────────────────────────
    public File convertAudio(MultipartFile file, String format) throws IOException, InterruptedException {
        if (format == null || format.isEmpty()) format = "mp3";

        File input = File.createTempFile("input_", getExtension(file));
        File output = File.createTempFile("converted_", "." + format);

        file.transferTo(input);

        List<String> cmd = Arrays.asList(
                FFMPEG, "-y",
                "-i", input.getAbsolutePath(),
                output.getAbsolutePath()
        );

        runFFmpeg(cmd);
        return output;
    }

    // ── TRIM AUDIO ────────────────────────────
    public File trimAudio(MultipartFile file, double start, double end) throws IOException, InterruptedException {
        File input = File.createTempFile("input_", getExtension(file));
        File output = File.createTempFile("trimmed_", ".mp3");

        file.transferTo(input);

        List<String> cmd = Arrays.asList(
                FFMPEG, "-y",
                "-i", input.getAbsolutePath(),
                "-ss", String.valueOf(start),
                "-to", String.valueOf(end),
                "-c", "copy",
                output.getAbsolutePath()
        );

        runFFmpeg(cmd);
        return output;
    }

    // ── COMPRESS AUDIO ────────────────────────
    public File compressAudio(MultipartFile file, int bitrate, String format, String sampleRate) throws IOException, InterruptedException {

        if (bitrate <= 0) bitrate = 128;
        if (format == null || format.isEmpty()) format = "mp3";
        if (sampleRate == null || sampleRate.isEmpty()) sampleRate = "44100";

        File input = File.createTempFile("input_", getExtension(file));
        File output = File.createTempFile("compressed_", "." + format);

        file.transferTo(input);

        List<String> cmd = Arrays.asList(
                FFMPEG, "-y",
                "-i", input.getAbsolutePath(),
                "-b:a", bitrate + "k",
                "-ar", sampleRate,
                output.getAbsolutePath()
        );

        runFFmpeg(cmd);

        // return original if compression not effective
        if (output.length() >= input.length()) return input;
        return output;
    }

    // ── MERGE AUDIO ───────────────────────────
    public File mergeAudio(MultipartFile[] files, String format, int bitrate, double gap) throws IOException, InterruptedException {

        if (format == null || format.isEmpty()) format = "mp3";

        File tempDir = Files.createTempDirectory("audio_merge_").toFile();
        List<File> normalizedFiles = new ArrayList<>();

        // normalize each file
        for (int i = 0; i < files.length; i++) {
            File input = File.createTempFile("input_" + i + "_", getExtension(files[i]));
            files[i].transferTo(input);

            File normalized = new File(tempDir, "norm_" + i + ".mp3");
            List<String> normCmd = Arrays.asList(
                    FFMPEG, "-y",
                    "-i", input.getAbsolutePath(),
                    "-acodec", "libmp3lame",
                    "-ar", "44100",
                    "-ac", "2",
                    "-ab", bitrate + "k",
                    normalized.getAbsolutePath()
            );
            runFFmpeg(normCmd);
            normalizedFiles.add(normalized);
        }

        // create list.txt for concat
        File listFile = new File(tempDir, "list.txt");
        try (PrintWriter pw = new PrintWriter(listFile)) {
            for (int i = 0; i < normalizedFiles.size(); i++) {
                pw.println("file '" + normalizedFiles.get(i).getAbsolutePath().replace("\\", "/") + "'");
                if (gap > 0 && i < normalizedFiles.size() - 1) {
                    // generate silence
                    File silence = new File(tempDir, "silence.mp3");
                    List<String> silCmd = Arrays.asList(
                            FFMPEG,
                            "-f", "lavfi",
                            "-i", "anullsrc=r=44100:cl=stereo",
                            "-t", String.valueOf(gap),
                            "-y",
                            silence.getAbsolutePath()
                    );
                    runFFmpeg(silCmd);
                    pw.println("file '" + silence.getAbsolutePath().replace("\\", "/") + "'");
                }
            }
        }

        File output = new File(tempDir, "merged_audio." + format);
        List<String> mergeCmd = Arrays.asList(
                FFMPEG,
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.getAbsolutePath(),
                "-b:a", bitrate + "k",
                "-y",
                output.getAbsolutePath()
        );
        runFFmpeg(mergeCmd);

        return output;
    }

    // ── RUN FFMPEG ─────────────────────────
    private void runFFmpeg(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
            }
        }

        int exit = process.waitFor();
        if (exit != 0) throw new RuntimeException("FFmpeg failed with exit code " + exit);
    }

    // ── HELPER ───────────────────────────────
    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            return name.substring(name.lastIndexOf("."));
        }
        return ".mp3";
    }
}
