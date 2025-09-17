package com.harold.rn.printer;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;

public interface RNPrinterModule {

    public void init(Callback successCallback, Callback errorCallback);

    public void closeConn();

    public void getDeviceList(Callback successCallback, Callback errorCallback);

    @ReactMethod
    public void printRawData(String base64Data, Callback errorCallback) ;

    @ReactMethod
    public void printImageData(String imageUrl, int imageWidth, int imageHeight, Callback errorCallback);

    @ReactMethod
    public void printImageBase64(String base64, int imageWidth, int imageHeight, Callback errorCallback) ;
}

