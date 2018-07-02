package com.example.sudhaseshu.login;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.sudhaseshu.login.Bluetooth.TAG;
import static com.google.android.gms.internal.zzahn.runOnUiThread;

public class ThreadConnected extends Thread {
    private final BluetoothSocket connectedBluetoothSocket;
    private final InputStream connectedInputStream;
    private final OutputStream connectedOutputStream;

    public ThreadConnected(BluetoothSocket socket) {
        connectedBluetoothSocket = socket;
        InputStream in = null;
        OutputStream out = null;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        connectedInputStream = in;
        connectedOutputStream = out;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = connectedInputStream.read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                final String msgReceived = String.valueOf(bytes) +
                        " bytes received:\n"
                        + strReceived;

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        Log.i(TAG,msgReceived);
                    }});

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                final String msgConnectionLost = "Connection lost:\n"
                        + e.getMessage();
                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        Log.i(TAG,msgConnectionLost);
                    }});
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            connectedOutputStream.write(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("Map","Failed");
        }
    }

    public void cancel() {
        try {
            connectedBluetoothSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}