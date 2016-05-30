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
public class BluetoothServer implements BluetoothNode  {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final long SERIAL_VERSION_UID = 1;
    private static final long serialVersionUID = 1;
    private boolean stopRequest;
    private ClientHandler connectedClient;
    private List<String> messages;
    private BattleActivity battleActivity;


    Handler handler;
    TextView searchMessage;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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



                //If connection was successful
                if (socket != null) {

                    // change to battle fragment
                    FragmentManager manager = battleActivity.getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.battle_fragment, battleActivity.getBattleFragment());
                    transaction.commit();
                    battleActivity.getSearchDialog().dismiss();



                    handler.post(new Runnable() {
                        public void run() {
                            searchMessage.setText("Starting Game...");
                        }
                    });

                    // create a thread for the client (message reciever)
                    connectedClient = new ClientHandler(socket);
                    Thread clientThread = new Thread(connectedClient);
                    clientThread.start();
                    // notify activity a client has connected
                    Log.w("ChatServer", "New client connection accepted");

                    //Starts sender thread
                    MessageSender sender = new MessageSender();
                    Thread senderThread = new Thread(sender);
                    senderThread.start();


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
    public void forward(String message) {
//        synchronized (messages) {
//            messages.add(message);
//            messages.notifyAll();
//        }
//        if(connectedClient != null)
//            try {
                connectedClient.send(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
    }

    @Override
    public void stop() {
        stopRequest = true;
        synchronized (messages) {
            messages.notifyAll();
        }
        // close all client connections
        //connectedClient.closeConnection();
    }


    @Override
    public void registerActivity(Activity activity) {
        this.battleActivity = (BattleActivity) activity;
        battleActivity.registerBluetoothNode(this);
    }


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // SENDING MESSAGES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void sendPlayerInfo() {

      //  try {
            String message = "client has connected";
            InitMessage initMessage = new InitMessage(message, battleActivity.getPlayer(), null );

            connectedClient.send(initMessage);
/////       }
// catch (IOException e) {
//            System.err.println("Unable to send the player to the server: " + e);
//        }
    }

    public void sendActiveSkill(Skill skill) {

       // try {
            Monster monster = battleActivity.getPlayer().getActiveMonster();
            String message = monster.getName() + " has used to skill " + skill;

            SkillMessage skillMessage = new SkillMessage(message,  monster, skill, null, null);

            connectedClient.send(skillMessage);

        //} catch (IOException e) {
      //      System.err.println("Unable to send selected skill to the server: " + e);
     //   }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //handles recieving messages
    private class ClientHandler implements Runnable {

        private BluetoothSocket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        public ClientHandler(BluetoothSocket socket) {
             this.socket = socket;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new  ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Stream Output error", e.toString());
            }
        }

        // RUNNABLE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // repeatedly listens for incoming messages
        public void run() {
            try {

                // loop until the connection closes or stop requested
                while (!stopRequest) {
                    Object object = input.readObject(); // blocking


                    if (object instanceof String) {
                        final String response = (String) object;
                        messages.add(response);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                battleActivity.setBattleResponseMessage(response);
                            }
                        });
                    } else if (object instanceof InitMessage) {

                        final InitMessage message = (InitMessage) object;
                        final Player clientPlayer = message.getClientPlayer();
                        final Player serverPlayer = battleActivity.getPlayer();
                        messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                battleActivity.setBattleResponseMessage(message.getMessage());

                                // set name and health of both monsters
                                if (serverPlayer != null) {
                                    battleActivity.setBattleOpponentName(serverPlayer);
                                    battleActivity.setBattleOpponentHealth(serverPlayer.getActiveMonster());
                                }

                                if (clientPlayer != null) {
                                    battleActivity.setBattlePlayerName(clientPlayer);
                                    battleActivity.setBattlePlayerHealth(clientPlayer.getActiveMonster());
                                }
                            }
                        });

                    } else if (object instanceof BattleMessage) {

                        final BattleMessage message = (BattleMessage) object;
                        messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                battleActivity.setBattleResponseMessage(message.getMessage());

                                // change health of monsters
                                if (message.getServerMonster() != null) {
                                    battleActivity.setBattleOpponentHealth(message.getServerMonster());
                                }
                                if (message.getClientMonster() != null) {
                                    battleActivity.setBattlePlayerHealth(message.getClientMonster());
                                }
                            }
                        });

                    } else if (object instanceof ResultMessage) {

                        final ResultMessage message = (ResultMessage) object;
                        messages.add(message.getMessage());

                        // set text on result fragment
                        final ResultFragment fragment = battleActivity.getResultFragment();
                        handler.post(new Runnable() {
                            public void run() {
                                fragment.setExp(message.getExpGain());
                                fragment.setWinner(message.getWinner());
                            }
                        });

                        // add exp gained to monster
                        Monster minion = battleActivity.getPlayer().getActiveMonster();
                        minion.setExp(minion.getExp() + message.getExpGain());

                        // transact to result fragment
                        FragmentManager manager = battleActivity.getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.battle_fragment, fragment);
                        transaction.commit();

                    }
                }
            } catch (IOException e) {
                handler.post(new Runnable() {
                                 public void run() {
                                     battleActivity.setBattleResponseMessage("SERVER: Opponent disconnecting");
                                 }
                             });
                Log.w("ChatServer", "Client Disconnecting");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }



        finally {closeConnection();}
        }
        //Use to send messages to client.
        public void send(Object message)  {

            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        {
//            try
//            {
                //sendPlayerInfo();
                //battleActivity.setBattleResponseMessage("Sent Message: yoyo" );
             //   connectedClient.send("hihi");
                Log.d("Sending message: ", "from Server");
            String message = "client has connected";

            InitMessage initMessage = new InitMessage(message, battleActivity.getPlayer().getName(),
                    battleActivity.getPlayer().getActiveMonster().getPassableMonsterInfo(), null, null );
//           / /ArrayList<String> arrayList = new ArrayList<>();
//            arrayList.add(battleActivity.getPlayer().getName());

            connectedClient.send(initMessage);
//            }
//            catch (IOException e)
//            {
//                Log.e("Server", "Message failed to send: " + e);
//            }
            while (!stopRequest)
        {  // get a message
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

        }
        }
        }


}
