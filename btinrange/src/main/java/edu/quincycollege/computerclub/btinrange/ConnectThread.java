package edu.quincycollege.computerclub.btinrange;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


/**
 * Created by Dark Cube on 1/9/2018.
 */

public class ConnectThread extends Thread {

    private BluetoothSocket socket;
    private BtInRange btInRangeCallbacks;

    public ConnectThread(BluetoothDevice device, BtInRange btInRangeCallbacks) {
        // Use a temporary object that is later assigned to socket
        // because socket is final.
        BluetoothSocket tmp = null;
        this.btInRangeCallbacks = btInRangeCallbacks;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            UUID uid = UUID.randomUUID();

            tmp = device.createRfcommSocketToServiceRecord(uid);
            //tmp = device.createInsecureRfcommSocketToServiceRecord(uid);

            Log.v("Msg", "Random uuid: " + uid.toString());

        } catch (IOException e) {
            Log.e("Msg", "Socket's create() method failed", e);
        }
        socket = tmp;
    }

    public void run() {

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.

            Log.v("Msg", "getRemoteDevice(): "+ socket.getRemoteDevice().getName());
            Log.v("Msg", "is Conneted(): "+ socket.isConnected());

            //synchronized(this) {
                Log.v("Msg", "socket try connecting");
                socket.connect();
                Log.v("Msg", "After socket.connect()");
            //}


        //} catch (InterruptedException interruptedEx) {
        //    Log.v("Msg", "InterruptedException ", interruptedEx);
        }

        catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Log.v("Msg", "Unable to connect", connectException);
            try {
                Log.v("Msg", "IOException socket close()");
                Log.v("Msg", "is Conneted(): "+ socket.isConnected());
                socket.close();
                Log.v("Msg", "socket.closing");
                //Thread.sleep(TIMEOUT);
                //Log.v("Msg", "TIMEOUT passed");
                btInRangeCallbacks.connectionTimeout();

            } catch (IOException closeException) {
                Log.v("Msg", "Could not close the client socket", closeException);
            //} catch (InterruptedException interruptedEx) {
            //    Log.v("Msg", "Sleep Stoped", interruptedEx);
            //    btInRangeCallbacks.connectionTimeout();
            }
            return;
        }

        manageMyConnectedSocket(socket);
        cancel();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            Log.v("Msg", "cancel() socket close()");
            socket.close();
        } catch (IOException e) {
            Log.e("Msg", "Could not close the client socket", e);
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mSocket){
        Log.v("Msg", "Client Got connected");
    }
}