package com.harold.rn.printer.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UtilsImage {

    // ESC/POS Print Constants - shared across all adapters
    public static final byte[] LINE_FEED = new byte[] { 0x0A };
    public static final byte[] LEFT_ALIGN = new byte[] { 0x1B, 0x61, 0x00 };
    public static final byte[] CENTER_ALIGN = new byte[] { 0x1B, 0x61, 0x31 };
    public static final byte[] RIGHT_ALIGN = new byte[] { 0x1B, 0x61, 0x02 };

    // Paper width constants (dots at 203 DPI)
    /** Print area width for 58mm paper: 48mm × 203dpi ≈ 384 dots */
    public static final int PAPER_WIDTH_58MM = 384;
    /** Print area width for 80mm paper: 72mm × 203dpi ≈ 576 dots */
    public static final int PAPER_WIDTH_80MM = 576;

    /**
     * Download bitmap from URL
     * @param src URL of the image
     * @return Bitmap or null if failed
     */
    public static Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Bitmap normalizeForPrint(Bitmap src) {
        // Always work in ARGB_8888
        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888, true);

        // Flatten transparent pixels onto white background
        Bitmap out = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        c.drawColor(Color.WHITE);
        c.drawBitmap(bmp, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return out;
    }

    public static Bitmap getBitmapResized(Bitmap image, float decreaseSizeBy, int imageWidth, int imageHeight) {
        int imageWidthForResize = image.getWidth();
        int imageHeightForResize = image.getHeight();
        if (imageWidth > 0) {
            imageWidthForResize = imageWidth;
        }

        if (imageHeight > 0) {
            imageHeightForResize = imageHeight;
        }
        return Bitmap.createScaledBitmap(image, (int) (imageWidthForResize * decreaseSizeBy),
                (int) (imageHeightForResize * decreaseSizeBy), true);
    }

    public static int getRGB(Bitmap bmpOriginal, int col, int row) {
        // get one pixel color
        int pixel = bmpOriginal.getPixel(col, row);
        // retrieve color of all channels
        int R = Color.red(pixel);
        int G = Color.green(pixel);
        int B = Color.blue(pixel);
        return Color.rgb(R, G, B);
    }

    public static Bitmap boostContrast(Bitmap src, float contrast) {
        // contrast: 1.0 = no change, 1.2–1.6 good range
        int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);

        int mid = 128;
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = (c >>> 24) & 0xFF;
            int r = (c >>> 16) & 0xFF;
            int g = (c >>> 8) & 0xFF;
            int b = c & 0xFF;

            r = clamp((int) ((r - mid) * contrast + mid));
            g = clamp((int) ((g - mid) * contrast + mid));
            b = clamp((int) ((b - mid) * contrast + mid));

            px[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    public static int autoThresholdOtsu(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] px = new int[w * h];
        bitmap.getPixels(px, 0, w, 0, 0, w, h);

        int[] hist = new int[256];
        for (int c : px) {
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            int gray = (r * 299 + g * 587 + b * 114) / 1000;
            hist[gray]++;
        }

        int total = w * h;
        float sum = 0;
        for (int i = 0; i < 256; i++)
            sum += i * hist[i];

        float sumB = 0;
        int wB = 0;
        int wF;
        float varMax = -1;
        int threshold = 160;

        for (int t = 0; t < 256; t++) {
            wB += hist[t];
            if (wB == 0)
                continue;
            wF = total - wB;
            if (wF == 0)
                break;

            sumB += (float) (t * hist[t]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        return threshold;
    }

    public static Bitmap applyGamma(Bitmap src, float gamma) {
        // gamma < 1.0 = darker text
        int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);

        float inv = 1.0f / gamma;
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = (c >>> 24) & 0xFF;
            int r = (c >>> 16) & 0xFF;
            int g = (c >>> 8) & 0xFF;
            int b = c & 0xFF;

            r = clamp((int) (255 * Math.pow(r / 255.0, inv)));
            g = clamp((int) (255 * Math.pow(g / 255.0, inv)));
            b = clamp((int) (255 * Math.pow(b / 255.0, inv)));

            px[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    public static int getARGB(Bitmap bmpOriginal, int col, int row) {
        // keep alpha!
        return bmpOriginal.getPixel(col, row);
    }

    /**
     * Resize a bitmap to match the target printer width while preserving aspect ratio.
     * Height is always auto-calculated — the printer renders row-by-row so only width matters.
     *
     * <p>Typical dot widths:</p>
     * <ul>
     *   <li>58mm paper → {@link #PAPER_WIDTH_58MM} (384 dots)</li>
     *   <li>80mm paper → {@link #PAPER_WIDTH_80MM} (576 dots)</li>
     * </ul>
     *
     * @param image      Source bitmap. Returns {@code null} if input is {@code null}.
     * @param imageWidth Target width in dots. Pass 0 or negative to default to 80mm (576 dots).
     * @return Resized bitmap, or the original if already the correct width.
     */
    public static Bitmap resizeTheImageForPrinting(Bitmap image, int imageWidth) {
        if (image == null) return null;

        int w = image.getWidth();
        int h = image.getHeight();

        // Default to 80mm (576 dots) if not provided
        int targetW = (imageWidth > 0) ? imageWidth : PAPER_WIDTH_80MM;

        // Already correct width — skip re-allocation
        if (w == targetW) return image;

        // Scale by width only, auto-calculate height to preserve aspect ratio
        float scale = (float) targetW / (float) w;
        int targetH = Math.max(1, Math.round(h * scale));

        return Bitmap.createScaledBitmap(image, targetW, targetH, true);
    }

    public static boolean shouldPrintColor(int col) {
        // If transparent, treat as white (do NOT print)
        int a = (col >>> 24) & 0xff;
        if (a < 128)
            return false;

        int r = (col >>> 16) & 0xff;
        int g = (col >>> 8) & 0xff;
        int b = col & 0xff;

        int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

        // 127 is often too low -> faint output. Try 160–190 depending on printer.
        final int threshold = 170;
        return luminance < threshold;
    }

    public static byte[] recollectSlice(int y, int x, int[][] img) {
        byte[] slices = new byte[] { 0, 0, 0 };
        for (int yy = y, i = 0; yy < y + 24 && i < 3; yy += 8, i++) {
            byte slice = 0;
            for (int b = 0; b < 8; b++) {
                int yyy = yy + b;
                if (yyy >= img.length) {
                    continue;
                }
                int col = img[yyy][x];
                boolean v = shouldPrintColor(col);
                slice |= (byte) ((v ? 1 : 0) << (7 - b));
            }
            slices[i] = slice;
        }
        return slices;
    }

    public static byte[] toRasterGSv0(Bitmap bitmap, int threshold) {
        // bitmap MUST already be normalized (white bg, ARGB_8888) + resized to printer
        // width
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int bytesPerRow = (w + 7) / 8;
        byte[] out = new byte[8 + bytesPerRow * h];

        // GS v 0
        out[0] = 0x1D;
        out[1] = 0x76;
        out[2] = 0x30;
        out[3] = 0x00; // m = 0
        out[4] = (byte) (bytesPerRow & 0xFF); // xL
        out[5] = (byte) ((bytesPerRow >> 8) & 0xFF); // xH
        out[6] = (byte) (h & 0xFF); // yL
        out[7] = (byte) ((h >> 8) & 0xFF); // yH

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        // Floyd–Steinberg error buffer (grayscale)
        int[] gray = new int[w * h];
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            gray[i] = (r * 299 + g * 587 + b * 114) / 1000;
        }

        // Dither
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                int old = gray[idx];
                int newVal = (old < threshold) ? 0 : 255; // 0=black,255=white
                int err = old - newVal;
                gray[idx] = newVal;

                // distribute error
                if (x + 1 < w)
                    gray[idx + 1] = clamp(gray[idx + 1] + (err * 7) / 16);
                if (y + 1 < h) {
                    if (x > 0)
                        gray[idx + w - 1] = clamp(gray[idx + w - 1] + (err * 3) / 16);
                    gray[idx + w] = clamp(gray[idx + w] + (err * 5) / 16);
                    if (x + 1 < w)
                        gray[idx + w + 1] = clamp(gray[idx + w + 1] + (err * 1) / 16);
                }
            }
        }

        // Pack bits: 1 = black dot, 0 = white dot
        int o = 8;
        for (int y = 0; y < h; y++) {
            for (int xb = 0; xb < bytesPerRow; xb++) {
                int b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = xb * 8 + bit;
                    int v = 255; // white
                    if (x < w)
                        v = gray[y * w + x];
                    if (v == 0)
                        b |= (1 << (7 - bit)); // black -> set bit
                }
                out[o++] = (byte) b;
            }
        }

        return out;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    public static int[][] getPixelsSlow(Bitmap image2, int imageWidth, int imageHeight) {
        Bitmap image = resizeTheImageForPrinting(normalizeForPrint(image2), imageWidth);
        if (image == null) return new int[0][0];

        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = getARGB(image, col, row);
            }
        }
        return result;
    }

    /**
     * Prepare image for thermal printing using fast raster mode (GS v 0).
     * This method combines all image processing steps:
     * 1. Normalize bitmap (white background, ARGB_8888)
     * 2. Resize to printer width
     * 3. Calculate optimal threshold using Otsu's method
     * 4. Convert to raster data using GS v 0 format
     *
     * @param bitmapImage Source bitmap to print
     * @param imageWidth  Target width in dots (e.g., 576 for 80mm, 384 for 58mm). Pass 0 to default to 80mm.
     * @return byte[] containing complete raster data ready to send to printer
     */
    public static byte[] prepareImageRasterData(Bitmap bitmapImage, int imageWidth) {
        // Normalize bitmap (white background, ARGB_8888) then resize to printer dot width
        Bitmap normalizedBitmap = normalizeForPrint(bitmapImage);
        Bitmap resizedBitmap = resizeTheImageForPrinting(normalizedBitmap, imageWidth);
        if (resizedBitmap == null) return new byte[0];

        // Calculate optimal binarization threshold using Otsu's method
        int threshold = autoThresholdOtsu(resizedBitmap);

        // Convert to raster data using GS v 0 format (fast, single transfer)
        return toRasterGSv0(resizedBitmap, threshold);
    }
}
