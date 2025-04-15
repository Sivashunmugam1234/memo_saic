package com.example.memo_saic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class photo_Grid extends AppCompatActivity implements PhotoAdapter.OnPhotoClickListener {

    private String district;
    private String state;
    private RecyclerView photosRecyclerView;
    private TextView locationTitleText;
    private List<PhotoData> photoList;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_grid);

        // Initialize views
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        locationTitleText = findViewById(R.id.locationTitleText);

        // Set up RecyclerView
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, photoList, this);
        photosRecyclerView.setAdapter(photoAdapter);

        // Get the district and state from the intent
        Intent intent = getIntent();
        if (intent != null) {
            district = intent.getStringExtra("DISTRICT");
            state = intent.getStringExtra("STATE");

            // Set title with location info
            if (district != null) {
                String titleText = district;
                if (state != null && !state.isEmpty()) {
                    titleText += ", " + state;
                }
                locationTitleText.setText(titleText);
            }

            // Fetch images for this district
            fetchImagesForDistrict();
        } else {
            Toast.makeText(this, "No district information provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchImagesForDistrict() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Make sure user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        // Clear existing photos
        photoList.clear();

        // Query Firestore for images from this district
        db.collection("photos")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("district", district)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String imageUrl = doc.getString("imageUrl");
                        String caption = doc.getString("caption");
                        String timestamp = doc.getString("timestamp");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            photoList.add(new PhotoData(imageUrl, caption, timestamp));
                        }
                    }

                    if (photoList.isEmpty()) {
                        Toast.makeText(photo_Grid.this,
                                "No photos found for " + district,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Update the adapter
                        photoAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error getting images", e);
                    Toast.makeText(photo_Grid.this,
                            "Failed to fetch images",
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPhotoClick(PhotoData photo, int position) {
        // Handle photo click - you could open a detail view if needed
        Toast.makeText(this, "Photo " + (position + 1) + " clicked", Toast.LENGTH_SHORT).show();

        // Optionally open a detail activity
        // Intent intent = new Intent(this, PhotoDetailActivity.class);
        // intent.putExtra("IMAGE_URL", photo.getImageUrl());
        // intent.putExtra("CAPTION", photo.getCaption());
        // startActivity(intent);
    }

    public void calculator(View v){
        Intent i = new Intent(this, calculator.class);
        startActivity(i);
    }
}