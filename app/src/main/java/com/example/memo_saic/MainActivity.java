package com.example.memo_saic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button logout;
    private TextView locationText;

    // Managers for different functionality
    private LocationManager locationManager;
    private CameraManager cameraManager;
    private CloudinaryManager cloudinaryManager;
    private NotificationHelper notificationHelper;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.logoutButton);
        locationText = findViewById(R.id.locationText);

        // Initialize managers
        locationManager = new LocationManager(this, locationText);
        cameraManager = new CameraManager(this);
        cloudinaryManager = new CloudinaryManager(this);
        notificationHelper = new NotificationHelper(this);
        permissionManager = new PermissionManager(this);

        // Initialize Cloudinary
        initializeCloudinary();

        // Set up logout button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logout Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
        });

        // Request location permissions and get location
        if (!permissionManager.checkLocationPermission()) {
            permissionManager.requestLocationPermission();
        } else {
            locationManager.getLastLocation();
        }
    }

    private void initializeCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "de66ollgh");
        config.put("api_key", "145498284129998");
        config.put("api_secret", "KxvkoULLjvIdvz_-131PrcRTjsY");
        cloudinaryManager.initialize(config);
    }

    public void photogrid(View v) {
        Intent i = new Intent(this, photo_Grid.class);
        startActivity(i);
        notificationHelper.displayNotification();
    }

    public void openCamera(View v) {
        if (!permissionManager.checkCameraPermission()) {
            permissionManager.requestCameraPermission();
        } else {
            cameraManager.launchCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cameraManager.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.handlePermissionsResult(requestCode, permissions, grantResults);
    }

    // Getters for accessing managers from other classes
    public LocationManager getLocationManager() {
        return locationManager;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public CloudinaryManager getCloudinaryManager() {
        return cloudinaryManager;
    }

    public TextView getLocationText() {
        return locationText;
    }
}