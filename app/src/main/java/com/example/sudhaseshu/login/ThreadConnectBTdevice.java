package com.example.sudhaseshu.login;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.android.gms.internal.zzahn;

import java.io.IOException;

import static com.example.sudhaseshu.login.Bluetooth.TAG;
import static com.example.sudhaseshu.login.Bluetooth.myThreadConnected;
import static com.example.sudhaseshu.login.Bluetooth.myUUID;

public class ThreadConnectBTdevice extends Thread {

    private BluetoothSocket bluetoothSocket = null;
    private final BluetoothDevice bluetoothDevice;


    public ThreadConnectBTdevice(BluetoothDevice device) {
        bluetoothDevice = device;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            Log.i(TAG,"bluetoothSocket: \n" + bluetoothSocket);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            bluetoothSocket.connect();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();

            final String eMessage = e.getMessage();
            zzahn.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.i(TAG,"something wrong bluetoothSocket.connect(): \n" + eMessage);
                }
            });

            try {
                bluetoothSocket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        if(success){
            //connect successful
            final String msgconnected = "connect successful:\n"
                    + "BluetoothSocket: " + bluetoothSocket + "\n"
                    + "BluetoothDevice: " + bluetoothDevice;

            zzahn.runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    Log.i(TAG,msgconnected);

                }});

            startThreadConnected(bluetoothSocket);
        }else{
            //fail
        }
    }
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    public void cancel() {

        Log.i(TAG,
                "close bluetoothSocket");

        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}