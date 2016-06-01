package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.services.DatabaseController;
import aut.pokimin_battlearena.services.LocationBasedController;

public class TrainingActivity extends Activity
        implements View.OnClickListener{

    DatabaseController database;
    Monster minion;

    // Location variables
    private boolean wantLocationUpdates;
    private static final String UPDATES_BUNDLE_KEY
            = "WantsLocationUpdates";
    LocationBasedController locationController;

    // UI elements
    private Button toggleButton;
    private TextView locationText;
    private TextView activeMinionText;
    private TextView minionXPText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        // UI elements initializations
        toggleButton = (Button) findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(this);
        locationText = (TextView) findViewById(R.id.location_textview);
        activeMinionText = (TextView) findViewById(R.id.activeminion_textview);
        minionXPText = (TextView) findViewById(R.id.minionxp_textview);

        // Database & loading monster data
        database = new DatabaseController(this);
        String minionName = database.getActiveMonsterName(database.getPlayerName());
        minion = new Monster(this, minionName);
        activeMinionText.setText(minionName+" Level "+minion.getLevel());
        minionXPText.setText("XP: "+minion.getExp());

        // Location based code & assignment
        locationController = new LocationBasedController(this);
        locationController.setGUIElements(toggleButton,minionXPText,locationText);
        locationController.setMinion(minion);


        // Load saved instance
        if (savedInstanceState!=null
                && savedInstanceState.containsKey(UPDATES_BUNDLE_KEY))
            wantLocationUpdates
                    = savedInstanceState.getBoolean(UPDATES_BUNDLE_KEY);
        else // activity is not being reinitialized from prior start
            wantLocationUpdates = false;


    }

    /** Called when the activity is started. */
    @Override
    public void onStart()
    {  super.onStart();
        if(locationController.isWantLocationUpdates())
            locationController.startGPS();
    }

    /** Called when the activity is stopped. */
    @Override
    public void onStop()
    {  super.onStop();
        // stop location updates while the activity is stopped
        locationController.stopGPS();
    }

    @Override
    public void onClick(View v) {
        locationController.toggleLocationUpdate();
    }

}
