package com.harold.rn.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.harold.rn.printer.adapter.PrinterAdapter;
import com.harold.rn.printer.adapter.PrinterDevice;
import com.harold.rn.printer.adapter.USBPrinterAdapter;
import com.harold.rn.printer.adapter.USBPrinterDeviceId;

import java.util.List;

public class RNUSBPrinterModule extends ReactContextBaseJavaModule implements RNPrinterModule {

    protected ReactApplicationContext reactContext;

    protected PrinterAdapter adapter;

    public RNUSBPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    @Override
    public void init(Callback successCallback, Callback errorCallback) {
        this.adapter = USBPrinterAdapter.getInstance();
        this.adapter.init(reactContext, successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void closeConn() {
        if (this.adapter == null) {
            this.adapter = USBPrinterAdapter.getInstance();
        }
        this.adapter.closeConnectionIfExists();
    }

    @ReactMethod
    @Override
    public void getDeviceList(Callback successCallback, Callback errorCallback) {
        List<PrinterDevice> printerDevices = adapter.getDeviceList(errorCallback);
        WritableArray pairedDeviceList = Arguments.createArray();
        if (printerDevices.size() > 0) {
            for (PrinterDevice printerDevice : printerDevices) {
                pairedDeviceList.pushMap(printerDevice.toRNWritableMap());
            }
            successCallback.invoke(pairedDeviceList);
        } else {
            errorCallback.invoke("No Device Found");
        }
    }

    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback) {
        adapter.printRawData(base64Data, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        adapter.printImageData(imageUrl, imageWidth, imageHeight, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageBase64(String base64, int imageWidth, int imageHeight, Callback errorCallback) {
        try {
            // Remove data URI prefix if present (e.g., "data:image/png;base64,")
            String base64Data = base64;
            if (base64Data != null && base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            // Remove any whitespace, newlines, or carriage returns that may cause "bad base64" error
            if (base64Data != null) {
                base64Data = base64Data.replaceAll("\\s+", "");
            }

            if (base64Data == null || base64Data.isEmpty()) {
                errorCallback.invoke("Base64 string is empty or null");
                return;
            }

            byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (decodedByte == null) {
                errorCallback.invoke("Failed to decode base64 image - invalid image data");
                return;
            }

            adapter.printImageBase64(decodedByte, imageWidth, imageHeight, errorCallback);
        } catch (IllegalArgumentException e) {
            errorCallback.invoke("Invalid base64 string: " + e.getMessage());
        } catch (Exception e) {
            errorCallback.invoke("Error decoding image: " + e.getMessage());
        }
    }

    @ReactMethod
    public void connectPrinter(Integer vendorId, Integer productId, Callback successCallback, Callback errorCallback) {
        adapter.selectDevice(USBPrinterDeviceId.valueOf(vendorId, productId), successCallback, errorCallback);
    }

    @Override
    public String getName() {
        return "RNUSBPrinter";
    }
}
