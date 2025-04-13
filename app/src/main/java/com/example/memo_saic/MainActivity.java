package com.example.memo_saic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button logout;
    private TextView locationText;
    private static final String CHANNEL_ID = "simplified_coding";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 103;

    private Uri imageUri;
    private String currentPhotoPath;
    private String cloudinaryImageUrl; // To store the Cloudinary URL
    private FusedLocationProviderClient fusedLocationClient;

    private static boolean isMediaManagerInitialized = false;

    private String[] cameraPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String[] locationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.logoutButton);
        locationText = findViewById(R.id.locationText); // Make sure to add this TextView in your layout

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logout Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Login.class));
        });

        // Initialize Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "de66ollgh");
        config.put("api_key", "145498284129998");
        config.put("api_secret", "KxvkoULLjvIdvz_-131PrcRTjsY");

        if (!isMediaManagerInitialized) {
            try {
                MediaManager.init(this, config);
                isMediaManagerInitialized = true;
            } catch (IllegalStateException e) {
                // Already initialized
                isMediaManagerInitialized = true;
            }
        }

        createNotificationChannel();

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check and request location permission
        if (!checkLocationPermission()) {
            requestLocationPermission();
        } else {
            getLastLocation();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
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

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Try multiple options for district-like info
                String subAdminArea = address.getSubAdminArea();    // often district
                String locality = address.getLocality();            // sometimes city
                String subLocality = address.getSubLocality();      // neighborhood/smaller area
                String adminArea = address.getAdminArea();          // state
                String fullAddress = address.getAddressLine(0);     // full address for debugging

                // Fallback logic for district
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

    public void photogrid(View v) {
        Intent i = new Intent(this, photo_Grid.class);
        startActivity(i);
        displayNotification();
    }

    public void openCamera(View v) {
        if (!checkCameraPermission()) {
            requestCameraPermission();
        } else {
            launchCamera();
        }
    }

    private boolean checkCameraPermission() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;

        boolean storagePermission = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        }

        return cameraPermission && storagePermission;
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MemoSaic");

                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    currentPhotoPath = "Pictures/MemoSaic/" + imageFileName;
                }
                else {
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (!storageDir.exists()) {
                        storageDir.mkdirs();
                    }

                    photoFile = new File(storageDir, imageFileName);
                    currentPhotoPath = photoFile.getAbsolutePath();
                    imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                }

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No camera app found on device", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Simplified Coding Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for notifications");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void displayNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
                return;
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notification Title")
                .setContentText("This photo was taken 5 days ago")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, mBuilder.build());
    }

    private void galleryAddPic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, we already saved to the gallery during capture
            // Just get the file path for upload
            uploadToCloudinary(currentPhotoPath);
            return;
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        saveToGallery(f);
    }

    private void saveToGallery(File sourceFile) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";

            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File memoSaicDir = new File(picturesDir, "MemoSaic");

            if (!memoSaicDir.exists()) {
                if (!memoSaicDir.mkdirs()) {
                    return;
                }
            }

            File destFile = new File(memoSaicDir, fileName);

            try (InputStream in = new FileInputStream(sourceFile);
                 OutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(destFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);

            currentPhotoPath = destFile.getAbsolutePath();

            // Upload the saved image to Cloudinary
            uploadToCloudinary(currentPhotoPath);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToCloudinary(String filePath) {
        // Show upload progress to user
        Toast.makeText(this, "Uploading photo to cloud...", Toast.LENGTH_SHORT).show();

        // For Android 10+, we need to handle the file path differently
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !filePath.startsWith("/")) {
            // This is a relative path, we need to get the actual file
            try {
                // We'll use the imageUri that was set during camera launch
                uploadToCloudinaryWithUri(imageUri);
                return;
            } catch (Exception e) {
                Log.e("Cloudinary", "Error handling Android 10+ path: " + e.getMessage());
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // For older Android versions or when we have a direct file path
        File fileToUpload = new File(filePath);
        if (!fileToUpload.exists()) {
            Toast.makeText(this, "File not found for upload", Toast.LENGTH_SHORT).show();
            return;
        }

        MediaManager.get().upload(fileToUpload.getAbsolutePath())
                .unsigned("memosaic_preset") // Your unsigned upload preset
                .callback(new UploadCallback() {
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

                        // Show success message with the URL
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Upload successful! URL: " + cloudinaryImageUrl,
                                    Toast.LENGTH_LONG).show();

                            // Here you could store the URL in Firebase or your database
                            // saveImageUrlToDatabase(cloudinaryImageUrl);
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Upload failed: " + error.getDescription(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    private void uploadToCloudinaryWithUri(Uri fileUri) {
        MediaManager.get().upload(fileUri)
                .unsigned("memosaic_preset") // You should create this preset in your Cloudinary dashboard
                .callback(new UploadCallback() {
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

                        // Show success message with the URL
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Upload successful! URL: " + cloudinaryImageUrl,
                                    Toast.LENGTH_LONG).show();

                            // Here you could store the URL in Firebase or your database
                            // saveImageUrlToDatabase(cloudinaryImageUrl);
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Upload failed: " + error.getDescription(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_CODE && resultCode == RESULT_OK) {
            galleryAddPic();
            Toast.makeText(this, "Photo saved to gallery", Toast.LENGTH_SHORT).show();

            // Remove this code block to prevent double upload for Android 10+
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uploadToCloudinaryWithUri(imageUri);
            }
            */
        } else if (requestCode == CAMERA_CAPTURE_CODE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Camera capture cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
            } else {
                allPermissionsGranted = false;
            }

            if (allPermissionsGranted) {
                launchCamera();
            } else {
                showPermissionExplanationDialog("Camera", "Camera permission is required to take photos.");
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayNotification();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                showPermissionExplanationDialog("Location", "Location permission is required to show district and state.");
                locationText.setText("Location permission denied");
            }
        }
    }

    private void showPermissionExplanationDialog(String permissionType, String message) {
        new AlertDialog.Builder(this)
                .setTitle(permissionType + " Permissions Required")
                .setMessage(message + " Please grant the requested permissions to use this feature.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, permissionType + " feature unavailable without permissions", Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    // Method to get the Cloudinary URL
    public String getCloudinaryImageUrl() {
        return cloudinaryImageUrl;
    }
}