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
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.activities.BattleActivity;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BluetoothClient implements BluetoothNode {

    // TODO: register discovery receiver
    // TODO: include actions to the discovery receiver
    // TODO: wait for a device with a service
    // TODO: start communicating

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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // CONNECTION RELATED ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private void connectToServer() {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        socket = null;

        // in case device has not been registered
        if(device == null && !stopRequest) {
            stopRequest = true;
            return;
        }

        // creating a connection with the device
        try {
            socket = device.createRfcommSocketToServiceRecord(BluetoothNode.SERVICE_UUID);
            socket.connect();
            adapter.cancelDiscovery();
        } catch (IOException ex) {
            System.err.println("Unable to connect socket with device: " + ex);
            socket = null;
        }

        // stop thread when socket connection fails
        if (socket == null) {
            stopRequest = true;
            return;
        }
    }

    private void startCommunication() {

        // start message receiver in new thread
        MessageReceiver messageReceiver = new MessageReceiver(socket);
        Thread receiverThread = new Thread(messageReceiver);
        receiverThread.start();

    }

    // SENDING MESSAGES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void sendPlayerInfo() {
        // TODO: send player information to server
    }

    public void sendActiveSkill(Skill skill) {
        // TODO: send activated skill to server
    }

    public void sendMessage(String message) {
        // TODO: send a message to server for debugging purposes
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public class MessageReceiver implements Runnable {

        // FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        BluetoothSocket    socket;
//        ObjectOutputStream output;
        ObjectInputStream  input;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public MessageReceiver(BluetoothSocket socket) {
            try {
                this.socket = socket;
//                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input  = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Unable to extract output stream: " + e);
            }
        }

        // RUNNABLE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        @Override
        public void run() {

            try {
                while (!stopRequest) {
                    Object object = input.readObject();

                    if (object instanceof String) {
                        String response = (String) object;
                        messages.add(response);
                        activity.setResponseMessage(response);
                    } else if (object instanceof Player) {
                        // TODO: set your opponent's information here
                        Player player = (Player) object;
                        activity.setBattleOpponentName(player);
                    } else if (object instanceof Monster) {
                        // TODO: set your opponent's minion stats here
                        Monster monster = (Monster) object;
                        activity.setBattleOpponentHealth(monster);
                    }
                }
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                if (input  != null) input.close();
//                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException ex) {
                System.err.println("Unable to close connection: " + ex);
            }
        }

        // UTILITY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
    public class DeviceDiscoverReceiver extends BroadcastReceiver {

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

                connectToServer();
                startCommunication();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                // activate activity here

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                device.notify();

            }
        }
    }
}
