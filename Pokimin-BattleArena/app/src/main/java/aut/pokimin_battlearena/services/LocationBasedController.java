package aut.pokimin_battlearena.services;

import android.content.Context;
<<<<<<< HEAD
import android.os.*;
import android.location.*;
import android.widget.Button;
import android.widget.TextView;

import aut.pokimin_battlearena.Objects.Monster;
=======
import android.app.*;
import android.os.*;
import android.view.View;
import android.location.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
>>>>>>> origin/FromTristanBranchAgain

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
<<<<<<< HEAD
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
=======
public class LocationBasedController implements LocationListener{

    private Context context;
    private Button toggleButton; // toggles whether GPS started/stopped
    private TextView locationTextView;
    public boolean wantLocationUpdates;
    public static final String UPDATES_BUNDLE_KEY
            = "WantsLocationUpdates";

    private float travelDistance;
    private Location pastLocation;
>>>>>>> origin/FromTristanBranchAgain

    public LocationBasedController(Context context){
        this.context = context;
        wantLocationUpdates = false;
<<<<<<< HEAD
        db = new DatabaseController(context);
=======
        travelDistance = 0;
>>>>>>> origin/FromTristanBranchAgain
    }

    // getters & setters
    public boolean isWantLocationUpdates(){return wantLocationUpdates;}
<<<<<<< HEAD
    public void setMinion(Monster m){
        minion = m;
        XP = minion.getExp();}
    public void setGUIElements(Button button, TextView xp, TextView log){
        toggleButton = button;
        XPTextView = xp;
        locationTextView = log;
    }
=======
    public float getTravelDistance(){return travelDistance;}
    public void setLocationTextView(TextView textview){locationTextView = textview;}
    public void setToggleButton(Button button){toggleButton = button;}

>>>>>>> origin/FromTristanBranchAgain
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
<<<<<<< HEAD
            locationManager.requestLocationUpdates(provider, 20000, 10, this);
            toggleButton.setText("Stop training");
=======
            locationManager.requestLocationUpdates(provider, 10000, 5, this);
            Location lastKnownLocation
                    = locationManager.getLastKnownLocation(provider);
            pastLocation = lastKnownLocation;
            if (lastKnownLocation != null)
                locationTextView.setText(lastKnownLocation.toString());
            toggleButton.setText("Stop training.");
>>>>>>> origin/FromTristanBranchAgain
        }catch(SecurityException e){}
    }

    // needs to be called when the activity onStop()
    public void stopGPS()
    {
        try {
            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(this);
<<<<<<< HEAD
            toggleButton.setText("Start training");
            locationTextView.setText("");
=======
            toggleButton.setText("Start training.");
>>>>>>> origin/FromTristanBranchAgain
        }catch(SecurityException e){}
    }


    // implementation of onLocationChanged method
    public void onLocationChanged(Location location)
    {
<<<<<<< HEAD
        // Adds XP every time the location listener is called
        XP += XPIncrease;
        // Updates minion object with new xp
        minion.setExp(XP);
        XPTextView.setText("XP: " + minion.getExp());
        locationTextView.setText("");
        // Updates database entry
        db.updateMonsterInfo(minion);

=======
        travelDistance+= location.distanceTo(pastLocation); // adds the travel distance by subtracting current to previous distance
        locationTextView.setText("Total distance: " + travelDistance);
        // considering database update every time location changes
        pastLocation = location;
>>>>>>> origin/FromTristanBranchAgain
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
<<<<<<< HEAD
    {  locationTextView.setText("Gathering location data..");
=======
    {  locationTextView.setText("GPS status changed. Please try again.");
>>>>>>> origin/FromTristanBranchAgain
    }


}
