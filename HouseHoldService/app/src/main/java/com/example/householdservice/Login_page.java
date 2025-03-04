package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Login_page extends AppCompatActivity {

    private EditText mobileEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPassword;
    private Button loginAsWorker;
    private TextView createAccount;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        mobileEditText = findViewById(R.id.Username_entry); // Assuming this is where user enters mobile
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);
        loginAsWorker = findViewById(R.id.worker_login);
        createAccount = findViewById(R.id.create_acc);

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        forgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(Login_page.this, ForgotPassword.class);
            startActivity(intent);
        });
        loginAsWorker.setOnClickListener(view -> {
            Intent intent = new Intent(Login_page.this, LoginAsWorker.class);
            startActivity(intent);
        });
        createAccount.setOnClickListener(view -> {
            Intent intent = new Intent(Login_page.this, CreateAccount.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String mobile = mobileEditText.getText().toString().trim(); // User enters mobile here
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both mobile number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check in Firestore if the mobile and password match
        firestore.collection("users").document(mobile)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override

                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String storedPassword = document.getString("password");
                                if (password.equals(storedPassword)) {
                                    String name = document.getString("name");
                                    String district = document.getString("district");
                                    String userId = document.getString("mobile");

                                   // Toast.makeText(Login_page.this, "Welcome " + name + " from " + district, Toast.LENGTH_SHORT).show();

                                    // Navigate to the home page
                                    Intent intent = new Intent(Login_page.this, Home_page.class);
                                    intent.putExtra("USER_ID", userId);
                                    startActivity(intent);
                                    finish(); // Close login activity to prevent back navigation to login
                                } else {
                                    Toast.makeText(Login_page.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(Login_page.this, "No user found with this mobile number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login_page.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
