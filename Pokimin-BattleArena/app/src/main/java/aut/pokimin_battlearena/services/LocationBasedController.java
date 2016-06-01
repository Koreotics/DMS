package aut.pokimin_battlearena.services;

import android.content.Context;

import android.os.*;
import android.location.*;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Monster;


/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */

/*
    How to use:

    Create a new LocationBasedController
    Set the GUI elements (XPTextView, locationTextView, toggleButton)
    Set the Minion object

 */
public class LocationBasedController implements LocationListener{

    private DatabaseController db;
    private Monster minion;
    private Context context;

    private Button toggleButton; // toggles whether GPS started/stopped
    private TextView XPTextView;
    private TextView locationTextView;

    public boolean wantLocationUpdates;

    private int XP;
    private int XPIncrease = 1; //editable XP increase for every 10 meters. This temporary value will be inputted to the database


    public LocationBasedController(Context context){
        this.context = context;
        wantLocationUpdates = false;

        db = new DatabaseController(context);

    }

    // getters & setters
    public boolean isWantLocationUpdates(){return wantLocationUpdates;}

    public void setMinion(Monster m){
        minion = m;
        XP = minion.getExp();}
    public void setGUIElements(Button button, TextView xp, TextView log){
        toggleButton = button;
        XPTextView = xp;
        locationTextView = log;
    }

    public void toggleLocationUpdate(){
        if(wantLocationUpdates) {
            wantLocationUpdates = false;
            stopGPS();
        }
        else {
            wantLocationUpdates = true;
            startGPS();
        }
    }

    // needs to be called when the activity onStart()
    public void startGPS()
    {
        try {
            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            String provider = LocationManager.GPS_PROVIDER;

            locationManager.requestLocationUpdates(provider, 20000, 10, this);
            toggleButton.setText("Stop training");

        }catch(SecurityException e){}
    }

    // needs to be called when the activity onStop()
    public void stopGPS()
    {
        try {
            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(this);

            toggleButton.setText("Start training");
            locationTextView.setText("");

        }catch(SecurityException e){}
    }


    // implementation of onLocationChanged method
    public void onLocationChanged(Location location)
    {

        // Adds XP every time the location listener is called
        XP += XPIncrease;
        // Updates minion object with new xp
        minion.setExp(XP);
        XPTextView.setText("XP: " + minion.getExp());
        locationTextView.setText("");
        // Updates database entry
        db.updateMonsterInfo(minion);


    }

    // implementation of onProviderDisabled method
    public void onProviderDisabled(String provider)
    {  locationTextView.setText("Please turn on GPS.");
    }

    // implementation of onProviderEnabled method
    public void onProviderEnabled(String provider)
    {  locationTextView.setText("GPS turned on. Try to move again.");
    }

    // implementation of onStatusChanged method
    public void onStatusChanged(String provider, int status,
                                Bundle extras)

    {  locationTextView.setText("Gathering location data..");

    }


}
