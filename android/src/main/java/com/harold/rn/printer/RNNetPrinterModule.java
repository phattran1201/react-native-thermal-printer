package com.harold.rn.printer;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.harold.rn.printer.adapter.NetPrinterAdapter;
import com.harold.rn.printer.adapter.NetPrinterDeviceId;

public class RNNetPrinterModule extends RNBasePrinterModule {

    public RNNetPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
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
        return "RNNetPrinter";
    }
}
