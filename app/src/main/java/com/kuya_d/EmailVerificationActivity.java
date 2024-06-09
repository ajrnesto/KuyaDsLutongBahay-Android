package com.kuya_d;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailVerificationActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    MaterialButton btnResendVerification, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();
        waitForVerification();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            USER.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("verification", "User " + USER.getEmail() + " is verified: " + USER.isEmailVerified());

                    if (USER.isEmailVerified()) {
                        DB.collection("users").document(USER.getUid())
                                .update("isVerified", true)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
                    }
                }
            });

            handler.postDelayed(this, 5000);
        }
    };

    private void waitForVerification() {
         handler.postDelayed(runnable, 5000); // 1 second delay (takes millis)
    }

    private void initializeViews() {
        btnResendVerification = findViewById(R.id.btnResendVerification);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void handleUserInteraction() {
        btnResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnResendVerification.setEnabled(false);

                AUTH.getCurrentUser().sendEmailVerification();
            }
        });

        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AUTH.signOut();
                startActivity(new Intent(EmailVerificationActivity.this, AuthenticationActivity.class));
                finish();
            }
        });
    }
}