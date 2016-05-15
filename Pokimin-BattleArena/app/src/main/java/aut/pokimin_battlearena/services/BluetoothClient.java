package aut.pokimin_battlearena.services;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aut.pokimin_battlearena.activities.BattleActivity;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BluetoothClient implements BluetoothNode {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private boolean stopRequest;

    // bluetooth related fields
    private List<BluetoothDevice> devices;
    private BluetoothSocket socket;

    // message related fields
    private List<String> messages;
    private BattleActivity activity;
    private BroadcastReceiver receiver;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothClient() {
        devices  = new ArrayList<>();
        socket   = null;
        messages = new ArrayList<>();
        activity = null;
        receiver = null;

        stopRequest = false;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // RUNNABLE
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run() {

        devices.clear();
        messages.clear();

        // start device discovery
        receiver = new DeviceDiscoverReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        // register receiver and filter to activity

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        synchronized (devices) {
            try { devices.wait(); }
            catch (InterruptedException ex) { System.err.println("Connection has been interrupted "
                    + ex); }
        }

        // no connected devices
        if (devices.size() == 0 && !stopRequest) {
            // notify activity no devices has been found
        }

        // check if connected device has application
        for (BluetoothDevice device : devices) {
            try {
                // notify activity checking for service from one of the devices
                socket = device.createRfcommSocketToServiceRecord(BluetoothNode.SERVICE_UUID);
                socket.connect();
                adapter.cancelDiscovery();
                break;
            } catch (IOException ex) {
                System.err.println("Cannot find service " + ex);
            }
        }

        if (socket == null) {
            // notify activity no client has been found
        }

        // notify activity a connection has been found
        // send some form of message
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITIES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void forward(String message) {

        synchronized (messages) {
            messages.add(message);
            messages.notifyAll();
        }

    }

    @Override
    public void stop() {
        stopRequest = true;

        if (receiver != null) { receiver = null; }

        synchronized (devices) {
            devices.notifyAll();
        }
        synchronized (messages) {
            messages.notifyAll();
        }

        try {
            if (socket   != null) { socket.close(); }
        } catch (IOException e) { e.printStackTrace(); }

    }

    @Override
    public void registerActivity(Activity activity) {
        this.activity = (BattleActivity) activity;
    }

    public class DeviceDiscoverReceiver extends BroadcastReceiver {

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // UTILITY
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // attempt to find a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                synchronized (devices) {
                    devices.add(device);
                }

                // activate activity here

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                // activate activity here

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                synchronized (devices) {
                    devices.notifyAll();
                }

                // activate activity here
            }

        }
    }

}
