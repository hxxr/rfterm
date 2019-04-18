package com.hxxr.rfterm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import java.io.IOException;
import java.util.UUID;


/**
 * Connect to a bluetooth device...
 * Example:
 * <pre>
 *     RFConnect connection = new RFConnect(device, Global.getInstance().BT_UUID, onSuccess, onError);
 *
 *     // Start the connection:
 *     connection.start();
 *
 *     // Stop the connection:
 *     connection.close();
 *
 *     // Get the socket
 *     BluetoothSocket ... = connection.socket
 * </pre>
 */
public final class RFConnect extends Thread {
    private static UUID _UUID;
    private final BluetoothDevice bluetoothDevice;
    private Runnable runnable_success;
    private Runnable runnable_error;

    public BluetoothSocket bluetoothSocket = null;


    /**
     * Connect to a bluetooth device...
     * Example:
     * <pre>
     *     RFConnect connection = new RFConnect(device, Global.getInstance().BT_UUID, onSuccess, onError);
     *
     *     // Start the connection:
     *     connection.start();
     *
     *     // Stop the connection:
     *     connection.close();
     *
     *     // Get the socket
     *     BluetoothSocket ... = connection.bluetoothSocket;
     * </pre>
     * @param device Device you want to connect to.
     * @param uuid UUID to use to pair. Setting this to Global.getInstance().BT_UUID works most of the time.
     * @param onSuccess Runnable to run when the connection succeeds. Setting this to null will run nothing.
     * @param onError Runnable to run when the connection fails. Setting this to null will run nothing.
     */
    public RFConnect(BluetoothDevice device,
                     String uuid,
                     Runnable onSuccess,
                     Runnable onError) {

        bluetoothDevice = device;
        _UUID = UUID.fromString(uuid);
        runnable_success = onSuccess;
        runnable_error = onError;
    }

    // This method is called when you use RFConnect.start()
    @Override
    public void run() {
        Global.getInstance().bluetoothAdapter.cancelDiscovery();
        boolean connected = false;

        // Try to create handler for the connection
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(_UUID);
            Global.getInstance().socket = bluetoothSocket;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Attempt to connect to the device's serial port
        try {
            bluetoothSocket.connect();
            connected = true; // This line doesn't get run if the connecting failed
        } catch (final IOException e) {
            e.printStackTrace();
            try {
                bluetoothSocket.close(); // Attempt to close the port if the connection failed
            } catch (IOException f) {
                f.printStackTrace();
            }
        }

        if (connected) { // Run the "onSuccess" code if the connection succeeded
            if (runnable_success != null) {
                runnable_success.run();
            }

            // If it didn't, then run the "onError" code instead
        } else if (runnable_error != null) {
            runnable_error.run();
        }
    }

    // This method is called when you use RFConnect.close()
    public void close() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
