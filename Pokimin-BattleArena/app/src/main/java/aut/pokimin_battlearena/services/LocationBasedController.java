package aut.pokimin_battlearena.services;

import android.content.Context;
import android.app.*;
import android.os.*;
import android.view.View;
import android.location.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class LocationBasedController implements LocationListener{

    private Context context;
    private Button toggleButton; // toggles whether GPS started/stopped
    private TextView locationTextView;
    public boolean wantLocationUpdates;
    public static final String UPDATES_BUNDLE_KEY
            = "WantsLocationUpdates";

    private float travelDistance;
    private Location pastLocation;

    public LocationBasedController(Context context){
        this.context = context;
        wantLocationUpdates = false;
        travelDistance = 0;
    }

    // getters & setters
    public boolean isWantLocationUpdates(){return wantLocationUpdates;}
    public float getTravelDistance(){return travelDistance;}
    public void setLocationTextView(TextView textview){locationTextView = textview;}
    public void setToggleButton(Button button){toggleButton = button;}

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
            locationManager.requestLocationUpdates(provider, 10000, 5, this);
            Location lastKnownLocation
                    = locationManager.getLastKnownLocation(provider);
            pastLocation = lastKnownLocation;
            if (lastKnownLocation != null)
                locationTextView.setText(lastKnownLocation.toString());
            toggleButton.setText("Stop training.");
        }catch(SecurityException e){}
    }

    // needs to be called when the activity onStop()
    public void stopGPS()
    {
        try {
            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(this);
            toggleButton.setText("Start training.");
        }catch(SecurityException e){}
    }


    // implementation of onLocationChanged method
    public void onLocationChanged(Location location)
    {
        travelDistance+= location.distanceTo(pastLocation); // adds the travel distance by subtracting current to previous distance
        locationTextView.setText("Total distance: " + travelDistance);
        // considering database update every time location changes
        pastLocation = location;
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
    {  locationTextView.setText("GPS status changed. Please try again.");
    }


}
