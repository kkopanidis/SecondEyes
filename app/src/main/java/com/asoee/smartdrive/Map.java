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
import java.util.Locale;

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

        Geocoder geocoder = new Geocoder(MainWindow.activeContext, new Locale("en"));
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                MainWindow.activity
                        .approveAction("You're on "+convertToLatin(addresses.get(0).getSubThoroughfare()) ,false);
            }
        } catch (IOException ioe){
            //MainWindow.activity.approveAction("Sorry, couldn't find your location. Try again." ,false);
        }
    }

    private String convertToLatin(String str){
        if(str == null || str.trim() == "") return null;

        char[] strc = str.toLowerCase().toCharArray();
        String latinStr = "";

        for(char c : strc){
            if(Character.isDigit(c)){
                latinStr += c;
                continue;
            }
            switch(c){
                case ' ': latinStr += " "; break;
                case 'α': latinStr += "a"; break;
                case 'β': latinStr += "v"; break;
                case 'γ': latinStr += "g"; break;
                case 'δ': latinStr += "d"; break;
                case 'ε': latinStr += "e"; break;
                case 'ζ': latinStr += "z"; break;
                case 'η': latinStr += "ee"; break;
                case 'θ': latinStr += "th"; break;
                case 'ι': latinStr += "ee"; break;
                case 'κ': latinStr += "k"; break;
                case 'λ': latinStr += "l"; break;
                case 'μ': latinStr += "m"; break;
                case 'ν': latinStr += "n"; break;
                case 'ξ': latinStr += "x"; break;
                case 'ο': latinStr += "o"; break;
                case 'π': latinStr += "p"; break;
                case 'ρ': latinStr += "r"; break;
                case 'σ': latinStr += "s"; break;
                case 'τ': latinStr += "t"; break;
                case 'υ': latinStr += "u"; break;
                case 'φ': latinStr += "f"; break;
                case 'χ': latinStr += "ch"; break;
                case 'ψ': latinStr += "ps"; break;
                case 'ω': latinStr += "o"; break;
            }
        }
        return latinStr;
    }
}
