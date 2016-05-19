package aut.pokimin_battlearena.services;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothServer() {
        connectedClient = null;
        messages         = new ArrayList<>();
        battleActivity         = null;

        stopRequest = false;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run() {

        connectedClient = null;
        messages.clear();

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

        while (!stopRequest) {
            try {
                BluetoothSocket socket = serverSocket.accept(1000);

                // create a thread for the client
                connectedClient = new ClientHandler(socket);
                Thread clientThread = new Thread(connectedClient);
                clientThread.start();
                // notify activity a client has connected


            } catch (IOException ex) {
                System.err.println("Cannot create a socket for client: " + ex);
            }

        }

        try { serverSocket.close(); }
        catch (IOException ex) { System.err.println("Cannot close server socket " + ex); }

    }

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
        synchronized (messages) {
            messages.notifyAll();
        }
        // close all client connections
        connectedClient.closeConnection();
    }

    @Override
    public void registerActivity(Activity activity) {
        this.battleActivity = (BattleActivity) activity;
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
                        battleActivity.setResponseMessage(response);
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
                battleActivity.setResponseMessage("SERVER: Opponent disconnecting");
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

}
