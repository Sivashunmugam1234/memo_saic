package com.example.memo_saic;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CloudinaryManager {
    private MainActivity activity;
    private String cloudinaryImageUrl;
    private static boolean isMediaManagerInitialized = false;

    private FirestoreHelper firestoreHelper;

    public CloudinaryManager(MainActivity activity) {
        this.activity = activity;
        this.firestoreHelper = new FirestoreHelper();
    }

    public void initialize(Map config) {
        if (!isMediaManagerInitialized) {
            try {
                MediaManager.init(activity, config);
                isMediaManagerInitialized = true;
            } catch (IllegalStateException e) {
                // Already initialized
                isMediaManagerInitialized = true;
            }
        }
    }

    public void uploadToCloudinary(String filePath) {
        Toast.makeText(activity, "Uploading photo to cloud...", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !filePath.startsWith("/")) {
            try {
                uploadToCloudinary(activity.getCameraManager().getImageUri());
            } catch (Exception e) {
                Log.e("Cloudinary", "Error handling Android 10+ path: " + e.getMessage());
                Toast.makeText(activity, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        File fileToUpload = new File(filePath);
        if (!fileToUpload.exists()) {
            Toast.makeText(activity, "File not found for upload", Toast.LENGTH_SHORT).show();
            return;
        }

        MediaManager.get().upload(fileToUpload.getAbsolutePath())
                .unsigned("memosaic_preset") // Your unsigned upload preset
                .callback(new CloudinaryCallback())
                .dispatch();
    }

    public void uploadToCloudinary(Uri fileUri) {
        MediaManager.get().upload(fileUri)
                .unsigned("memosaic_preset")
                .callback(new CloudinaryCallback())
                .dispatch();
    }

    // Inner callback class to handle upload results
    private class CloudinaryCallback implements UploadCallback {
        @Override
        public void onStart(String requestId) {
            Log.d("Cloudinary", "Upload started");
        }

        @Override
        public void onProgress(String requestId, long bytes, long totalBytes) {
            double progress = (double) bytes / totalBytes;
            Log.d("Cloudinary", "Upload progress: " + (int) (progress * 100) + "%");
        }

        @Override
        public void onSuccess(String requestId, Map resultData) {
            cloudinaryImageUrl = resultData.get("secure_url").toString();
            Log.d("Cloudinary", "Uploaded successfully: " + cloudinaryImageUrl);

            // Get location info from LocationManager
            LocationManager locationManager = activity.getLocationManager();
            String district = locationManager.getDistrictFromLocationText();
            String state = locationManager.getStateFromLocationText();

            // Get current date
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Save photo data to Firestore
            firestoreHelper.savePhotoData(cloudinaryImageUrl, district, state, currentDate,
                    success -> {
                        activity.runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(activity, "Upload successful!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(activity, "Failed to save photo details", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        }

        @Override
        public void onError(String requestId, ErrorInfo error) {
            Log.e("Cloudinary", "Upload error: " + error.getDescription());
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onReschedule(String requestId, ErrorInfo error) {
            Log.e("Cloudinary", "Upload rescheduled: " + error.getDescription());
        }
    }

    public String getCloudinaryImageUrl() {
        return cloudinaryImageUrl;
    }
}