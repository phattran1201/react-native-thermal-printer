package com.harold.rn.printer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.harold.rn.printer.adapter.BLEPrinterAdapter;
import com.harold.rn.printer.adapter.BLEPrinterDeviceId;
import com.harold.rn.printer.adapter.PrinterDevice;

import java.util.List;

public class RNBLEPrinterModule extends RNBasePrinterModule {

    public RNBLEPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    @Override
    public void init(Callback successCallback, Callback errorCallback) {
        this.adapter = BLEPrinterAdapter.getInstance();
        this.adapter.init(reactContext, successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void closeConn() {
        if (this.adapter == null) {
            this.adapter = BLEPrinterAdapter.getInstance();
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
    public void connectPrinter(String innerAddress, Callback successCallback, Callback errorCallback) {
        adapter.selectDevice(BLEPrinterDeviceId.valueOf(innerAddress), successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback) {
        super.printRawData(base64Data, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        super.printImageData(imageUrl, imageWidth, imageHeight, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageBase64(String base64ImageData, int imageWidth, int imageHeight, Callback errorCallback) {
        super.printImageBase64(base64ImageData, imageWidth, imageHeight, errorCallback);
    }

    @Override
    public String getName() {
        return "RNBLEPrinter";
    }
}
