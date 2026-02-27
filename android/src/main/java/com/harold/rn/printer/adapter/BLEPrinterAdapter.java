package com.harold.rn.printer.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

public class BLEPrinterAdapter extends BasePrinterAdapter {

    private static BLEPrinterAdapter mInstance;

    private final String LOG_TAG = "RNBLEPrinter";

    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private ReactApplicationContext mContext;

    // Store last connected device for auto-reconnect
    private String lastConnectedMacAddress = null;

    private BLEPrinterAdapter() {
    }

    public static BLEPrinterAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new BLEPrinterAdapter();
        }
        return mInstance;
    }

    // ==================== BaseStreamPrinterAdapter Implementation
    // ====================

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected boolean isConnectionValid() {
        return mBluetoothSocket != null && mBluetoothSocket.isConnected();
    }

    @Override
    protected OutputStream getRawOutputStream() throws IOException {
        return mBluetoothSocket != null ? mBluetoothSocket.getOutputStream() : null;
    }

    @Override
    protected String getConnectionErrorMessage() {
        return "bluetooth connection is not built, may be you forgot to connectPrinter";
    }

    @Override
    protected boolean tryReconnect() {
        if (lastConnectedMacAddress == null) {
            Log.w(LOG_TAG, "No previous connection to reconnect to");
            return false;
        }

        BluetoothAdapter bluetoothAdapter = getBTAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(LOG_TAG, "Bluetooth adapter not available or not enabled");
            return false;
        }

        // Find the device by MAC address
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(lastConnectedMacAddress)) {
                Log.i(LOG_TAG, "Found device, attempting to reconnect: " + lastConnectedMacAddress);

                // Close existing connection first
                closeConnectionIfExists();

                try {
                    connectBluetoothDevice(device, false);
                    return true;
                } catch (IOException e) {
                    Log.w(LOG_TAG, "First connection attempt failed, trying fallback method");
                    try {
                        connectBluetoothDevice(device, true);
                        return true;
                    } catch (IOException er) {
                        Log.e(LOG_TAG, "Reconnect failed: " + er.getMessage());
                        return false;
                    }
                }
            }
        }

        Log.e(LOG_TAG, "Device with MAC " + lastConnectedMacAddress + " not found in paired devices");
        return false;
    }

    // ==================== PrinterAdapter Implementation ====================

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
            if (isPrinterDevice(device)) {
                printerDevices.add(new BLEPrinterDevice(device));
            }
        }
        return printerDevices;
    }

    /**
     * Check if Bluetooth device is likely a printer. Filters by BluetoothClass
     * (IMAGING/PRINTER) or common printer name patterns.
     */
    private boolean isPrinterDevice(BluetoothDevice device) {
        // Check by BluetoothClass
        if (device.getBluetoothClass() != null) {
            int majorClass = device.getBluetoothClass().getMajorDeviceClass();
            int deviceClass = device.getBluetoothClass().getDeviceClass();

            // Major class IMAGING (0x0600) includes printers
            if (majorClass == 0x0600) {
                return true;
            }

            // Check for printer-specific device class
            // 0x0680 = Printer in IMAGING major class
            if (deviceClass == 0x0680) {
                return true;
            }
        }

        // Fallback: check device name for common printer keywords
        String name = device.getName();
        if (name != null) {
            String lowerName = name.toLowerCase();
            return lowerName.contains("printer") || lowerName.contains("print") || lowerName.contains("pos")
                    || lowerName.contains("thermal") || lowerName.contains("receipt") || lowerName.contains("label")
                    || lowerName.contains("esc") || lowerName.contains("tsc") || lowerName.contains("epson")
                    || lowerName.contains("zebra") || lowerName.contains("star") || lowerName.contains("bixolon")
                    || lowerName.contains("xprinter") || lowerName.contains("hprt") || lowerName.contains("sewoo");
        }

        return false;
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
                if (this.mBluetoothSocket != null) {
                    this.mBluetoothSocket.connect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Failed to create or connect socket: " + e.getMessage());
            }
        } else {
            this.mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            this.mBluetoothSocket.connect();
        }

        this.mBluetoothDevice = device;

        // Store MAC address for auto-reconnect
        this.lastConnectedMacAddress = device.getAddress();
        Log.i(LOG_TAG, "Connected to device, saved MAC for auto-reconnect: " + lastConnectedMacAddress);
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

        // Clear cached OutputStream from base class
        clearOutputStream();

        if (this.mBluetoothDevice != null) {
            this.mBluetoothDevice = null;
        }
        // Note: Do NOT clear lastConnectedMacAddress here - we need it for reconnect
    }
}
