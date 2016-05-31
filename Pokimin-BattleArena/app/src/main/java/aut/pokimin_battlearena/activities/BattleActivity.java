package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;

import aut.pokimin_battlearena.Objects.Battle;
import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.fragments.BattleFragment;
import aut.pokimin_battlearena.fragments.ResultFragment;
import aut.pokimin_battlearena.dialogs.SearchDialog;
import aut.pokimin_battlearena.services.BluetoothClient;
import aut.pokimin_battlearena.services.BluetoothNode;
import aut.pokimin_battlearena.services.BluetoothServer;
import aut.pokimin_battlearena.services.DatabaseController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BattleActivity extends Activity implements Serializable,
        BattleFragment.OnFragmentInteractionListener,
        ResultFragment.OnFragmentInteractionListener {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static final int SEARCH_FRAGMENT = 0;
    public static final int BATTLE_FRAGMENT = 1;
    public static final int RESULT_FRAGMENT = 2;

    SearchDialog search;
    BattleFragment battle;
    ResultFragment result;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    BluetoothAdapter adapter;

    // Tristan added this
    BluetoothClient client;
    BluetoothServer server;

    Player player;
    BluetoothNode bluetoothNode;

    Button bluetoothButton;
    DatabaseController dbc;
    Battle battleLogic;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ACTIVITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set view for activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

//        Intent intent = this.getIntent();
//        Serializable s = intent.getSerializableExtra("player");
//        player = (Player) s;
        dbc = new DatabaseController(this);
        battleLogic = new Battle();
        player = new Player(this, dbc.getPlayerName());
        if (savedInstanceState == null) {

            // create instance of the fragments
            search = new SearchDialog();
            battle = new BattleFragment();
            result = new ResultFragment();

            // specifying intent filter
            intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

            // initialise fragment transactions
            FragmentManager     manager     = this.getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();


            // present the fragments into the screen
            switch (getIntent().getIntExtra("fragmentID", 0)) {
                case (SEARCH_FRAGMENT): search.show(manager, "Search"); break;
                case (BATTLE_FRAGMENT): transaction.replace(R.id.battle_fragment, battle); break;
                case (RESULT_FRAGMENT): transaction.replace(R.id.battle_fragment, result); break;
            }

            // finalise fragment transaction
            transaction.commit();

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        } catch (IllegalArgumentException ex) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (receiver != null) {
                registerReceiver(receiver, intentFilter);
            }
        } catch (IllegalArgumentException ex) {}
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (client != null) { client.stop(); }
            if (server != null) { server.stop(); }
            if (bluetoothNode != null) { bluetoothNode.stop(); }
            if (receiver != null) { unregisterReceiver(receiver); }
        } catch (IllegalArgumentException ex) {}

    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ACCESSORS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BattleFragment getBattleFragment() { return battle; }
    public ResultFragment getResultFragment() { return result; }
    public SearchDialog   getSearchDialog()   { return search; }

    // Tristan added this
    public Player         getPlayer()         { return player; }

    public BluetoothNode getBluetoothNode() {return bluetoothNode;}

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // MUTATORS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void setSearchResponseMessage(String message) { search.setMessage(message); }
    public void setBattleResponseMessage(String message) { battle.setMessage(message);}
    public void setBattlePlayerName(String player)       { battle.setPlayerName(player); }
    public void setBattleOpponentName(String player)     { battle.setOpponentName(player); }
    public void setBattlePlayerHealth(Monster monster)   { battle.setPlayerHealth(monster); }
    public void setBattleOpponentHealth(Monster monster) { battle.setOpponentHealth(monster); }
    public void setMaxOpponentHealth(Monster monster){ battle.setMaxOpponentHealth(monster);}


    public void showServerButton() { search.showServerButton(); }
    public void showSearchButton() { search.showSearchButton();}

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Battle Logic
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //returns client monster after the round. ONly called from server
    public Monster executeBattleRound(Skill serverSkill, Monster clientMonster, Skill clientSkill){

        battleLogic.setClientMonster(clientMonster);
        battleLogic.setServerMonster(getPlayer().getActiveMonster());

        battleLogic.executeBattleRound(serverSkill, clientSkill);
        getPlayer().setActiveMonster(battleLogic.getServerMonster());

        return battleLogic.getClientMonster();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UTILITY
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



    // REGISTER BLUETOOTH ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Tristan added this
    public void registerClient(BluetoothClient client) { this.client = client; }
    public void registerServer(BluetoothServer server) { this.server = server; }

    // SENDING CLIENT MESSAGES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void sendActiveSkill(Skill skill) { this.bluetoothNode.sendActiveSkill(skill); }
    public void sendPlayerInfo()             { this.bluetoothNode.sendPlayerInfo(); }

    public void registerBluetoothNode(BluetoothNode node) { this.bluetoothNode = node; }

    // BLUETOOTH UTILITY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void configBluetooth() {

        adapter = BluetoothAdapter.getDefaultAdapter();

        // check if device supports bluetooth
        if (adapter == null) {
            search.setMessage("This device does not support bluetooth. Get an upgrade mate");
        } else {

            if (receiver == null) { receiver = new BluetoothReceiver(); }

            // registering receiver
            registerReceiver(receiver, intentFilter);

            // enable bluetooth
            if (!adapter.isEnabled()) {
                search.setMessage("Bluetooth is disabled. Please enable to proceed.");
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetooth);
            } else { search.setMessage("Bluetooth is enabled"); }
        }
    }

    public void enableDiscoverable() {

        // enables device to be discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);
        search.getSearchButton().setVisibility(View.GONE);
    }

    // MISC UTILITY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void transactFragment(int fragmentID) {
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("fragmentID", fragmentID);
        startActivity(intent);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CLASS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (newState) {
                    case BluetoothAdapter.STATE_OFF : search.setMessage("Bluetooth is disabled"); break;
                    case BluetoothAdapter.STATE_TURNING_ON : search.setMessage("Enabling bluetooth..."); break;
                    case BluetoothAdapter.STATE_ON : search.setMessage("Bluetooth has been enabled"); break;
                    case BluetoothAdapter.STATE_TURNING_OFF : search.setMessage("Disabling bluetooth..."); break;
                }
            }
            else if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int newScanMode = intent.getIntExtra (BluetoothAdapter.EXTRA_SCAN_MODE, -1);

                switch (newScanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        search.setMessage("Searching for challenger...");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE :
                        search.setMessage("Challenger not found.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE :
                        search.setMessage("Bluetooth is not connected or discoverable");
                        break;
                }
            }
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UNUSED IMPLEMENTATIONS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override public void onBattleFragmentInteraction() {}
    @Override public void onResultFragmentInteraction() {}
}
