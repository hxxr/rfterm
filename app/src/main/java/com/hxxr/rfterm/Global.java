package com.hxxr.rfterm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

// This singleton contains global variables that can be accessed from all files
public final class Global {



    // This is the device the user has chosen to connect to
    public BluetoothDevice device;

    // This is the thread that controls the bluetooth socket
    public RFConnect connection;

    // This is the thread that controls I/O for the bluetooth socket
    public RFIO rfio;

    // This is the UUID to be used to connect to bluetooth serial ports (the "well-known SPP UUID")
    public final String BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    // Font size to use for the text in the terminal
    public float fontSize = 8f;

    // Background colour of the terminal as a 6-digit hex integer (defaults to #000000 for black)
    public int background = 0x000000;

    // Size of buffer (in bytes) in terminal (determines how many bytes can be read at once)
    public int bufferSize = 2048;

    public BluetoothAdapter bluetoothAdapter = null;

    public BluetoothSocket socket = null;

    // Maximum size of terminal scrollback buffer, in lines
    public int maxScrollback = 500;

    // The six shortcut characters in the terminal toolbar
    public char tshortcut[] = { '*', '#', '_', '-', '/', '&', '(', ')', '=', '%' };

    // Colours for the terminal toolbar (dark)
    public final int tcolorsDark[] = { 0xff03a9f4, 0xff6702f4, 0xff02f47b, 0xfff40212, 0xfff48b02, 0xfff4028b, 0xff0273f4, 0xffa4f402 };

    // Colours for the terminal toolbar (light)
    public final int tcolorsLight[] = { 0xffb7e1f4, 0xffd1b7f4, 0xffb7f4d6, 0xfff4b7bb, 0xfff4dab7, 0xfff4b7da, 0xffb7d4f4, 0xffe0f4b7 };





    // This is the code needed to make the class a singleton pattern class
    private Global() { }
    private static Global i = null;
    public static synchronized Global getInstance() {
        if (null == i) { i = new Global(); }
        return i;
    }
}