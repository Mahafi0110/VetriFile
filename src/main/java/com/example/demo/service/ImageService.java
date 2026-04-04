package com.example.demo.service;

import net.coobird.thumbnailator.Thumbnails;
// import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    // ── COMPRESS IMAGE ────────────────────────
    public byte[] compressImage(MultipartFile file, float quality)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .scale(1.0)
                .outputQuality(quality)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }

    // ── RESIZE IMAGE ──────────────────────────
    public byte[] resizeImage(MultipartFile file,
                               int width, int height)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(width, height)
                .keepAspectRatio(false)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }

    // ── CROP IMAGE ────────────────────────────
    public byte[] cropImage(MultipartFile file,
                             int x, int y,
                             int width, int height)
            throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());

        // Clamp values to image bounds
        int maxX = Math.min(x + width,  original.getWidth());
        int maxY = Math.min(y + height, original.getHeight());
        int cropW = maxX - x;
        int cropH = maxY - y;

        BufferedImage cropped = original.getSubimage(x, y, cropW, cropH);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(cropped, "jpg", out);
        return out.toByteArray();
    }

    // ── CONVERT IMAGE ─────────────────────────
    public byte[] convertImage(MultipartFile file, String format)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .scale(1.0)
                .outputFormat(format)
                .toOutputStream(out);
        return out.toByteArray();
    }

    // ── ADD WATERMARK ─────────────────────────
    public byte[] addWatermark(MultipartFile file, String text)
            throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());

        // Draw on top of image
        Graphics2D g2d = original.createGraphics();

        // Watermark style
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        float alpha = 0.4f;
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        );
        g2d.setColor(Color.GRAY);

        // Font size based on image size
        int fontSize = original.getWidth() / 10;
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));

        FontMetrics fm = g2d.getFontMetrics();
        int x = (original.getWidth() - fm.stringWidth(text)) / 2;
        int y = original.getHeight() / 2;

        g2d.drawString(text, x, y);
        g2d.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(original, "jpg", out);
        return out.toByteArray();
    }
    // ── REMOVE BACKGROUND ────────────────────────
public byte[] removeBackground(MultipartFile file,
                                String bgOption,
                                String bgColor,
                                String format)
        throws IOException {

    BufferedImage original =
        ImageIO.read(file.getInputStream());

    // Create ARGB image
    BufferedImage result = new BufferedImage(
        original.getWidth(),
        original.getHeight(),
        BufferedImage.TYPE_INT_ARGB
    );

    Graphics2D g2d = result.createGraphics();

    // Fill background based on option
    if (bgOption.equals("white")) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0,
            result.getWidth(), result.getHeight());
    } else if (bgOption.equals("black")) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0,
            result.getWidth(), result.getHeight());
    } else if (bgOption.equals("color")) {
        g2d.setColor(Color.decode(bgColor));
        g2d.fillRect(0, 0,
            result.getWidth(), result.getHeight());
    }
    // transparent = no fill needed

    // Simple background removal — flood fill from corners
    // For production use rembg Python service or remove.bg API
    int bgColorSample = original.getRGB(0, 0);
    int tolerance     = 30;

    for (int y = 0; y < original.getHeight(); y++) {
        for (int x = 0; x < original.getWidth(); x++) {
            int pixel = original.getRGB(x, y);
            if (isColorSimilar(pixel, bgColorSample, tolerance)) {
                result.setRGB(x, y, 0x00FFFFFF); // transparent
            } else {
                result.setRGB(x, y, pixel);
            }
        }
    }

    g2d.drawImage(result, 0, 0, null);
    g2d.dispose();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(result, format.equals("jpg") ? "jpg" : "png",
        out);
    return out.toByteArray();
}

private boolean isColorSimilar(int c1, int c2, int tolerance) {
    int r1 = (c1 >> 16) & 0xFF;
    int g1 = (c1 >> 8)  & 0xFF;
    int b1 =  c1        & 0xFF;
    int r2 = (c2 >> 16) & 0xFF;
    int g2 = (c2 >> 8)  & 0xFF;
    int b2 =  c2        & 0xFF;
    return Math.abs(r1-r2) < tolerance &&
           Math.abs(g1-g2) < tolerance &&
           Math.abs(b1-b2) < tolerance;
}
}