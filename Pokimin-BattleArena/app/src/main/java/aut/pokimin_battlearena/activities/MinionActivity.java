package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.services.DatabaseController;
import aut.pokimin_battlearena.services.LocationBasedController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MinionActivity extends Activity{

    DatabaseController db;

    String minionName;
    Monster minion;

    TextView nameTextView;
    TextView elementTextView;
    TextView levelTextView;
    TextView experienceTextView;
    TextView healthTextView;
    TextView defenseTextView;
    TextView attackTextView;
    TextView speedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion);

        // Load monster object
        db = new DatabaseController(this);
        minionName = db.getActiveMonsterName(db.getPlayerName());
        minion = new Monster(this, minionName);

        // UI elements
        nameTextView        = (TextView) findViewById(R.id.minion_name);
        elementTextView     = (TextView) findViewById(R.id.minion_element);
        levelTextView       = (TextView) findViewById(R.id.minion_level);
        experienceTextView  = (TextView) findViewById(R.id.minion_experience);
        healthTextView      = (TextView) findViewById(R.id.minion_health);
        defenseTextView     = (TextView) findViewById(R.id.minion_defense);
        attackTextView      = (TextView) findViewById(R.id.minion_attack);
        speedTextView       = (TextView) findViewById(R.id.minion_speed);

        // fill in the view with monster object
        nameTextView.setText        (minionName);
        elementTextView.setText     ("Element: "+minion.getElement());
        levelTextView.setText       ("Level: "  +minion.getLevel());
        experienceTextView.setText  ("XP: "     +minion.getExp());
        healthTextView.setText      ("Health: " +minion.getHealth());
        defenseTextView.setText     ("Defense: "+minion.getDefence());
        attackTextView.setText      ("Attack: " +minion.getAttack());
        speedTextView.setText       ("Speed: "  +minion.getSpeed());
    }


}
