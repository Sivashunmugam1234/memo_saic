package com.example.memo_saic;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db;

    public interface FirestoreCallback {
        void onResult(boolean success);
    }

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void savePhotoData(String imageUrl, String district, String state,
                              String uploadDate, FirestoreCallback callback) {
        // Get current user ID from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "anonymous";

        // Create a data object
        Map<String, Object> photoData = new HashMap<>();
        photoData.put("userId", userId);
        photoData.put("imageUrl", imageUrl);
        photoData.put("district", district);
        photoData.put("state", state);
        photoData.put("uploadDate", uploadDate);
        photoData.put("timestamp", new Date()); // For sorting/filtering

        // Add to Firestore
        db.collection("photos")
                .add(photoData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                        callback.onResult(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding document", e);
                    callback.onResult(false);
                });
    }
}