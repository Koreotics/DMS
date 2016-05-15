package aut.pokimin_battlearena.services;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private List<Object> connectedClients;
    private List<String> messages;
    private BattleActivity activity;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothServer() {
        connectedClients = new ArrayList<>();
        messages         = new ArrayList<>();
        activity         = null;

        stopRequest = false;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void run() {

        connectedClients.clear();
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
                // notify activity a client has connected


            } catch (IOException ex) {

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
    }

    @Override
    public void registerActivity(Activity activity) {
        this.activity = (BattleActivity) activity;
    }
}
