package aut.pokimin_battlearena.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import aut.pokimin_battlearena.R;
import aut.pokimin_battlearena.services.DatabaseController;
import aut.pokimin_battlearena.services.LocationBasedController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class MinionActivity extends Activity implements OnClickListener {

    LocationBasedController locationController;
    DatabaseController database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion);
        locationController = new LocationBasedController(this);
        // add button
        // add view
        // add saved instance
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
