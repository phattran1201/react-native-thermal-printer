package com.harold.rn.printer.adapter;

import android.os.Build;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Bitmap;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

public class USBPrinterAdapter implements PrinterAdapter {
    @SuppressLint("StaticFieldLeak")
    private static USBPrinterAdapter mInstance;

    private final String LOG_TAG = "RNUSBPrinter";
    private Context mContext;
    private UsbManager mUSBManager;
    private PendingIntent mPermissionIndent;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mEndPoint;
    private static final String ACTION_USB_PERMISSION = "com.harold.rn.USBPrinter.USB_PERMISSION";
    private static final String EVENT_USB_DEVICE_ATTACHED = "usbAttached";

    private USBPrinterAdapter() {
    }

    public static USBPrinterAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new USBPrinterAdapter();
        }
        return mInstance;
    }

    private final BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        assert usbDevice != null;
                        Log.i(LOG_TAG,
                                "success to grant permission for device " + usbDevice.getDeviceId() + ", vendor_id: "
                                        + usbDevice.getVendorId() + " product_id: " + usbDevice.getProductId());
                        mUsbDevice = usbDevice;
                    } else {
                        assert usbDevice != null;
                        Toast.makeText(context,
                                "User refuses to obtain USB device permissions" + usbDevice.getDeviceName(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (mUsbDevice != null) {
                    Toast.makeText(context, "USB device has been turned off", Toast.LENGTH_LONG).show();
                    closeConnectionIfExists();
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    if (mContext != null) {
                        ((ReactApplicationContext) mContext)
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(EVENT_USB_DEVICE_ATTACHED, null);
                    }
                }
            }
        }
    };

    public void init(ReactApplicationContext reactContext, Callback successCallback, Callback errorCallback) {
        this.mContext = reactContext;
        this.mUSBManager = (UsbManager) this.mContext.getSystemService(Context.USB_SERVICE);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 34) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        this.mPermissionIndent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), flags);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        if (Build.VERSION.SDK_INT >= 33) {
            mContext.registerReceiver(mUsbDeviceReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            mContext.registerReceiver(mUsbDeviceReceiver, filter);
        }
        Log.v(LOG_TAG, "RNUSBPrinter initialized");
        successCallback.invoke();
    }

    public void closeConnectionIfExists() {
        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection.releaseInterface(mUsbInterface);
            mUsbDeviceConnection.close();
            mUsbInterface = null;
            mEndPoint = null;
            mUsbDeviceConnection = null;
        }
    }

    public List<PrinterDevice> getDeviceList(Callback errorCallback) {
        List<PrinterDevice> lists = new ArrayList<>();
        if (mUSBManager == null) {
            errorCallback.invoke("USBManager is not initialized while get device list");
            return lists;
        }

        for (UsbDevice usbDevice : mUSBManager.getDeviceList().values()) {
            lists.add(new USBPrinterDevice(usbDevice));
        }
        return lists;
    }

    @Override
    public void selectDevice(PrinterDeviceId printerDeviceId, Callback successCallback, Callback errorCallback) {
        if (mUSBManager == null) {
            errorCallback.invoke("USBManager is not initialized before select device");
            return;
        }

        USBPrinterDeviceId usbPrinterDeviceId = (USBPrinterDeviceId) printerDeviceId;
        if (mUsbDevice != null && mUsbDevice.getVendorId() == usbPrinterDeviceId.getVendorId()
                && mUsbDevice.getProductId() == usbPrinterDeviceId.getProductId()) {
            Log.i(LOG_TAG, "already selected device, do not need repeat to connect");
            if (!mUSBManager.hasPermission(mUsbDevice)) {
                closeConnectionIfExists();
                mUSBManager.requestPermission(mUsbDevice, mPermissionIndent);
            }
            successCallback.invoke(new USBPrinterDevice(mUsbDevice).toRNWritableMap());
            return;
        }
        closeConnectionIfExists();
        if (mUSBManager.getDeviceList().size() == 0) {
            errorCallback.invoke("Device list is empty, can not choose device");
            return;
        }
        for (UsbDevice usbDevice : mUSBManager.getDeviceList().values()) {
            if (usbDevice.getVendorId() == usbPrinterDeviceId.getVendorId()
                    && usbDevice.getProductId() == usbPrinterDeviceId.getProductId()) {
                Log.v(LOG_TAG, "request for device: vendor_id: " + usbPrinterDeviceId.getVendorId() + ", product_id: "
                        + usbPrinterDeviceId.getProductId());
                closeConnectionIfExists();
                mUSBManager.requestPermission(usbDevice, mPermissionIndent);
                successCallback.invoke(new USBPrinterDevice(usbDevice).toRNWritableMap());
                return;
            }
        }

        errorCallback.invoke("can not find specified device");
        return;
    }

    private boolean openConnection() {
        if (mUsbDevice == null) {
            Log.e(LOG_TAG, "USB Deivce is not initialized");
            return false;
        }
        if (mUSBManager == null) {
            Log.e(LOG_TAG, "USB Manager is not initialized");
            return false;
        }

        if (mUsbDeviceConnection != null) {
            Log.i(LOG_TAG, "USB Connection already connected");
            return true;
        }

        UsbInterface usbInterface = mUsbDevice.getInterface(0);
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            final UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    UsbDeviceConnection usbDeviceConnection = mUSBManager.openDevice(mUsbDevice);
                    if (usbDeviceConnection == null) {
                        Log.e(LOG_TAG, "failed to open USB Connection");
                        return false;
                    }
                    if (usbDeviceConnection.claimInterface(usbInterface, true)) {

                        mEndPoint = ep;
                        mUsbInterface = usbInterface;
                        mUsbDeviceConnection = usbDeviceConnection;
                        Log.i(LOG_TAG, "Device connected");
                        return true;
                    } else {
                        usbDeviceConnection.close();
                        Log.e(LOG_TAG, "failed to claim usb connection");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void printRawData(String data, Callback errorCallback) {
        final String rawData = data;
        Log.v(LOG_TAG, "start to print raw data " + data);
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = Base64.decode(rawData, Base64.DEFAULT);
                    int b = mUsbDeviceConnection.bulkTransfer(mEndPoint, bytes, bytes.length, 100000);
                    Log.i(LOG_TAG, "Return Status: b-->" + b);
                }
            }).start();
        } else {
            String msg = "failed to connected to device";
            Log.v(LOG_TAG, msg);
            errorCallback.invoke(msg);
        }
    }



    @Override
    public void printImageData(final String imageUrl, int imageWidth, int imageHeight, Callback errorCallback) {
        final Bitmap bitmapImage = UtilsImage.getBitmapFromURL(imageUrl);

        if (bitmapImage == null) {
            errorCallback.invoke("image not found");
            return;
        }

        Log.v(LOG_TAG, "start to print image data (fast raster mode) " + bitmapImage);
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");

            // Prepare image raster data using shared utility
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            // Center align before printing
            mUsbDeviceConnection.bulkTransfer(mEndPoint, UtilsImage.CENTER_ALIGN, UtilsImage.CENTER_ALIGN.length, 100000);

            // Send ALL image data in ONE bulk transfer - much faster!
            mUsbDeviceConnection.bulkTransfer(mEndPoint, rasterData, rasterData.length, 100000);

            // Line feed after image
            mUsbDeviceConnection.bulkTransfer(mEndPoint, UtilsImage.LINE_FEED, UtilsImage.LINE_FEED.length, 100000);
        } else {
            String msg = "failed to connected to device";
            Log.v(LOG_TAG, msg);
            errorCallback.invoke(msg);
        }

    }

    @Override
    public void printImageBase64(final Bitmap bitmapImage, int imageWidth, int imageHeight, Callback errorCallback) {
        if (bitmapImage == null) {
            errorCallback.invoke("image not found");
            return;
        }

        Log.v(LOG_TAG, "start to print image base64 (fast raster mode) " + bitmapImage);
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");

            // Prepare image raster data using shared utility
            byte[] rasterData = UtilsImage.prepareImageRasterData(bitmapImage, imageWidth, imageHeight);

            // Center align before printing
            mUsbDeviceConnection.bulkTransfer(mEndPoint, UtilsImage.CENTER_ALIGN, UtilsImage.CENTER_ALIGN.length, 100000);

            // Send ALL image data in ONE bulk transfer - much faster!
            mUsbDeviceConnection.bulkTransfer(mEndPoint, rasterData, rasterData.length, 100000);

            // Line feed after image
            mUsbDeviceConnection.bulkTransfer(mEndPoint, UtilsImage.LINE_FEED, UtilsImage.LINE_FEED.length, 100000);
        } else {
            String msg = "failed to connected to device";
            Log.v(LOG_TAG, msg);
            errorCallback.invoke(msg);
        }

    }
}
