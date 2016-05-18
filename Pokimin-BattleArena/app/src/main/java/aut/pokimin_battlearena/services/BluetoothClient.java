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
import java.io.ObjectInputStream;
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
    private BluetoothDevice device;
    private BluetoothSocket socket;

    // message related fields
    private List<String> messages;
    private BattleActivity activity;
    private BroadcastReceiver receiver;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothClient() {
        device   = null;
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

        messages.clear();

        // start device discovery
        receiver = new DeviceDiscoverReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        // register receiver and filter to activity
        activity.registerReceiver(receiver, intentFilter);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

//        try { device.wait(); }
//        catch (InterruptedException ex) { System.err.println("Connection has been interrupted "
//                + ex); }

        if(device == null && !stopRequest) {
            stopRequest = true;
            return;
        }

        socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(BluetoothNode.SERVICE_UUID);
            socket.connect();
            adapter.cancelDiscovery();
        } catch (IOException ex) {
            System.err.println("Unable to connect socket with device: " + ex);
            socket = null;
        }

        if (socket == null) {
            stopRequest = true;
            return;
        }

        // TODO: start a new thread to send messages to device

        // read receiving messages from the socket
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while (!stopRequest) {
                String response = (String) input.readObject();
                activity.setResponseMessage(response);
            }
        }

        catch (IOException e) {}
        catch (ClassNotFoundException e) {}

        try {  socket.close(); }
        catch (IOException e) {}

        activity.transactFragment(BattleActivity.RESULT_FRAGMENT);

        socket = null;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // BLUETOOTH NODE
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

        device.notify();

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

                BluetoothDevice bDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device = bDevice;

                Intent battle = new Intent(activity, BattleActivity.class);
                battle.putExtra("fragmentID", BattleActivity.BATTLE_FRAGMENT);
                activity.startActivity(battle);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                // activate activity here

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                device.notify();

            }

        }
    }

}
