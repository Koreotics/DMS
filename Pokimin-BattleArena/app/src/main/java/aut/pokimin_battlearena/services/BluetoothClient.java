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
import aut.pokimin_battlearena.Objects.Message.ResultMessage;
import aut.pokimin_battlearena.Objects.Message.SkillMessage;
import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;
import aut.pokimin_battlearena.fragments.ResultFragment;


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
    private static final long serialVersionUID = 1;
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
    Context context;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothClient(Context context) {
        devices      = new ArrayList<>();
        messages     = new ArrayList<>();

        socket       = null;
        serverDevice = null;
        activity     = null;
        receiver     = null;
        adapter      = null;
        connection   = null;
        this.context = context;

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
                    //cancels discover after successful connection
                    adapter.cancelDiscovery();
                } catch (IOException e) { socket = null;}

                // break off for loop once device with service has been found
                if (socket != null) { break; }
            }
            //If connection was successful
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
                connection = new ServerConnection(socket, this.context);
                Thread receiverThread = new Thread(connection);
                receiverThread.start();

                sendPlayerInfo();
                // start sending messages to server
                while (!stopRequest) {
                    // TODO: start sender thread here

                }
            } else { //if connection failed
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

        if (receiver != null) { activity.unregisterReceiver(receiver);
            receiver = null;}


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
        this.activity.registerBluetoothNode(this);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // SENDING MESSAGES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void sendPlayerInfo() {
        ObjectOutputStream output = connection.output;

        try {
            String message = "client has connected";
            InitMessage initMessage = new InitMessage(message, null, null, activity.getPlayer().getName(),
                    activity.getPlayer().getActiveMonster().getPassableMonsterInfo());
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

            SkillMessage skillMessage = new SkillMessage(message, null, null, monster.getPassableMonsterInfo(),
                    skill.getPassableSkillInfo());
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
        Context context;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public ServerConnection(BluetoothSocket socket, Context context) {
            try {
                this.socket = socket;
                this.context = context;
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

                    if(object instanceof ArrayList){
                        final ArrayList<String> message = (ArrayList) object;
                        final Player clientPlayer = activity.getPlayer();
                        final String serverPlayer = message.get(0);
                       // messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                activity.setBattleResponseMessage(serverPlayer);


                            }
                        });
                    }
                    if (object instanceof String) {
                        final String response = (String) object;
                        messages.add(response);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                activity.setBattleResponseMessage(response);
                            }
                        });

                    } else if (object instanceof InitMessage) {

                        final InitMessage message = (InitMessage) object;
                        final Player clientPlayer = activity.getPlayer();
                        final String serverPlayer = message.getServerPlayerName();
                        final Monster serverMonster = new Monster(this.context, message.getServerMonInfo());
                        messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                activity.setBattleResponseMessage(message.getMessage());

                                // set name and health of both monsters
                                if (serverPlayer != null) {
                                    activity.setBattleOpponentName(serverPlayer);
                                    activity.setMaxOpponentHealth(serverMonster);
                                    activity.setBattleOpponentHealth(serverMonster);
                                }

                                if (clientPlayer != null) {
                                    activity.setBattlePlayerName(clientPlayer.getName());
                                    activity.setBattlePlayerHealth(clientPlayer.getActiveMonster());
                                }
                            }
                        });

                    } else if (object instanceof BattleMessage) {

                        final BattleMessage message = (BattleMessage) object;
                        final Monster serverMonster = new Monster(this.context, message.getServerMonster());
                        final Monster clientMonster = new Monster(this.context, message.getClientMonster());
                        messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                activity.setBattleResponseMessage(message.getMessage());

                                // change health of monsters
                                if (serverMonster != null) {
                                    activity.setBattleOpponentHealth(serverMonster);
                                }
                                if (clientMonster != null) {
                                    activity.setBattlePlayerHealth(clientMonster);
                                }
                            }
                        });

                    } else if (object instanceof ResultMessage) {

                        final ResultMessage message = (ResultMessage) object;
                        messages.add(message.getMessage());

                        // set text on result fragment
                        final ResultFragment fragment= activity.getResultFragment();
                        handler.post(new Runnable() {
                            public void run() {
                                fragment.setExp(message.getExpGain());
                                fragment.setWinner(message.getWinner());
                            }
                        });

                        // add exp gained to monster
                        Monster minion = activity.getPlayer().getActiveMonster();
                        minion.setExp(minion.getExp() + message.getExpGain());

                        // transact to result fragment
                        FragmentManager manager = activity.getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.battle_fragment, fragment);
                        transaction.commit();

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
                //devices.clear();

            // discovery completed
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Wakes up devices from waiting
                synchronized (devices) { devices.notifyAll(); }
                activity.setSearchResponseMessage("Search completed.");
                activity.showSearchButton();
                activity.showServerButton();

            }


        }
    }
}
