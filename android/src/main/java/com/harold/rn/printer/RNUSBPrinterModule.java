package com.harold.rn.printer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.harold.rn.printer.adapter.PrinterDevice;
import com.harold.rn.printer.adapter.USBPrinterAdapter;
import com.harold.rn.printer.adapter.USBPrinterDeviceId;

import java.util.List;

public class RNUSBPrinterModule extends RNBasePrinterModule {

    public RNUSBPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
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
    public void connectPrinter(Integer vendorId, Integer productId, Callback successCallback, Callback errorCallback) {
        adapter.selectDevice(USBPrinterDeviceId.valueOf(vendorId, productId), successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback) {
        super.printRawData(base64Data, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, int imageWidth, String align, Callback errorCallback) {
        super.printImageData(imageUrl, imageWidth, align, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageBase64(String base64ImageData, int imageWidth, String align, Callback errorCallback) {
        super.printImageBase64(base64ImageData, imageWidth, align, errorCallback);
    }

    @Override
    public String getName() {
        return "RNUSBPrinter";
    }
}
