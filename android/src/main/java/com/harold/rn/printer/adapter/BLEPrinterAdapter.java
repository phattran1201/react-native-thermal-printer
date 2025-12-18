package com.harold.rn.printer.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BLEPrinterAdapter implements PrinterAdapter {

    private static BLEPrinterAdapter mInstance;

    private final String LOG_TAG = "RNBLEPrinter";

    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private ReactApplicationContext mContext;

    private BLEPrinterAdapter() {
    }

    public static BLEPrinterAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new BLEPrinterAdapter();
        }
        return mInstance;
    }

    @Override
    public void init(ReactApplicationContext reactContext, Callback successCallback, Callback errorCallback) {
        this.mContext = reactContext;
        BluetoothAdapter bluetoothAdapter = getBTAdapter();
        if (bluetoothAdapter == null) {
            errorCallback.invoke("No bluetooth adapter available");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            errorCallback.invoke("bluetooth adapter is not enabled");
            return;
        } else {
            successCallback.invoke();
        }

    }

    private static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public List<PrinterDevice> getDeviceList(Callback errorCallback) {
        BluetoothAdapter bluetoothAdapter = getBTAdapter();
        List<PrinterDevice> printerDevices = new ArrayList<>();
        if (bluetoothAdapter == null) {
            errorCallback.invoke("No bluetooth adapter available");
            return printerDevices;
        }
        if (!bluetoothAdapter.isEnabled()) {
            errorCallback.invoke("bluetooth is not enabled");
            return printerDevices;
        }
        Set<BluetoothDevice> pairedDevices = getBTAdapter().getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            printerDevices.add(new BLEPrinterDevice(device));
        }
        return printerDevices;
    }

    @Override
    public void selectDevice(PrinterDeviceId printerDeviceId, Callback successCallback, Callback errorCallback) {
        BluetoothAdapter bluetoothAdapter = getBTAdapter();
        if (bluetoothAdapter == null) {
            errorCallback.invoke("No bluetooth adapter available");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            errorCallback.invoke("bluetooth is not enabled");
            return;
        }
        BLEPrinterDeviceId blePrinterDeviceId = (BLEPrinterDeviceId) printerDeviceId;
        if (this.mBluetoothDevice != null) {
            if (this.mBluetoothDevice.getAddress().equals(blePrinterDeviceId.getInnerMacAddress())
                    && this.mBluetoothSocket != null) {
                Log.v(LOG_TAG, "do not need to reconnect");
                successCallback.invoke(new BLEPrinterDevice(this.mBluetoothDevice).toRNWritableMap());
                return;
            } else {
                closeConnectionIfExists();
            }
        }
        Set<BluetoothDevice> pairedDevices = getBTAdapter().getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(blePrinterDeviceId.getInnerMacAddress())) {

                try {
                    connectBluetoothDevice(device, false);
                    successCallback.invoke(new BLEPrinterDevice(this.mBluetoothDevice).toRNWritableMap());
                    return;
                } catch (IOException e) {
                    try {
                        connectBluetoothDevice(device, true);
                        successCallback.invoke(new BLEPrinterDevice(this.mBluetoothDevice).toRNWritableMap());
                        return;
                    } catch (IOException er) {
                        er.printStackTrace();
                        errorCallback.invoke(er.getMessage());
                        return;
                    }
                }
            }
        }
        String errorText = "Can not find the specified printing device, please perform Bluetooth pairing in the system settings first.";
        Toast.makeText(this.mContext, errorText, Toast.LENGTH_LONG).show();
        errorCallback.invoke(errorText);
        return;
    }

    private void connectBluetoothDevice(BluetoothDevice device, Boolean retry) throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        if (retry) {
            try {
                this.mBluetoothSocket = (BluetoothSocket) device.getClass()
                        .getMethod("createRfcommSocket", new Class[] { int.class }).invoke(device, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            this.mBluetoothSocket.connect();
        }

        this.mBluetoothDevice = device;// 最后一步执行

    }

    @Override
    public void closeConnectionIfExists() {
        try {
            if (this.mBluetoothSocket != null) {
                this.mBluetoothSocket.close();
                this.mBluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.mBluetoothDevice != null) {
            this.mBluetoothDevice = null;
        }
    }

    @Override
    public void printRawData(String rawBase64Data, Callback errorCallback) {
        if (this.mBluetoothSocket == null) {
            errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
            return;
        }
        final String rawData = rawBase64Data;
        final BluetoothSocket socket = this.mBluetoothSocket;
        Log.v(LOG_TAG, "start to print raw data " + rawBase64Data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = Base64.decode(rawData, Base64.DEFAULT);
                try {
                    OutputStream printerOutputStream = socket.getOutputStream();
                    printerOutputStream.write(bytes, 0, bytes.length);
                    printerOutputStream.flush();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "failed to print data" + rawData);
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

        if (this.mBluetoothSocket == null) {
            errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
            return;
        }

        Log.v(LOG_TAG, "start to print image data (fast raster mode) " + bitmapImage);
        final BluetoothSocket socket = this.mBluetoothSocket;

        try {
            OutputStream printerOutputStream = socket.getOutputStream();

            // Prepare image raster data using shared utility
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            // Center align before printing
            printerOutputStream.write(UtilsImage.CENTER_ALIGN);

            // Send ALL image data in ONE transfer - much faster!
            printerOutputStream.write(rasterData);

            // Line feed after image
            printerOutputStream.write(UtilsImage.LINE_FEED);

            printerOutputStream.flush();
        } catch (IOException e) {
            Log.e(LOG_TAG, "failed to print data");
            e.printStackTrace();
        }
    }

    @Override
    public void printImageBase64(final Bitmap bitmapImage, int imageWidth, int imageHeight, Callback errorCallback) {
        if (bitmapImage == null) {
            errorCallback.invoke("image not found");
            return;
        }

        if (this.mBluetoothSocket == null) {
            errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
            return;
        }

        Log.v(LOG_TAG, "start to print image base64 (fast raster mode) " + bitmapImage);
        final BluetoothSocket socket = this.mBluetoothSocket;

        try {
            OutputStream printerOutputStream = socket.getOutputStream();

            // Prepare image raster data using shared utility
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            // Center align before printing
            printerOutputStream.write(UtilsImage.CENTER_ALIGN);

            // Send ALL image data in ONE transfer - much faster!
            printerOutputStream.write(rasterData);

            // Line feed after image
            printerOutputStream.write(UtilsImage.LINE_FEED);

            printerOutputStream.flush();
        } catch (IOException e) {
            Log.e(LOG_TAG, "failed to print data");
            e.printStackTrace();
        }
    }
}
