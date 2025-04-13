package com.example.memo_saic;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraManager {
    private MainActivity activity;
    private Uri imageUri;
    private String currentPhotoPath;

    public static final int CAMERA_CAPTURE_CODE = 101;

    public CameraManager(MainActivity activity) {
        this.activity = activity;
    }

    public void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MemoSaic");

                    imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    currentPhotoPath = "Pictures/MemoSaic/" + imageFileName;
                }
                else {
                    File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (!storageDir.exists()) {
                        storageDir.mkdirs();
                    }

                    photoFile = new File(storageDir, imageFileName);
                    currentPhotoPath = photoFile.getAbsolutePath();
                    imageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", photoFile);
                }

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activity.startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "Error creating image file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activity, "No camera app found on device", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            galleryAddPic();
            Toast.makeText(activity, "Photo saved to gallery", Toast.LENGTH_SHORT).show();
        } else if (requestCode == CAMERA_CAPTURE_CODE && resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, "Camera capture cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void galleryAddPic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.getCloudinaryManager().uploadToCloudinary(imageUri);
            return;
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);

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
            activity.sendBroadcast(mediaScanIntent);

            currentPhotoPath = destFile.getAbsolutePath();

            activity.getCloudinaryManager().uploadToCloudinary(currentPhotoPath);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
}