package com.example.memo_saic;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button logout;
    private TextView locationText;
    private GridLayout gridLayout;

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
        gridLayout = findViewById(R.id.districtGridLayout); // Make sure to add this ID to your GridLayout

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

        // Fetch user data and populate district cards
        fetchUserData();
    }

    private void initializeCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "de66ollgh");
        config.put("api_key", "145498284129998");
        config.put("api_secret", "KxvkoULLjvIdvz_-131PrcRTjsY");
        cloudinaryManager.initialize(config);
    }

    private void fetchUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Make sure user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        Set<String> uniqueDistricts = new HashSet<>();
        Map<String, String> districtStateMap = new HashMap<>();

        // Clear existing views in the gridLayout
        gridLayout.removeAllViews();

        db.collection("photos")  // Replace with your actual collection name if different
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String district = doc.getString("district");
                        String state = doc.getString("state");

                        if (district != null && !district.isEmpty()) {
                            uniqueDistricts.add(district);
                            districtStateMap.put(district, state);
                        }
                    }

                    // Create a card for each unique district
                    for (String district : uniqueDistricts) {
                        createDistrictCard(district, districtStateMap.get(district));
                    }

                    if (uniqueDistricts.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No locations found. Try uploading photos first.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error getting documents", e);
                    Toast.makeText(MainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void createDistrictCard(String district, String state) {
        // Create a new CardView programmatically
        CardView cardView = new CardView(this);

        // Card parameters
        int cardHeight = getResources().getDimensionPixelSize(R.dimen.card_height);
        int cardMargin = getResources().getDimensionPixelSize(R.dimen.card_margin);
        int cardRadius = getResources().getDimensionPixelSize(R.dimen.card_corner_radius);
        int cardElevation = getResources().getDimensionPixelSize(R.dimen.card_elevation);
        int cardPadding = getResources().getDimensionPixelSize(R.dimen.card_padding);

        // Set column weight for the card to take equal space in grid
        GridLayout.LayoutParams glParams = new GridLayout.LayoutParams();
        glParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        glParams.width = 0;
        glParams.height = cardHeight;
        glParams.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        cardView.setLayoutParams(glParams);

        cardView.setRadius(cardRadius);
        cardView.setCardElevation(cardElevation);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));

        // Create a LinearLayout to hold the content
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        linearLayout.setGravity(Gravity.CENTER);

        // Set click listener for the district card
        linearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, photo_Grid.class);
            intent.putExtra("DISTRICT", district);
            intent.putExtra("STATE", state);
            startActivity(intent);
        });

        // Add a TextView for the district name
        TextView districtTextView = new TextView(this);
        districtTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        districtTextView.setText(district);
        districtTextView.setTextColor(getResources().getColor(R.color.black));
        districtTextView.setTextSize(22);
        districtTextView.setTypeface(null, Typeface.BOLD);
        districtTextView.setGravity(Gravity.CENTER);

        // Add a TextView for the state name if available
        TextView stateTextView = new TextView(this);
        stateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        stateTextView.setText(state != null ? state : "");
        stateTextView.setTextColor(getResources().getColor(R.color.gray));  // Use gray or define dark_gray
        stateTextView.setTextSize(16);
        stateTextView.setGravity(Gravity.CENTER);

        // Add views to the LinearLayout
        linearLayout.addView(districtTextView);
        linearLayout.addView(stateTextView);

        // Add the LinearLayout to the CardView
        cardView.addView(linearLayout);

        // Add the CardView to the GridLayout
        gridLayout.addView(cardView);
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