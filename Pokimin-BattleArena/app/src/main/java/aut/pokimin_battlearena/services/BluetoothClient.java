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

import aut.pokimin_battlearena.Objects.Message.BattleMessage;
import aut.pokimin_battlearena.Objects.Message.InitMessage;
import aut.pokimin_battlearena.Objects.Message.SkillMessage;
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

    // connection related fields
    private List<String> messages;
    private BattleActivity activity;
    private BroadcastReceiver receiver;
    private ServerConnection connection;

    Handler handler;
    TextView searchMessage;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothClient() {
        devices      = new ArrayList<>();
        messages     = new ArrayList<>();

        socket       = null;
        serverDevice = null;
        activity     = null;
        receiver     = null;
        adapter      = null;
        connection   = null;

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

                activity.getSearchDialog().dismiss();

                handler.post(new Runnable() {
                    public void run() {
                        searchMessage.setText("Service found! Starting communication...");
                    }
                });

                // start receiving messages from server
                connection = new ServerConnection(socket);
                Thread receiverThread = new Thread(connection);
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

        if (receiver != null) { activity.unregisterReceiver(receiver); }

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
        ObjectOutputStream output = connection.output;

        try {
            String message = "client has connected";
            InitMessage initMessage = new InitMessage(message, null, activity.getPlayer());
            output.writeObject(initMessage);
        } catch (IOException e) {
            System.err.println("Unable to send the player to the server: " + e);
        }
    }

    public void sendActiveSkill(Skill skill) {
        ObjectOutputStream output = connection.output;

        try {
            Monster monster = activity.getPlayer().getActiveMonster();
            String message = monster.getName() + " has used to skill " + skill;

            SkillMessage skillMessage = new SkillMessage(message, null, null, monster, skill);
            output.writeObject(skillMessage);

        } catch (IOException e) {
            System.err.println("Unable to send selected skill to the server: " + e);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public class ServerConnection implements Runnable {

        // FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        BluetoothSocket    socket;
        ObjectInputStream  input;
        ObjectOutputStream output;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public ServerConnection(BluetoothSocket socket) {
            try {
                this.socket = socket;
                this.input  = new ObjectInputStream(socket.getInputStream());
                this.output = new ObjectOutputStream(socket.getOutputStream());
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
                    } else if (object instanceof InitMessage) {

                        InitMessage message = (InitMessage) object;
                        Player clientPlayer = message.getClientPlayer();
                        Player serverPlayer = message.getServerPlayer();

                        messages.add(message.getMessage());
                        activity.setBattleResponseMessage(message.getMessage());

                        if (serverPlayer != null) {
                            activity.setBattleOpponentName(serverPlayer);
                            activity.setBattleOpponentHealth(serverPlayer.getActiveMonster());
                        }

                        if (clientPlayer != null) {
                            activity.setBattlePlayerName(clientPlayer);
                            activity.setBattlePlayerHealth(clientPlayer.getActiveMonster());
                        }

                    } else if (object instanceof BattleMessage) {
                        BattleMessage message = (BattleMessage) object;

                        messages.add(message.getMessage());
                        activity.setBattleResponseMessage(message.getMessage());

                        if (message.getServerMonster() != null) {
                            activity.setBattleOpponentHealth(message.getServerMonster());
                        }

                        if (message.getClientMonster() != null) {
                            activity.setBattlePlayerHealth(message.getClientMonster());
                        }

                    }
                }
            }
            catch (OptionalDataException e)  { e.printStackTrace(); }
            catch (IOException e)            { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }

            try {
                if (input  != null) input.close();
                if (output != null) output.close();
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
