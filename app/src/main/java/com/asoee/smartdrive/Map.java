package com.asoee.smartdrive;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;

public class Map extends Action {

    protected static Location currentLocation;
    LocationManager locationManager;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Map(String sentence) {
        super(sentence);
        lolcation();
    }

    @Override
    protected void analyzeSentence() {

    }

    protected void lolcation(){
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) MainWindow.activeContext.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Map.currentLocation = location;
                locationManager.removeUpdates(this);
                executeCommand();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void executeCommand() {
        if(currentLocation == null) return;

        Geocoder geocoder = new Geocoder(MainWindow.activeContext, MainWindow.activity.getResources().getConfiguration().locale);
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                MainWindow.activity
                        .approveAction("You're on "+addresses.get(0).getThoroughfare()+", "+addresses.get(0).getSubAdminArea() ,false);
            }
        } catch (IOException ioe){
            //MainWindow.activity.approveAction("Sorry, couldn't find your location. Try again." ,false);
        }
    }
}
