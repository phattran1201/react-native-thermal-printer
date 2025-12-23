package com.harold.rn.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.harold.rn.printer.adapter.NetPrinterAdapter;
import com.harold.rn.printer.adapter.NetPrinterDeviceId;
import com.harold.rn.printer.adapter.PrinterAdapter;

public class RNNetPrinterModule extends ReactContextBaseJavaModule implements RNPrinterModule {

    private PrinterAdapter adapter;
    private ReactApplicationContext reactContext;

    public RNNetPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    @Override
    public void init(Callback successCallback, Callback errorCallback) {
        this.adapter = NetPrinterAdapter.getInstance();
        this.adapter.init(reactContext, successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void closeConn() {
        if (this.adapter == null) {
            this.adapter = NetPrinterAdapter.getInstance();
        }
        this.adapter.closeConnectionIfExists();
    }

    @ReactMethod
    @Override
    public void getDeviceList(Callback successCallback, Callback errorCallback) {
        try {
            this.adapter.getDeviceList(errorCallback);
            successCallback.invoke();
        } catch (Exception ex) {
            errorCallback.invoke(ex.getMessage());
        }
        // this.adapter.getDeviceList(errorCallback);
    }

    @ReactMethod
    public void connectPrinter(String host, Integer port, Callback successCallback, Callback errorCallback) {
        adapter.selectDevice(NetPrinterDeviceId.valueOf(host, port), successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback) {
        adapter.printRawData(base64Data, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        Log.v("imageUrl", imageUrl);
        adapter.printImageData(imageUrl, imageWidth, imageHeight, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageBase64(String base64, int imageWidth, int imageHeight, Callback errorCallback) {
        Log.v("RNNetPrinter", "printImageBase64 called, base64 length: " + (base64 != null ? base64.length() : "null"));

        try {
            // Remove data URI prefix if present (e.g., "data:image/png;base64,")
            String base64Data = base64;
            if (base64Data != null && base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                Log.v("RNNetPrinter", "Stripped prefix, new length: " + base64Data.length());
            }

            // Remove any whitespace, newlines, or carriage returns that may cause "bad base64" error
            if (base64Data != null) {
                base64Data = base64Data.replaceAll("\\s+", "");
                Log.v("RNNetPrinter", "After whitespace removal, length: " + base64Data.length());
            }

            if (base64Data == null || base64Data.isEmpty()) {
                errorCallback.invoke("Base64 string is empty or null");
                return;
            }

            byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
            Log.v("RNNetPrinter", "Decoded bytes length: " + decodedString.length);

            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (decodedByte == null) {
                Log.e("RNNetPrinter", "BitmapFactory.decodeByteArray returned null");
                errorCallback.invoke("Failed to decode base64 image - invalid image data");
                return;
            }

            Log.v("RNNetPrinter", "Bitmap decoded: " + decodedByte.getWidth() + "x" + decodedByte.getHeight());

            adapter.printImageBase64(decodedByte, imageWidth, imageHeight, errorCallback);
        } catch (IllegalArgumentException e) {
            Log.e("RNNetPrinter", "IllegalArgumentException: " + e.getMessage());
            errorCallback.invoke("Invalid base64 string: " + e.getMessage());
        } catch (Exception e) {
            Log.e("RNNetPrinter", "Exception: " + e.getMessage());
            errorCallback.invoke("Error decoding image: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "RNNetPrinter";
    }
}
