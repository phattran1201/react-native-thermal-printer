package com.harold.rn.printer.adapter;

import com.facebook.react.bridge.WritableMap;

public interface PrinterDevice {

    public PrinterDeviceId getPrinterDeviceId();

    public WritableMap toRNWritableMap();

}
