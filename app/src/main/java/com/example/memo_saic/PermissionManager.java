package com.example.memo_saic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    private MainActivity activity;

    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int STORAGE_REQUEST_CODE = 102;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 103;

    private String[] cameraPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String[] locationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public PermissionManager(MainActivity activity) {
        this.activity = activity;
    }

    public boolean checkCameraPermission() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;

        boolean storagePermission = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        }

        return cameraPermission && storagePermission;
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, cameraPermissions, CAMERA_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void handlePermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                activity.getCameraManager().launchCamera();
            } else {
                showPermissionExplanationDialog("Camera", "Camera permission is required to take photos.");
            }
        } else if (requestCode == NotificationHelper.NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
            } else {
                Toast.makeText(activity, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activity.getLocationManager().getLastLocation();
            } else {
                showPermissionExplanationDialog("Location", "Location permission is required to show district and state.");
                activity.getLocationText().setText("Location permission denied");
            }
        }
    }

    public void showPermissionExplanationDialog(String permissionType, String message) {
        new AlertDialog.Builder(activity)
                .setTitle(permissionType + " Permissions Required")
                .setMessage(message + " Please grant the requested permissions to use this feature.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(activity, permissionType + " feature unavailable without permissions", Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }
}