package aut.pokimin_battlearena.services;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aut.pokimin_battlearena.Objects.Message.InitMessage;
import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.activities.BattleActivity;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BluetoothServer implements BluetoothNode {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final long SERIAL_VERSION_UID = 1;

    private boolean stopRequest;
    private ClientHandler connectedClient;
    private List<String> messages;
    private BattleActivity battleActivity;


    Handler handler;
    TextView searchMessage;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothServer() {
        connectedClient = null;
        messages        = new ArrayList<>();
        battleActivity  = null;


        stopRequest = false;
        handler = new Handler();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run() {

        connectedClient = null;
        messages.clear();
        searchMessage   = battleActivity.getSearchDialog().getMessageView();
        // setting server socket
        BluetoothServerSocket serverSocket = null;

        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(BluetoothNode.SERVICE_NAME,
                    BluetoothNode.SERVICE_UUID);
        } catch (IOException ex) {
            System.err.println("Cannot create server socket " + ex);
        }

        // prepare messaging thread
        BluetoothSocket socket = null;
        while (!stopRequest) { //search for client
            try {
                handler.post(new Runnable() {
                    public void run() {
                        searchMessage.setText("Looking for devices...");
                    }
                });
                 socket = serverSocket.accept(30000);

                handler.post(new Runnable() {
                    public void run() {
                        searchMessage.setText("Device Connected... ");
                    }
                });

                // create a thread for the client (message reciever)
                connectedClient = new ClientHandler(socket);
                Thread clientThread = new Thread(connectedClient);
                clientThread.start();
                // notify activity a client has connected
                Log.w("ChatServer", "New client connection accepted");

                //If connection was successful
                if (socket != null) {

                    handler.post(new Runnable() {
                        public void run() {
                            searchMessage.setText("Starting Game...");
                        }
                    });

                    //Starts sender thread
                    MessageSender sender = new MessageSender();
                    Thread senderThread = new Thread(sender);
                    senderThread.start();

                    // change to battle fragment
                    FragmentManager manager = battleActivity.getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.battle_fragment, battleActivity.getBattleFragment());
                    transaction.commit();
                }


            } catch (IOException ex) {
                System.err.println("Cannot create a socket for client: " + ex);
            }
            // break off for loop once device with service has been found
            if (socket != null) { break; }

        }

        try { serverSocket.close(); }
        catch (IOException ex) { System.err.println("Cannot close server socket " + ex); }

    }

    @Override
    public void forward(Object message) {
    public void forward(String message) {
//        synchronized (messages) {
//            messages.add(message);
//            messages.notifyAll();
//        }
        if(connectedClient != null)
            try {
                connectedClient.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void stop() {
        stopRequest = true;
        synchronized (messages) {
            messages.notifyAll();
        }
        // close all client connections
        connectedClient.closeConnection();
    }

    @Override
    public void registerActivity(Activity activity) {
        this.battleActivity = (BattleActivity) activity;
        battleActivity.registerBluetoothNode(this);
    }




    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSes
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //handles recieving messages
    private class ClientHandler implements Runnable {

        private BluetoothSocket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        public ClientHandler(BluetoothSocket socket) {
             this.socket = socket;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Stream Output error", e.toString());
            }
        }

        // repeatedly listens for incoming messages
        public void run() {
            try {
                input = new  ObjectInputStream(socket.getInputStream());
                // loop until the connection closes or stop requested
                while (!stopRequest) {
                    Object message = input.readObject(); // blocking


                    //TODO change, after implementing message classes and add appropriate actions
                    if (message instanceof String) {
                        String response = (String) message;
                        messages.add(response);
                        battleActivity.setBattleResponseMessage(response);
                    } else if (message instanceof Player) {
                        // TODO: set your opponent's information here
                        Player player = (Player) message;
                        battleActivity.setBattleOpponentName(player);
                    } else if (message instanceof Monster) {
                        // TODO: set your opponent's minion stats here
                        Monster monster = (Monster) message;
                        battleActivity.setBattleOpponentHealth(monster);
                    }
                }
            } catch (IOException e) {
                battleActivity.setBattleResponseMessage("SERVER: Opponent disconnecting");
                Log.w("ChatServer", "Client Disconnecting");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        finally {closeConnection();}
        }
        //Use to send messages to client.
        public void send(Object message) throws IOException {
            output.writeObject(message);
            output.flush();
        }

        public void closeConnection() {
            try {  socket.close();}
            catch (IOException e) { // ignore
            }
            connectedClient = null;
        }
    }


    // inner class handles sending messages to all client chat nodes
    private class MessageSender implements Runnable
    {
        public void run()
        {  //while (!stopRequest)
       // {  // get a message
//            String message;
//            synchronized (messages)
//            {
//                while (messages.size() == 0)
//                {  try{  messages.wait();}
//                    catch (InterruptedException e)
//                    { // ignore
//                    }
//                    if (stopRequest)
//                        return;
//                }
//                message = messages.remove(0);
//            }
            // put message on server display
//            if (battleActivity != null)
//                battleActivity.showReceivedMessage("RECEIVED: "+message);
            // pass message to all clients
            try
            {  connectedClient.send(new InitMessage("yoyo", new Player(this, )));
                //battleActivity.setBattleResponseMessage("Sent Message: yoyo" );
                Log.d("Sending message: ", "from Server");

            }
            catch (IOException e)
            {
                Log.e("Server", "Message failed to send: " + e);
            }
        //}
        }
        }


}
