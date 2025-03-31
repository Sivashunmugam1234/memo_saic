package com.example.memo_saic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Signup extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private EditText usernameEditText;
    private Button registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize EditTexts and Button
        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        usernameEditText = findViewById(R.id.usernameInput);
        confirmPasswordEditText =findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.signUpButton);

        // Set up register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = emailEditText.getText().toString();
                String txt_password = passwordEditText.getText().toString();
                String txt_confirmpassword = confirmPasswordEditText.getText().toString();
                String txt_username = usernameEditText.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(Signup.this, "Empty credentials!", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(Signup.this, "Password too short!", Toast.LENGTH_SHORT).show();
                } else if (!txt_password.equals(txt_confirmpassword)) {
                    Toast.makeText(Signup.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(txt_email, txt_password, txt_username);
                }
            }
        });
    }

    //login activity
    public void login(View v) {
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    private void registerUser(String email, String password, String username) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get current user
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            if (firebaseUser != null) {
                                // Create a UserProfileChangeRequest to update username
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                // Update the user profile
                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> usernameTask) {
                                                if (usernameTask.isSuccessful()) {
                                                    Toast.makeText(Signup.this,
                                                            "User registered successfully with username",
                                                            Toast.LENGTH_SHORT).show();

                                                    // Optional: Navigate to next screen after successful registration
                                                    Intent intent = new Intent(Signup.this, Login.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(Signup.this,
                                                            "Username update failed",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Signup.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}