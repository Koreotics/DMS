package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Player;
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

    Player myPlayer;
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

            myPlayer = new Player(this, database.getPlayerName());
            playerName.setText(myPlayer.getName());

        } else {
            RegisterDialog register = new RegisterDialog();

            Bundle bundle = new Bundle();
            bundle.putSerializable("database", database);
            register.setArguments(bundle);

            register.show(manager, "Register");
        }

        transaction.commit();
    }

    public DatabaseController getDatabase() { return database; }
    public Player             getMyPlayer() { return myPlayer; }

    @Override public void onMainMenuFragmentInteraction(int position) {}
}
