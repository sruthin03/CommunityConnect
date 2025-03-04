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

public class LoginAsWorker extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private Button createButton;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as_worker); // Use the layout XML you provided

        firestore = FirebaseFirestore.getInstance();

        // Initialize the views
        usernameEditText = findViewById(R.id.Username_entry);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password);
        createButton = findViewById(R.id.worker_create);

        createButton.setOnClickListener(view->{
            Intent intent = new Intent(LoginAsWorker.this,WorkerLoginActivity.class);
            startActivity(intent);
        });// Set up the Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set up the Forgot Password click listener
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Reset Password page
                // This could open a new activity or show a dialog
                openForgotPasswordPage();
            }
        });
    }

    private void loginUser() {
        String mobile = usernameEditText.getText().toString().trim(); // User enters mobile here
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both mobile number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check in Firestore if the mobile and password matcch
        firestore.collection("workerId").document(mobile)
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
                                    Intent intent = new Intent(LoginAsWorker.this, WorkerHomePage.class);
                                    intent.putExtra("USER_ID", userId);
                                    startActivity(intent);
                                    finish(); // Close login activity to prevent back navigation to login
                                } else {
                                    Toast.makeText(LoginAsWorker.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginAsWorker.this, "No user found with this mobile number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginAsWorker.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidCredentials(String username, String password) {
        // For example, check if username is email and password length
        return username.equals("worker@example.com") && password.length() >= 6;
    }

    private void openForgotPasswordPage() {
        // This will open the reset password activity
        // startActivity(new Intent(this, ResetPasswordActivity.class));
    }
}
