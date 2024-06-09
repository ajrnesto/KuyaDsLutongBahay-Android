package com.kuya_d;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    FirebaseFirestore RTDB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        RTDB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeFirebase();

        if (USER == null || USER.isEmailVerified()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
        else {
            startActivity(new Intent(SplashActivity.this, EmailVerificationActivity.class));
            finish();
        }
    }
}