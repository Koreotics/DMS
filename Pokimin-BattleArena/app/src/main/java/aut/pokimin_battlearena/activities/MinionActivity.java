package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Skill;
import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.services.LocationBasedController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MinionActivity extends Activity implements OnClickListener {

    LocationBasedController locationController;
    Button toggleButton;
    TextView distanceTextView;
    TextView nameTextView;
    TextView elementTextView;
    TextView levelTextView;
    TextView experienceTextView;


    //-- test minion data without monster object first
    String minionName = "Blop";
    String minionElement = "Fire";
    int minionAttack = 3;
    double minionDefence = 0.1;
    int minionSpeed = 1;
    Skill skill1 = new Skill(this,"Tackle");
    Skill skill2 = new Skill(this,"Ember");
    Skill skill3 = new Skill(this,"Harden");
    Skill skill4 = new Skill(this,"Protect");

    int minionLevel = 1;
    int minionExperience = 0;
    //--

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion);
        locationController = new LocationBasedController(this);
        // add button that implements onclicklistener
        // add view
        // add saved instance in case home button is pressed
        toggleButton = (Button) findViewById(R.id.minion_train_button);
        toggleButton.setOnClickListener(this);
        locationController.setToggleButton(toggleButton);

        distanceTextView    = (TextView) findViewById(R.id.train_distance_textview);
        locationController.setLocationTextView(distanceTextView);

        nameTextView        = (TextView) findViewById(R.id.minion_name);
        elementTextView     = (TextView) findViewById(R.id.minion_element);
        levelTextView       = (TextView) findViewById(R.id.minion_level);
        experienceTextView  = (TextView) findViewById(R.id.minion_experience);

        if (savedInstanceState!=null
                && savedInstanceState.containsKey(locationController.UPDATES_BUNDLE_KEY))
            locationController.wantLocationUpdates
                    = savedInstanceState.getBoolean(locationController.UPDATES_BUNDLE_KEY);
        else // activity is not being reinitialized from prior start
            locationController.wantLocationUpdates = false;

        // fill in the view
        nameTextView.setText("Name: "+ minionName);
        elementTextView.setText("Element: "+ minionElement);
        levelTextView.setText("Level "+minionLevel);
        experienceTextView.setText("Exp: "+minionExperience);
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

    /** Called when activity is about to be killed to save app state */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {  super.onSaveInstanceState(outState);
        outState.putBoolean(locationController.UPDATES_BUNDLE_KEY, locationController.isWantLocationUpdates());
    }

    // implementation of OnClickListener method
    public void onClick(View view){
        locationController.toggleLocationUpdate();
    }
}
