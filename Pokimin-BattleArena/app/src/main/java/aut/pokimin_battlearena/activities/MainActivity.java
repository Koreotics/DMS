package aut.pokimin_battlearena.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.services.DatabaseController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MainActivity extends AppCompatActivity {

    public DatabaseController db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------Debugging/testing for DBController---------------------------
        Log.v("test2", "success");
        db = new DatabaseController(this);
        Log.v("test4", "success");
        Monster monster = new Monster(this, "Blop");
        monster.printInfo();
        Log.v("test3", "success");

        db.createPlayer("Dominic"); //creates a player entry
        Player player = new Player(this, "Dominic");
        player.printPlayerInfoDebug();
        //---=--------------------------------------------------------------
    }
}
