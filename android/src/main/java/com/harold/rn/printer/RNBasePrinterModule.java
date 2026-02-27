package com.harold.rn.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.harold.rn.printer.adapter.PrinterAdapter;

/**
 * Base abstract class for printer modules to reduce code duplication.
 * Provides default implementation for printing functions by delegating
 * them to the underlying PrinterAdapter instance.
 */
public abstract class RNBasePrinterModule extends ReactContextBaseJavaModule implements RNPrinterModule {

    protected PrinterAdapter adapter;
    protected ReactApplicationContext reactContext;

    public RNBasePrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback) {
        if (adapter != null) {
            adapter.printRawData(base64Data, errorCallback);
        } else {
            errorCallback.invoke("Printer adapter is not initialized");
        }
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        if (adapter != null) {
            adapter.printImageData(imageUrl, imageWidth, imageHeight, errorCallback);
        } else {
            errorCallback.invoke("Printer adapter is not initialized");
        }
    }

    @ReactMethod
    @Override
    public void printImageBase64(String base64ImageData, int imageWidth, int imageHeight, Callback errorCallback) {
        if (adapter != null) {
            try {
                byte[] decodedString = Base64.decode(base64ImageData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                adapter.printImageBase64(decodedByte, imageWidth, imageHeight, errorCallback);
            } catch (Exception e) {
                errorCallback.invoke("Failed to decode base64 image: " + e.getMessage());
            }
        } else {
            errorCallback.invoke("Printer adapter is not initialized");
        }
    }
}
