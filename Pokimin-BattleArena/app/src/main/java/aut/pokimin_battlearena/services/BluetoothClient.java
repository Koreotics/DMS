package aut.pokimin_battlearena.services;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
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
    private List<BluetoothDevice> devices;
    private BluetoothSocket socket;
    private BluetoothDevice serverDevice;
    private BluetoothAdapter adapter;

    // message related fields
    private List<String> messages;
    private BattleActivity activity;
    private BroadcastReceiver receiver;

    Handler handler;
    TextView searchMessage;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothClient() {
        devices      = new ArrayList<>();
        socket       = null;
        serverDevice = null;
        messages     = new ArrayList<>();
        activity     = null;
        receiver     = null;
        adapter      = null;

        stopRequest = false;

        handler = new Handler();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // RUNNABLE
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run() {

        // ensure ArrayLists do not contain any objects.
        devices.clear();
        messages.clear();

        // register receiver and filter to activity
        receiver = new DeviceDiscoverReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        activity.registerReceiver(receiver, intentFilter);

        // start device discovery
        adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.startDiscovery();
        searchMessage = activity.getSearchDialog().getMessageView();

        // wait until the discovery for devices has been completed
        synchronized (devices) {
            try { devices.wait(); }
            catch (InterruptedException ex) {}
        }

        if (!devices.isEmpty() && !stopRequest) {

            handler.post(new Runnable() {
                public void run() {
                    searchMessage.setText("Devices found! Processing service...");
                }
            });

            // attempt to connect to the first device with the same service
            for (BluetoothDevice device : devices) {
                try {
                    socket = device.createRfcommSocketToServiceRecord(BluetoothNode.SERVICE_UUID);
                    socket.connect();
                    adapter.cancelDiscovery();
                } catch (IOException e) { socket = null;}

                // break off for loop once device with service has been found
                if (socket != null) { break; }
            }

            if (socket != null) {

                // change to battle fragment
                FragmentManager manager = activity.getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.battle_fragment, activity.getBattleFragment());
                transaction.commit();

                handler.post(new Runnable() {
                    public void run() {
                        searchMessage.setText("Service found! Starting communication...");
                    }
                });

                // start receiving messages from server
                MessageReceiver messageReceiver = new MessageReceiver(socket);
                Thread receiverThread = new Thread(messageReceiver);
                receiverThread.start();

                // start sending messages to server
                while (!stopRequest) {
                    // TODO: start sender thread here
                }
            } else {
                handler.post(new Runnable() {
                    public void run() {
                        searchMessage.setText("Devices do not have service");
                    }
                });
            }

            // close all connections
            try {
                if (socket != null) socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            handler.post(new Runnable() {
                public void run() {
                    searchMessage.setText("Search completed! No devices found.");
                }
            });
        }

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

<<<<<<< HEAD
        if (receiver != null) {
            receiver = null; }
=======
        if (receiver != null) { activity.unregisterReceiver(receiver); }
>>>>>>> refs/remotes/origin/Tristan

        // notify array lists
        synchronized (devices)  { devices.notifyAll(); }
        synchronized (messages) { messages.notifyAll(); }

        try {
            if (socket != null) { socket.close(); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void registerActivity(Activity activity) {
        this.activity = (BattleActivity) activity;
        this.activity.registerClient(this);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
        ObjectInputStream  input;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public MessageReceiver(BluetoothSocket socket) {
            try {
                this.socket = socket;
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
                        activity.setBattleResponseMessage(response);
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
            }
            catch (OptionalDataException e)  { e.printStackTrace(); }
            catch (IOException e)            { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }

            try {
                if (input  != null) input.close();
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

            // device with bluetooth enabled found
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                activity.setSearchResponseMessage("Device(s) found!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                synchronized (devices) { devices.add(device); }

            // discovery started
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                activity.setSearchResponseMessage("Searching for devices...");
                devices.clear();

            // discovery completed
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                synchronized (devices) { devices.notifyAll(); }
                activity.setSearchResponseMessage("Search completed.");
                activity.showSearchButton();
                activity.showServerButton();

            }


        }
    }
}
