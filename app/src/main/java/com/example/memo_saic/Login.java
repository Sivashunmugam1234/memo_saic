package com.example.memo_saic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }
    public void Signup(View v){
        Intent i=new Intent(this, Signup.class);
        startActivity(i);
    }
    public void forgot_password(View v){
        Intent i=new Intent(this, froget_password.class);
        startActivity(i);
    }
}