package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.fragments.MainMenuFragment;
import aut.pokimin_battlearena.dialogs.RegisterDialog;
import aut.pokimin_battlearena.services.DatabaseController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MainActivity extends Activity implements
        MainMenuFragment.OnFragmentInteractionListener {

    public DatabaseController database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new DatabaseController(this);

        // initiate fragment transactions
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        // determine which fragment to show
        if (database.isPlayerExist()) {
            MainMenuFragment menu = new MainMenuFragment();
            transaction.replace(R.id.main_fragment, menu);

            TextView playerName = (TextView) findViewById(R.id.main_player_name);
            playerName.setText(database.getPlayerName());

        } else {
            RegisterDialog register = new RegisterDialog();

            Bundle bundle = new Bundle();
            bundle.putSerializable("database", database);
            register.setArguments(bundle);

            register.show(manager, "Register");
        }

        transaction.addToBackStack(null);
        transaction.commit();


        //---------------Debugging/testing for DBController---------------------------
//        Log.v("test2", "success");
//        database = new DatabaseController(this);
//        Log.v("test4", "success");
//        Monster monster = new Monster(this, "Blop");
//        monster.printInfo();
//        Log.v("test3", "success");

//        database.createPlayer("Dominic"); //creates a player entry
//        Player player = new Player(this, "Dominic");
//        player.printPlayerInfoDebug();
        //------------------------------------------------------------------
    }

    public DatabaseController getDatabase() { return database; }

    @Override
    public void onMainMenuFragmentInteraction(int position) {

        Intent intent = new Intent(this, MainActivity.class);

        // Creating content for intent
        switch (position) {
            case 0: intent = new Intent(this, PlayerActivity.class); break;
            case 1: intent = new Intent(this, MinionActivity.class); break;
            case 2:
                intent.putExtra("fragmentID", 0);
                intent = new Intent(this, BattleActivity.class);
                break;
        }

        startActivity(intent);

    }
}
