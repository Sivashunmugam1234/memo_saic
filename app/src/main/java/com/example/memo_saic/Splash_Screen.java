package com.example.memo_saic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash_Screen extends Activity {
    private static final String TAG = "SplashScreen";
    private static final int SPLASH_DELAY = 1500; // Reduced to 1.5 seconds
    private FirebaseAuth auth;
    private boolean isRedirected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        try {
            auth = FirebaseAuth.getInstance();
            scheduleNavigation();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            // Fallback to login if Firebase fails
            navigateToLogin();
        }
    }

    private void scheduleNavigation() {
        new Handler().postDelayed(() -> {
            if (!isRedirected) {
                checkAuthAndNavigate();
            }
        }, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        } else {
            navigateToLogin();
        }
    }

    private void navigateToMain() {
        isRedirected = true;
        startActivity(new Intent(Splash_Screen.this, MainActivity.class));
        finish();
    }

    private void navigateToLogin() {
        isRedirected = true;
        startActivity(new Intent(Splash_Screen.this, Login.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRedirected = true; // Prevent multiple redirects
    }
}