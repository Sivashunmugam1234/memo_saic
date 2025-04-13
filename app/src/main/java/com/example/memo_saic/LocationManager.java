package com.example.memo_saic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationManager {
    private MainActivity activity;
    private TextView locationText;
    private FusedLocationProviderClient fusedLocationClient;

    public LocationManager(MainActivity activity, TextView locationText) {
        this.activity = activity;
        this.locationText = locationText;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Get district and state from location
                            getAddressFromLocation(location);
                        } else {
                            locationText.setText("Location not available");
                        }
                    }
                });
    }

    public void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Try multiple options for district-like info
                String subAdminArea = address.getSubAdminArea();    // often district
                String locality = address.getLocality();            // sometimes city
                String subLocality = address.getSubLocality();      // neighborhood/smaller area
                String adminArea = address.getAdminArea();          // state

                String district = (subAdminArea != null) ? subAdminArea :
                        (locality != null) ? locality :
                                (subLocality != null) ? subLocality :
                                        "Unknown District";

                locationText.setText("District: " + district + "\nState: " + adminArea);
            } else {
                locationText.setText("Location info not available");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationText.setText("Failed to get location info");
        }
    }

    public String getDistrictFromLocationText() {
        String locationInfo = locationText.getText().toString();
        if (locationInfo != null && !locationInfo.isEmpty()) {
            String[] lines = locationInfo.split("\n");
            if (lines.length >= 1 && lines[0].startsWith("District:")) {
                return lines[0].substring("District:".length()).trim();
            }
        }
        return "Unknown";
    }

    public String getStateFromLocationText() {
        String locationInfo = locationText.getText().toString();
        if (locationInfo != null && !locationInfo.isEmpty()) {
            String[] lines = locationInfo.split("\n");
            if (lines.length >= 2 && lines[1].startsWith("State:")) {
                return lines[1].substring("State:".length()).trim();
            }
        }
        return "Unknown";
    }
}