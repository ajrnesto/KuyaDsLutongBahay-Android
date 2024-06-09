package com.kuya_d;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuya_d.Utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthenticationActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    // global
    ConstraintLayout clLogo;

    // authentication
    ConstraintLayout clLogin;
    TextInputLayout tilLoginMobile;
    TextInputEditText etLoginEmail, etLoginPassword;
    MaterialButton btnGotoSignup, btnLogin, btnSkip;

    // registration
    ConstraintLayout clSignup;
    TextInputEditText etSignupFirstName, etSignupLastName, etMobile, etSignupEmail, etSignupPassword;
    MaterialButton btnForgotPassword, btnGotoLogin, btnSignup;

    // verification
    ConstraintLayout clVerification;
    TextInputLayout tilVerificationCode;
    TextInputEditText etVerificationCode;
    MaterialButton btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        initializeFirebase();
        initializeViews();
        handleUserInteractions();
    }

    private void initializeViews() {
        // global
        clLogo = findViewById(R.id.clLogo);

        // authentication
        clLogin = findViewById(R.id.clLogin);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnGotoSignup = findViewById(R.id.btnGotoSignup);
        btnLogin = findViewById(R.id.btnLogin);
        btnSkip = findViewById(R.id.btnSkip);

        // registration
        clSignup = findViewById(R.id.clSignup);
        etSignupFirstName = findViewById(R.id.etSignupFirstName);
        etSignupLastName = findViewById(R.id.etSignupLastName);
        etMobile = findViewById(R.id.etMobile);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnGotoLogin = findViewById(R.id.btnGotoLogin);
        btnSignup = findViewById(R.id.btnSignup);
    }

    private void handleUserInteractions() {
        btnForgotPassword.setOnClickListener(view -> startActivity(new Intent(AuthenticationActivity.this, ForgotPasswordActivity.class)));

        btnSignup.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateRegistrationForm();
        });

        btnLogin.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateAuthenticationForm();
        });

        btnSkip.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            finish();
        });

        btnGotoSignup.setOnClickListener(view -> {
            clLogin.setVisibility(View.GONE);
            clSignup.setVisibility(View.VISIBLE);
        });

        btnGotoLogin.setOnClickListener(view -> {
            clLogin.setVisibility(View.VISIBLE);
            clSignup.setVisibility(View.GONE);
        });
    }

    private void validateAuthenticationForm() {
        if (etLoginEmail.getText().toString().isEmpty() ||
                etLoginPassword.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getUser().isEmailVerified()  ) {
                            Toast.makeText(AuthenticationActivity.this, "Signed in as "+email, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                            finish();
                        }
                        else {
                            startActivity(new Intent(AuthenticationActivity.this, EmailVerificationActivity.class));
                            finish();
                        }
                    }
                    else {
                        Utils.basicDialog(this, "Incorrect email or password.", "Try again");
                        btnLogin.setEnabled(true);
                    }
                });
    }

    private void validateRegistrationForm() {
        if (etSignupFirstName.getText().toString().isEmpty() ||
                etSignupLastName.getText().toString().isEmpty() ||
                etMobile.getText().toString().isEmpty() ||
                etSignupEmail.getText().toString().isEmpty() ||
                etSignupPassword.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etSignupFirstName.getText().toString().toUpperCase();
        String lastName = etSignupLastName.getText().toString().toUpperCase();
        String mobile = etMobile.getText().toString().toUpperCase();
        String email = etSignupEmail.getText().toString();
        String password = etSignupPassword.getText().toString();

        if (password.length() < 6) {
            Utils.basicDialog(this, "Please use a password with at least 6 characters.", "Okay");
            return;
        }

        btnSignup.setEnabled(false);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("mobile", mobile);
        userInfo.put("email", email);

        AUTH.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userInfo.put("uid", AUTH.getUid());
                        DB.collection("users").document(AUTH.getUid())
                                .set(userInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AUTH.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    startActivity(new Intent(AuthenticationActivity.this, EmailVerificationActivity.class));
                                                    finish();
                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(AuthenticationActivity.this, "Registration error: "+task.getException(), Toast.LENGTH_SHORT).show();
                                            btnSignup.setEnabled(true);
                                        }
                                    }
                                });
                    }
                    else {
                        Utils.basicDialog(this, "Something went wrong when trying to create your account.", "Try again");
                        btnSignup.setEnabled(true);
                    }
                });
    }
}