package com.harold.rn.printer.adapter;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Callback;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract base class for all printer adapters. Provides shared logic for image
 * rasterization, chunk processing, base64 parsing, and automatic connection
 * retry via OutputStream.
 */
public abstract class BasePrinterAdapter implements PrinterAdapter {

    private OutputStream mOutputStream;
    private final ExecutorService mPrintExecutor = Executors.newSingleThreadExecutor();

    // Auto-reconnect settings
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final int RECONNECT_DELAY_MS = 500;

    protected abstract String getLogTag();

    protected abstract boolean isConnectionValid();

    protected abstract OutputStream getRawOutputStream() throws IOException;

    protected abstract String getConnectionErrorMessage();

    protected abstract boolean tryReconnect();

    protected synchronized OutputStream getOutputStream() {
        if (!isConnectionValid()) {
            mOutputStream = null;
            return null;
        }

        if (mOutputStream == null) {
            try {
                mOutputStream = getRawOutputStream();
            } catch (IOException e) {
                Log.e(getLogTag(), "Failed to get output stream: " + e.getMessage());
                mOutputStream = null;
            }
        }
        return mOutputStream;
    }

    protected synchronized void clearOutputStream() {
        mOutputStream = null;
    }

    protected boolean ensureConnection() {
        if (isConnectionValid()) {
            return true;
        }

        Log.i(getLogTag(), "Connection lost, attempting to reconnect...");

        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            Log.i(getLogTag(), "Reconnect attempt " + attempt + "/" + MAX_RECONNECT_ATTEMPTS);

            if (tryReconnect()) {
                Log.i(getLogTag(), "Reconnect successful!");
                return true;
            }

            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        Log.e(getLogTag(), "Failed to reconnect after " + MAX_RECONNECT_ATTEMPTS + " attempts");
        return false;
    }

    protected boolean ensureConnection(Callback errorCallback) {
        if (!ensureConnection()) {
            errorCallback.invoke(getConnectionErrorMessage());
            return false;
        }

        OutputStream printerOutputStream = getOutputStream();
        if (printerOutputStream == null) {
            errorCallback.invoke(getConnectionErrorMessage());
            return false;
        }
        return true;
    }

    protected void sendData(byte[] data) throws IOException {
        OutputStream os = getOutputStream();
        if (os == null) {
            throw new IOException("OutputStream is null, connection may have been closed");
        }
        sendInChunks(os, data);
    }

    private void sendInChunks(OutputStream os, byte[] data) throws IOException {
        int CHUNK_SIZE = 16 * 1024;
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(CHUNK_SIZE, data.length - offset);
            os.write(data, offset, len);
            os.flush();
            offset += len;
        }
    }

    @Override
    public void printRawData(final String rawBase64Data, final Callback errorCallback) {
        mPrintExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!ensureConnection(errorCallback)) {
                    return;
                }
                Log.v(getLogTag(), "start to print raw data " + rawBase64Data);
                try {
                    byte[] bytes = Base64.decode(rawBase64Data, Base64.DEFAULT);
                    sendData(bytes);
                } catch (Exception e) {
                    Log.e(getLogTag(), "failed to print data: " + e.getMessage());
                    e.printStackTrace();
                    errorCallback.invoke("failed to print data: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void printImageData(final String imageUrl, final int imageWidth, final String align,
            final Callback errorCallback) {
        mPrintExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmapImage = UtilsImage.getBitmapFromURL(imageUrl);

                if (bitmapImage == null) {
                    errorCallback.invoke("image not found");
                    return;
                }

                if (!ensureConnection(errorCallback)) {
                    return;
                }

                Log.v(getLogTag(), "start to print image data (fast raster mode)");

                try {
                    byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth);
                    sendData(getAlignCommand(align));
                    sendData(rasterData);
                } catch (Exception e) {
                    Log.e(getLogTag(), "failed to print image data: " + e.getMessage());
                    e.printStackTrace();
                    errorCallback.invoke("failed to print image data: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void printImageBase64(final Bitmap bitmapImage, final int imageWidth, final String align,
            final Callback errorCallback) {
        mPrintExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (bitmapImage == null) {
                    errorCallback.invoke("image not found");
                    return;
                }

                if (!ensureConnection(errorCallback)) {
                    return;
                }

                Log.v(getLogTag(), "start to print image base64 (fast raster mode)");

                try {
                    byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth);
                    sendData(getAlignCommand(align));
                    sendData(rasterData);
                } catch (Exception e) {
                    Log.e(getLogTag(), "failed to print image base64: " + e.getMessage());
                    e.printStackTrace();
                    errorCallback.invoke("failed to print image base64: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Returns the ESC/POS alignment command byte array for the given align string.
     *
     * @param align "left" | "right" | "center" (default)
     */
    private byte[] getAlignCommand(String align) {
        if ("left".equals(align))
            return UtilsImage.LEFT_ALIGN;
        if ("right".equals(align))
            return UtilsImage.RIGHT_ALIGN;
        return UtilsImage.CENTER_ALIGN; // "center" or anything else → default center
    }
}
