package com.harold.rn.printer.adapter;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

public class NetPrinterAdapter extends BaseStreamPrinterAdapter {
    private static NetPrinterAdapter mInstance;
    private ReactApplicationContext mContext;
    private final String LOG_TAG = "RNNetPrinter";
    private NetPrinterDevice mNetDevice;

    private final int[] PRINTER_ON_PORTS = { 9100 };
    private static final String EVENT_SCANNER_RESOLVED = "scannerResolved";
    private static final String EVENT_SCANNER_RUNNING = "scannerRunning";

    private Socket mSocket;
    private boolean isRunning = false;

    // Store last connected device for auto-reconnect
    private String lastConnectedHost = null;
    private int lastConnectedPort = -1;

    // Connection timeout in milliseconds
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    private NetPrinterAdapter() {
    }

    public static NetPrinterAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new NetPrinterAdapter();
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
        return mSocket != null && !mSocket.isClosed() && mSocket.isConnected();
    }

    @Override
    protected OutputStream getRawOutputStream() throws IOException {
        return mSocket != null ? mSocket.getOutputStream() : null;
    }

    @Override
    protected String getConnectionErrorMessage() {
        return "Net connection is not built, may be you forgot to connectPrinter";
    }

    @Override
    protected boolean tryReconnect() {
        if (lastConnectedHost == null || lastConnectedPort < 0) {
            Log.w(LOG_TAG, "No previous connection to reconnect to");
            return false;
        }

        Log.i(LOG_TAG, "Attempting to reconnect to " + lastConnectedHost + ":" + lastConnectedPort);

        // Close existing connection first
        closeConnectionIfExists();

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(lastConnectedHost, lastConnectedPort), CONNECTION_TIMEOUT_MS);

            if (socket.isConnected()) {
                this.mSocket = socket;
                this.mNetDevice = new NetPrinterDevice(lastConnectedHost, lastConnectedPort);
                Log.i(LOG_TAG, "Reconnect successful!");
                return true;
            } else {
                socket.close();
                Log.e(LOG_TAG, "Failed to reconnect: socket not connected");
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Reconnect failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== PrinterAdapter Implementation ====================

    @Override
    public void init(ReactApplicationContext reactContext, Callback successCallback, Callback errorCallback) {
        this.mContext = reactContext;
        successCallback.invoke();
    }

    @Override
    public List<PrinterDevice> getDeviceList(Callback errorCallback) {
        this.scan();
        return new ArrayList<>();
    }

    private void scan() {
        if (isRunning)
            return;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    isRunning = true;
                    emitEvent(EVENT_SCANNER_RUNNING, isRunning);

                    WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE);
                    String ipAddress = ipToString(wifiManager.getConnectionInfo().getIpAddress());
                    WritableArray array = Arguments.createArray();

                    String prefix = ipAddress.substring(0, ipAddress.lastIndexOf('.') + 1);
                    int suffix = Integer
                            .parseInt(ipAddress.substring(ipAddress.lastIndexOf('.') + 1, ipAddress.length()));

                    for (int i = 0; i <= 255; i++) {
                        if (i == suffix)
                            continue;
                        ArrayList<Integer> ports = getAvailablePorts(prefix + i);
                        if (!ports.isEmpty()) {
                            WritableMap payload = Arguments.createMap();

                            payload.putString("host", prefix + i);
                            payload.putInt("port", 9100);

                            array.pushMap(payload);
                        }
                    }

                    emitEvent(EVENT_SCANNER_RESOLVED, array);

                } catch (NullPointerException ex) {
                    Log.i(LOG_TAG, "No connection");
                } finally {
                    isRunning = false;
                    emitEvent(EVENT_SCANNER_RUNNING, isRunning);
                }
            }
        }).start();
    }

    private void emitEvent(String eventName, Object data) {
        if (mContext != null) {
            mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private ArrayList<Integer> getAvailablePorts(String address) {
        ArrayList<Integer> ports = new ArrayList<>();
        for (int port : PRINTER_ON_PORTS) {
            if (crunchifyAddressReachable(address, port))
                ports.add(port);
        }
        return ports;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean crunchifyAddressReachable(String address, int port) {
        try {

            try (Socket crunchifySocket = new Socket()) {
                crunchifySocket.connect(new InetSocketAddress(address, port), 100);
            }
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private String ipToString(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    @Override
    public void selectDevice(PrinterDeviceId printerDeviceId, Callback sucessCallback, Callback errorCallback) {
        NetPrinterDeviceId netPrinterDeviceId = (NetPrinterDeviceId) printerDeviceId;

        if (this.mSocket != null && !this.mSocket.isClosed() && this.mNetDevice != null
                && mNetDevice.getPrinterDeviceId().equals(netPrinterDeviceId)) {
            Log.i(LOG_TAG, "already selected device, do not need repeat to connect");
            sucessCallback.invoke(this.mNetDevice.toRNWritableMap());
            return;
        }

        try {
            closeConnectionIfExists();

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(netPrinterDeviceId.getHost(), netPrinterDeviceId.getPort()),
                    CONNECTION_TIMEOUT_MS);

            if (socket.isConnected()) {
                this.mSocket = socket;
                this.mNetDevice = new NetPrinterDevice(netPrinterDeviceId.getHost(), netPrinterDeviceId.getPort());

                // Store connection info for auto-reconnect
                this.lastConnectedHost = netPrinterDeviceId.getHost();
                this.lastConnectedPort = netPrinterDeviceId.getPort();
                Log.i(LOG_TAG, "Connected to device, saved for auto-reconnect: " + lastConnectedHost + ":"
                        + lastConnectedPort);

                sucessCallback.invoke(this.mNetDevice.toRNWritableMap());
            } else {
                socket.close();
                errorCallback.invoke("unable to build connection with host: " + netPrinterDeviceId.getHost()
                        + ", port: " + netPrinterDeviceId.getPort());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorCallback.invoke("failed to connect printer: " + e.getMessage());
        }
    }

    @Override
    public void closeConnectionIfExists() {
        if (this.mSocket != null) {
            if (!this.mSocket.isClosed()) {
                try {
                    this.mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.mSocket = null;
        }

        // Clear cached OutputStream from base class
        clearOutputStream();

        // Note: Do NOT clear lastConnectedHost/lastConnectedPort - we need them for
        // reconnect
    }
}
