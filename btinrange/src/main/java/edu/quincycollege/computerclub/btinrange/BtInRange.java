package edu.quincycollege.computerclub.btinrange;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by Dark Cube on 1/9/2018.
 */

public class BtInRange {

    private BluetoothManagerCallBacks bTCallBacks;
    private BroadcastReceiver receiver;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String targetDeviceMac;

    private Thread checkNetworkthread;
    private boolean deviceConnectedResult;

    private boolean isPause;
    private int intervalMillisec = 60000;

    public BtInRange(BluetoothManagerCallBacks bTCallBacks)
    {
        this.bTCallBacks = bTCallBacks;
        receiver = null;
        targetDeviceMac = "";
        checkNetworkthread = null;
        isPause = true;
    }

    public void setTargetDeviceByMac(String mac) {
        targetDeviceMac = mac;
    }
    public void setIntervalMillisec(int millisec) {
        if (millisec<30000)
            intervalMillisec = 30000;
        else
            intervalMillisec = millisec;
    }

    public BroadcastReceiver getReceiver() {
        if (receiver == null) {
            initialReceiver();
        }
        return receiver;
    }

    public void startChecker() {
        if (isPause) {
            isPause = false;
            runChecker();
        }
    }

    public void stopChecker() {
        isPause = true;
    }

    private void delayChecker() {
        if (isPause)
            return;
        // Check again after 30s
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after ??ms
                runChecker();
            }
        }, intervalMillisec);
    }

    public void connectionTimeout() {
        if (!deviceConnectedResult) {
            bTCallBacks.deviceConnectionResult(deviceConnectedResult);
            Log.v("Msg", "Timeout, not connected");

            // Check again after 30s
            Looper.prepare();
            delayChecker();
            Looper.loop();
        }

    }

    private void initialReceiver() {
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //Do something with bluetooth device connection

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    // check if device matched first!!
                    if(device.getAddress().equals(targetDeviceMac))
                    {
                        // Device in range, bingo
                        //checkNetworkthread.interrupt();
                        deviceConnectedResult = true;
                        bTCallBacks.deviceConnectionResult(deviceConnectedResult);
                        Log.v("Msg", "BT connected "+ device.getName() + " looking for "+deviceHardwareAddress);
                        delayChecker();

                    }
                    else {
                        // Not target device.
                        Log.v("Msg", "ACTION_ACL_CONNECTED: " + deviceHardwareAddress);
                    }
                }
            }
        };
    }

    private BluetoothDevice getTargetDevice(String targetDeviceMac) {
        if(bluetoothAdapter == null)
            return null;

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        int numOfDevices = pairedDevices.size();
        Log.v("msg", "Number device:" + numOfDevices);
        if (numOfDevices > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String macAddr = device.getAddress();
                // Get target device object
                if (macAddr.equals(targetDeviceMac)) {
                    return device;
                }
            }
        }
        return null;
    }

    private void runChecker() {

        if (bluetoothAdapter == null){
            Log.v(TAG, "Bluetooth not available.");
            return;
        }

        bluetoothAdapter.cancelDiscovery();
        Log.v("Msg", "target mac:" + targetDeviceMac);
        BluetoothDevice targetDevice = getTargetDevice(targetDeviceMac);
        if (targetDevice == null) {
            deviceConnectedResult = false;
            bTCallBacks.deviceConnectionResult(deviceConnectedResult);
            delayChecker();
            return;
        }
        checkNetworkthread = new Thread(new ConnectThread(targetDevice, this));
        checkNetworkthread.start();
        deviceConnectedResult = false;

    }



}
