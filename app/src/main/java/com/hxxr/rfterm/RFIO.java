package com.hxxr.rfterm;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Once you start the connection with RFConnect, use RFIO to actually send and receive data!
 * Example:
 * <pre>
 *     RFIO rfio = new RFIO(Global.getInstance().connection, terminal, Terminal.this, onClose, onLoseConnection);
 *
 *     // Start transmitting/receiving data
 *     rfio.start();
 *
 *     // Stop transmitting/receiving data
 *     rfio.close();
 * </pre>
 */
public final class RFIO extends Thread {
    private final BluetoothSocket bluetoothSocket;
    private final InputStream IStream;
    private final OutputStream OStream;
    private xtermView _terminal;
    private Runnable runnable_close;
    private Runnable runnable_loseConnection;
    private Handler _handler;
    private BufferedReader reader;

    private boolean connected;

    /**
     * Once you start the connection with RFConnect, use RFIO to actually send and receive data!
     * Example:
     * <pre>
     *     RFIO rfio = new RFIO(Global.getInstance().connection, terminal, Terminal.this, onClose, onLoseConnection);
     *
     *     // Start transmitting/receiving data
     *     rfio.start();
     *
     *     // Stop transmitting/receiving data
     *     rfio.close();
     * </pre>
     * @param socket Bluetooth socket created by RFConnect (RFConnect.bluetoothSocket).
     * @param terminal TextView to write the received data to.
     * @param onClose Runnable to run if user closes connection willingly using RFIO.close().
     *                Setting this to null will run nothing.
     *                It will not run if we lose connection unexpectedly.
     * @param onLoseConnection Runnable to run if we lose connection unexpectedly.
     *                         Setting this to null will run nothing.
     *                         It will not run if user closes connection willingly.
     */
    public RFIO(BluetoothSocket socket,
                xtermView terminal,
                Runnable onClose,
                Runnable onLoseConnection) {
        bluetoothSocket = socket;
        runnable_close = onClose;
        runnable_loseConnection = onLoseConnection;
        InputStream in = null;
        OutputStream out = null;
        connected = true;
        _terminal = terminal;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IStream = in;
        OStream = out;
        reader = new BufferedReader(new InputStreamReader(IStream));
    }

    // This method is run when you use RFIO.start()
    @Override
    public void run() {
        _terminal.setCallback(callback);

        final char buffer[] = new char[Global.getInstance().bufferSize];
        final BufferedReader r = reader;
        int length;
        while (connected) {
            try {
                length = r.read(buffer, 0, Global.getInstance().bufferSize);

                if (length != -1) {
                    _terminal.write(buffer, length);
                }

            } catch (IOException e) { // This code runs if we can't read from the serial port (i.e. we lost connection)
                e.printStackTrace();
                closeAfterLoseConnection();
            }
        }
    }

    // This method is called when you try to send data to the bluetooth device (i.e. by typing in the terminal)
    public void send(byte b[]) {
        try {
            OStream.write(b);
        } catch (IOException e) { // This code runs if we can't write to the serial port (i.e. we lost connection)
            e.printStackTrace();
            closeAfterLoseConnection();
        }
    }

    // This method is called when the terminal wants to send data to the bluetooth device
    private xtermView.callback callback = new xtermView.callback() {
        public void output(char[] c) {
            try { send(new String(c).getBytes("UTF-8")); }
            catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        }
    };

    // This method is called when we lose connection
    private void closeAfterLoseConnection() {
        connected = false;
        if (runnable_loseConnection != null) {
            runnable_loseConnection.run();
        }
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method is called if the connection is closed by the user
    public void close() {
        connected = false;
        if (runnable_close != null) {
            runnable_close.run();
        }
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final int HANDLER_INPUT = 12345679;
}