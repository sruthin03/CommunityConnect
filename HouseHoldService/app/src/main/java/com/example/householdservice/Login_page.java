package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Login_page extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, createAccountButton;
    private TextView phoneSignupText;
    private RadioGroup userTypeRadioGroup;
    private RadioButton radioUser, radioWorker;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userType = "users"; // Default user type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.Username_entry);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.worker_login);
        phoneSignupText = findViewById(R.id.forgot_password);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        radioUser = findViewById(R.id.radioUser);
        radioWorker = findViewById(R.id.radioWorker);

        // Set default user type
        userTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioUser) {
                userType = "users"; // Login as a user
            } else if (checkedId == R.id.radioWorker) {
                userType = "workers"; // Login as a worker
            }
        });

        // Login button click event
        loginButton.setOnClickListener(view -> loginUser());

        // Navigate to Phone Login Page
        phoneSignupText.setOnClickListener(view -> {
            Intent intent = new Intent(Login_page.this, PhoneLoginActivity.class);
            startActivity(intent);
        });

        // Navigate to Register Page
        createAccountButton.setOnClickListener(view -> {
            if (userType == "users"){
                Intent intent = new Intent(Login_page.this, CreateAccount.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(Login_page.this, WorkerLoginActivity.class);
                startActivity(intent);
            }

        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserType();
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException){
                            Toast.makeText(Login_page.this, "Email not found. Please sign up.", Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(Login_page.this, "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(Login_page.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserType() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        firestore.collection(userType).document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Toast.makeText(Login_page.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login_page.this, Home_page.class);
                            intent.putExtra("USER_ID", currentUser.getUid());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login_page.this, "User not found in " + userType, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
