package com.example.householdservice;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePassword extends AppCompatActivity {

    private EditText currPassword, newPassword, confirmPassword;
    private Button submitButton;
    private ImageButton backArrow;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password); // Ensure your layout file name matches

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Initialize UI elements
        currPassword = findViewById(R.id.curr);
        newPassword = findViewById(R.id.textView55);
        confirmPassword = findViewById(R.id.textView25);
        submitButton = findViewById(R.id.button6);
        backArrow = findViewById(R.id.backArrow);

        // Back button functionality
        backArrow.setOnClickListener(v -> finish());

        // Submit button click listener
        submitButton.setOnClickListener(v -> updatePassword());
    }

    private void updatePassword() {
        String oldPass = currPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null && user.getEmail() != null) {
            // Re-authenticate the user
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        // If re-authentication is successful, update password
                        user.updatePassword(newPass)
                                .addOnSuccessListener(unused1 -> {
                                    Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Close activity after success
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
}
