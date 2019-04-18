package com.hxxr.rfterm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private List<SerialDevice> DeviceList = new ArrayList<>();
    private List<BluetoothDevice> PairedDeviceList = new ArrayList<>();
    private Set<BluetoothDevice> PairedDeviceSet;
    private SerialDeviceAdapter Adapter;

    // This code runs when the app starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Connect to Device");

        // Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateDeviceList();
                Snackbar.make(view, "Refreshed Device List", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Setup Bluetooth Adapter
        Global.getInstance().bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Devices View
        RecyclerView Recycler = (RecyclerView) findViewById(R.id.serial_recycler);
        Adapter = new SerialDeviceAdapter(DeviceList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        Recycler.setLayoutManager(mLayoutManager);
        Recycler.setItemAnimator(new DefaultItemAnimator());
        Recycler.setAdapter(Adapter);
        populateDeviceList();

        // Registers listeners for every item in the devices view
        // The listener is non-standard and defined in RecyclerListener.java
        Recycler.addOnItemTouchListener(new RecyclerListener(getApplicationContext(), Recycler,
                new RecyclerListener.ClickListener() {

            @Override
            // This method is called when a device is clicked
            // dev represents the device (class SerialDevice) that has been clicked
            public void onClick(View view, int position) {
                SerialDevice dev = DeviceList.get(position); // Represents the View of device that has been clicked
                Global.getInstance().device = PairedDeviceList.get(position);
                int[] coords = new int[2];
                view.getLocationInWindow(coords); // Stores the on-screen co-ordinates of the View
                Intent i = new Intent(MainActivity.this, Connecting.class);
                i.putExtra("deviceName", dev.getName()); // Send the device name to the "Connecting" Activity
                i.putExtra("deviceAddress", dev.getAddress()); // Send the device address
                i.putExtra("deviceType", dev.getType()); // Send the device type
                i.putExtra("deviceClassId", "(0x"+Integer.toString(dev.getClassId(), 16)+")"); // Send the class ID
                i.putExtra("deviceIcon", dev.getIcon()); // Send the device icon
                i.putExtra("deviceCoords", coords); // Send the co-ordinates
                MainActivity.this.startActivityForResult(i, 1); // Start the "Connecting" activity
                overridePendingTransition(R.anim.layout_fade_in, R.anim.layout_fade_out); // Change the transition to a "fade out" effect
            }

            // This method is called when you tap and hold on a device
            // It doesn't do anything currently
            @Override
            public void onLongClick(View view, int position) { }
        }));
    }

    // Scans for devices and then populates the list of devices
    private void populateDeviceList() {
        DeviceList.clear(); // Clear the old devices before we add the new ones

        // Attempt to turn on bluetooth if it is off
        if (!Global.getInstance().bluetoothAdapter.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, 2);
        }

        PairedDeviceSet = Global.getInstance().bluetoothAdapter.getBondedDevices(); // Get paired devices

        for (BluetoothDevice dev : PairedDeviceSet) { // For each paired device add it into the list of devices
            PairedDeviceList.add(dev);
            SerialDevice sDevice = new SerialDevice(
                    dev.getName(), dev.getAddress(), dev.getBluetoothClass().hashCode());
            DeviceList.add(sDevice);
        }

        Adapter.notifyDataSetChanged(); // Refresh the list of devices
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // This code runs if the user accepts request to turn on bluetooth
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            populateDeviceList(); // Refresh list of devices
        }

        if (requestCode == 1) {
            // This code runs if the app failed to connect to a serial port
            if (resultCode == Activity.RESULT_FIRST_USER) {
                Snackbar.make(findViewById(R.id.activity_main),
                        R.string.snackbar_failed,
                        Snackbar.LENGTH_INDEFINITE).setDuration(6000)
                        .setAction("Action", null).show();
            }

            // This code runs if we connected succesfully and then closed the connection
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(findViewById(R.id.activity_main),
                        "Closed connection.",
                        Snackbar.LENGTH_INDEFINITE).setDuration(6000)
                        .setAction("Action", null).show();
            }

            // This code runs if we connected succesfully and then closed the connection
            if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(findViewById(R.id.activity_main),
                        "Lost connection unexpectedly. Oops.",
                        Snackbar.LENGTH_INDEFINITE).setDuration(6000)
                        .setAction("Action", null).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
