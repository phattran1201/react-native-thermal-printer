package com.harold.rn.printer.adapter;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Callback;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract base class for printer adapters that use OutputStream for printing.
 * Provides shared logic for safe OutputStream handling, common print
 * operations, and auto-reconnect functionality. Used by BLEPrinterAdapter and
 * NetPrinterAdapter.
 */
public abstract class BaseStreamPrinterAdapter implements PrinterAdapter {

    private OutputStream mOutputStream;

    // Auto-reconnect settings
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final int RECONNECT_DELAY_MS = 500;

    /**
     * Get the log tag for this adapter.
     */
    protected abstract String getLogTag();

    /**
     * Check if the underlying connection is valid and connected.
     *
     * @return true if connection is valid
     */
    protected abstract boolean isConnectionValid();

    /**
     * Get the raw OutputStream from the underlying socket/connection.
     *
     * @return OutputStream or null if not available
     * @throws IOException if there's an error getting the stream
     */
    protected abstract OutputStream getRawOutputStream() throws IOException;

    /**
     * Get the error message when connection is not available.
     */
    protected abstract String getConnectionErrorMessage();

    /**
     * Attempt to reconnect to the last connected device. Child classes should
     * implement this to store connection info and reconnect.
     *
     * @return true if reconnect was successful, false otherwise
     */
    protected abstract boolean tryReconnect();

    /**
     * Safely get the output stream with validation and caching. Thread-safe
     * implementation with proper null checking.
     *
     * @return OutputStream if available, null otherwise
     */
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

    /**
     * Clear the cached output stream. Should be called when closing connection.
     */
    protected synchronized void clearOutputStream() {
        mOutputStream = null;
    }

    /**
     * Ensure connection is valid, attempting to reconnect if needed.
     *
     * @return true if connected (or successfully reconnected), false otherwise
     */
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

    @Override
    public void printRawData(String rawBase64Data, Callback errorCallback) {
        // Try to ensure connection (with auto-reconnect if needed)
        if (!ensureConnection()) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        OutputStream printerOutputStream = getOutputStream();
        if (printerOutputStream == null) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        final String rawData = rawBase64Data;
        Log.v(getLogTag(), "start to print raw data " + rawBase64Data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = Base64.decode(rawData, Base64.DEFAULT);
                try {
                    OutputStream os = getOutputStream();
                    if (os == null) {
                        Log.e(getLogTag(), "OutputStream is null, connection may have been closed");
                        return;
                    }
                    os.write(bytes, 0, bytes.length);
                    os.flush();
                } catch (IOException e) {
                    Log.e(getLogTag(), "failed to print data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        final Bitmap bitmapImage = UtilsImage.getBitmapFromURL(imageUrl);

        if (bitmapImage == null) {
            errorCallback.invoke("image not found");
            return;
        }

        // Try to ensure connection (with auto-reconnect if needed)
        if (!ensureConnection()) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        OutputStream printerOutputStream = getOutputStream();
        if (printerOutputStream == null) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        Log.v(getLogTag(), "start to print image data (fast raster mode)");

        try {
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            printerOutputStream.write(UtilsImage.CENTER_ALIGN);
            printerOutputStream.write(rasterData);
            printerOutputStream.write(UtilsImage.LINE_FEED);
            printerOutputStream.flush();
        } catch (IOException e) {
            Log.e(getLogTag(), "failed to print image data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void printImageBase64(final Bitmap bitmapImage, int imageWidth, int imageHeight, Callback errorCallback) {
        if (bitmapImage == null) {
            errorCallback.invoke("image not found");
            return;
        }

        // Try to ensure connection (with auto-reconnect if needed)
        if (!ensureConnection()) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        OutputStream printerOutputStream = getOutputStream();
        if (printerOutputStream == null) {
            errorCallback.invoke(getConnectionErrorMessage());
            return;
        }

        Log.v(getLogTag(), "start to print image base64 (fast raster mode)");

        try {
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            printerOutputStream.write(UtilsImage.CENTER_ALIGN);
            printerOutputStream.write(rasterData);
            printerOutputStream.write(UtilsImage.LINE_FEED);
            printerOutputStream.flush();
        } catch (IOException e) {
            Log.e(getLogTag(), "failed to print image base64: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
