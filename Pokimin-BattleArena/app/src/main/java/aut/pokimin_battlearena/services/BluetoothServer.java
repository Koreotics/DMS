package aut.pokimin_battlearena.services;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.GridView;
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
import aut.pokimin_battlearena.activities.MainActivity;
import aut.pokimin_battlearena.fragments.ResultFragment;
import aut.pokimin_battlearena.utils.MovesAdapter;

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
    private List<SkillMessage> messages;
    private BattleActivity battleActivity;

    Handler handler;
    TextView searchMessage;
    Context context;
    static boolean hasAttacked;
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BluetoothServer(Context context) {
        connectedClient = null;
        messages        = new ArrayList<>();
        battleActivity  = null;
        this.context    = context;

        stopRequest = false;
        handler = new Handler();
        hasAttacked = false;
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
                 socket = serverSocket.accept(50000);

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
                    connectedClient = new ClientHandler(socket, context);
                    Thread clientThread = new Thread(connectedClient);
                    clientThread.start();
                    // notify activity a client has connected
                    Log.w("ChatServer", "New client connection accepted");

                    //Starts sender thread
                    MessageSender sender = new MessageSender(this.context);
                    Thread senderThread = new Thread(sender);
                    senderThread.start();

                    sendPlayerInfo();
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

//        try {
            String message = "Battle Start!";
            InitMessage initMessage = new InitMessage(message, battleActivity.getPlayer().getName(),
                    battleActivity.getPlayer().getActiveMonster().getPassableMonsterInfo(), null, null  );

            connectedClient.send(initMessage);
//       }
//        catch (IOException e) {
//            System.err.println("Unable to send the player to the server: " + e);
//        }
    }

    public void sendActiveSkill(Skill skill, int position, ArrayList<Skill> skills, GridView view, MovesAdapter adapter) {

        if (hasAttacked == false) {
            // updating gridview
            skill.reducePP();
            skills.set(position, skill);
            adapter.notifyDataSetChanged();
            view.setAdapter(adapter);

            Player player = battleActivity.getPlayer();
            Monster monster = player.getActiveMonster();
            String message = player.getName() + "'s " +
                    monster.getName() + " has used the skill " + skill;

            SkillMessage skillMessage = new SkillMessage(message, monster.getPassableMonsterInfo(), skill.getPassableSkillInfo(), null, null);

            synchronized (messages) {
                messages.add(skillMessage);
                if (messages.size() == 2) messages.notifyAll();

            }
            hasAttacked = true;
        }
    }

    public void sendResultsMessage(String message, String winner, int expGain) {

        ResultMessage resultMessage = new ResultMessage(message, winner, expGain);

        connectedClient.send(resultMessage);
        stopRequest = true;

    }

    public void showResultsPage(ResultMessage object) {

        final ResultMessage message = (ResultMessage) object;


        // set text on result fragment
        final ResultFragment fragment= battleActivity.getResultFragment();

        // transact to result fragment
        FragmentManager manager = battleActivity.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.battle_fragment, fragment);
        transaction.commit();

        handler.post(new Runnable() {
            public void run() {
                fragment.setExp(message.getExpGain());
                fragment.setWinner(message.getWinner());
            }
        });

        // add exp gained to monster
        Monster minion = battleActivity.getPlayer().getActiveMonster();
        minion.setExp(minion.getExp() + message.getExpGain());

    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASSES
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //handles recieving messages
    private class ClientHandler implements Runnable {

        private BluetoothSocket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private Context context;

        // CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        public ClientHandler(BluetoothSocket socket, Context context) {
             this.socket = socket;
             this.context = context;
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
                        //messages.add(response);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                battleActivity.setBattleResponseMessage(response);
                            }
                        });
                    } else if (object instanceof InitMessage) {

                        final InitMessage message = (InitMessage) object;
                        final String clientPlayer = message.getClientPlayerName();
                        final Player serverPlayer = battleActivity.getPlayer();
                        final Monster clientMonster = new Monster(this.context, message.getClientMonInfo());
                        //messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                battleActivity.setBattleResponseMessage(message.getMessage());

                                // set name and health of both monsters
                                if (clientPlayer != null) {
                                    battleActivity.setBattleOpponentName(clientPlayer + ": " + clientMonster.getName());
                                    battleActivity.setMaxOpponentHealth(clientMonster);
                                    battleActivity.setBattleOpponentHealth(clientMonster);

                                }

                                if (serverPlayer != null) {
                                    battleActivity.setBattlePlayerName(serverPlayer.getName() + ": " + serverPlayer.getActiveMonster().getName());
                                    battleActivity.setBattlePlayerHealth(serverPlayer.getActiveMonster());
                                }
                            }
                        });

                    }
                    else if(object instanceof SkillMessage){
                        final SkillMessage message = (SkillMessage) object;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                battleActivity.setBattleResponseMessage(message.getMessage());
                            }
                        });

                        synchronized (messages) {
                            messages.add(message);
                            if(messages.size() ==2) messages.notifyAll();
                        }

//                        Monster clientMonster = new Monster(this.context, message.getClientMonster());
//                        final Skill clientSkill = new Skill(message.getClientSkill());


//                        synchronized (messages) {
//                            while (messages.size() == 0) {
//                                try {
//                                    wait(2000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }




                    }
                    else if (object instanceof BattleMessage) {

                        final BattleMessage message = (BattleMessage) object;
                        final Monster serverMonster = new Monster(this.context, message.getServerMonster());
                        final Monster clientMonster = new Monster(this.context, message.getClientMonster());
                       // messages.add(message.getMessage());

                        handler.post(new Runnable() {
                            public void run() {
                                battleActivity.setBattleResponseMessage(message.getMessage());

                                // change health of monsters
                                if (clientMonster != null) {
                                    battleActivity.setBattleOpponentHealth(clientMonster);
                                }
                                if (serverMonster != null) {
                                    battleActivity.setBattlePlayerHealth(serverMonster);
                                }
                            }
                        });


                    } else if (object instanceof ResultMessage) {

                        final ResultMessage message = (ResultMessage) object;
                       // messages.add(message.getMessage());

                        // set text on result fragment
                        final ResultFragment fragment = battleActivity.getResultFragment();

                        // transact to result fragment
                        FragmentManager manager = battleActivity.getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.battle_fragment, fragment);
                        transaction.commit();

                        handler.post(new Runnable() {
                            public void run() {
                                fragment.setExp(message.getExpGain());
                                fragment.setWinner(message.getWinner());
                            }
                        });

                        // add exp gained to monster
                        Monster minion = battleActivity.getPlayer().getActiveMonster();
                        minion.setExp(minion.getExp() + message.getExpGain());

                    }
                   // BluetoothServer.hasAttacked = false;
                }
            } catch (IOException e) {
                handler.post(new Runnable() {
                                 public void run() {
                                     battleActivity.setBattleResponseMessage("SERVER: Opponent disconnecting");
                                 }
                             });
                Log.w("ChatServer", "Client Disconnecting");
                Intent intent = new Intent(battleActivity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                battleActivity.startActivity(intent);
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
        private Context context;
        public MessageSender(Context context){
            this.context = context;
        }
        public void run()
        {
//            try
//            {
                //sendPlayerInfo();
                //battleActivity.setBattleResponseMessage("Sent Message: yoyo" );
             //   connectedClient.send("hihi");
                Log.d("Sending message: ", "from Server");
            String message = "client has connected";

//            InitMessage initMessage = new InitMessage(message, battleActivity.getPlayer().getName(),
//                    battleActivity.getPlayer().getActiveMonster().getPassableMonsterInfo(), null, null );
////           / /ArrayList<String> arrayList = new ArrayList<>();
////            arrayList.add(battleActivity.getPlayer().getName());
//
//            connectedClient.send(initMessage);
//            }
//            catch (IOException e)
//            {
//                Log.e("Server", "Message failed to send: " + e);
//            }
            while (!stopRequest)
        {  // get a message
            SkillMessage skillMessage1;
            SkillMessage skillMessage2;
            synchronized (messages)
            {
                while (messages.size() != 2)
                {  try{  messages.wait();}
                    catch (InterruptedException e)
                    { // ignore
                    }
                    if (stopRequest)
                        return;
                }
                skillMessage1 = messages.remove(0);
                skillMessage2 = messages.remove(0);
            }
            Monster clientMonster;
            Skill clientSkill;
            Monster serverMonster;
            Skill serverSkill;

            if(skillMessage1.getClientMonster() != null){
                clientMonster = new Monster(this.context, skillMessage1.getClientMonster());
                clientSkill = new Skill(skillMessage1.getClientSkill());
                serverMonster = new Monster(this.context, skillMessage2.getServerMonster());
                serverSkill = new Skill(skillMessage2.getServerSkill());

            }
            else{
                serverMonster = new Monster(this.context, skillMessage1.getServerMonster());
                serverSkill = new Skill(skillMessage1.getServerSkill());
                clientMonster = new Monster(this.context, skillMessage2.getClientMonster());
                clientSkill = new Skill(skillMessage2.getClientSkill());
            }

            Player player = battleActivity.getPlayer();
            //calculates damgage
            clientMonster = battleActivity.executeBattleRound(serverSkill, clientMonster, clientSkill);
            String battleActions = player.getName() + "'s " +
                    serverMonster.getName() + " has used the skill " + serverSkill.getName();
            connectedClient.send(new BattleMessage(battleActions, battleActivity.getPlayer().getActiveMonster().getPassableMonsterInfo(),
                    clientMonster.getPassableMonsterInfo()));

            // change health of monsters
            if (clientMonster != null) {
                battleActivity.setBattleOpponentHealth(clientMonster);
                //checks win conditions and sends results messages
                if(clientMonster.getHealth() <= 0){ // server wins
                    sendResultsMessage("You are the Loser!!", "have been defeated", player.getActiveMonster().getLevel()+10);
                    showResultsPage(new ResultMessage("You Win!", "are victorious", clientMonster.getLevel()*5+10));
                }
            }
            if (player.getActiveMonster() != null) {
                battleActivity.setBattlePlayerHealth(player.getActiveMonster());
                //checks win conditions and sends results messages
                if(serverMonster.getHealth() <= 0){ // client wins
                    sendResultsMessage("You are the winner!!", "are victorious", clientMonster.getLevel()*5+10);
                    showResultsPage(new ResultMessage("You Lose.", "have been defeated", player.getActiveMonster().getLevel()+10));
                }
            }


            BluetoothServer.hasAttacked = false;


            // put message on server display
//            if (battleActivity != null)
//                battleActivity.showReceivedMessage("RECEIVED: "+message);
            // pass message to all clients


        }
        }
        }


}
