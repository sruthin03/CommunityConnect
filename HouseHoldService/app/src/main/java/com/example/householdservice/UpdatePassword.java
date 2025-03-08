package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePassword extends AppCompatActivity {

    private EditText currPassword, newPassword, confirmPassword;
    private Button submitButton;
    private ImageButton backArrow;
    private FirebaseAuth mAuth;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private TextView forgotPassword;


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
        forgotPassword = findViewById(R.id.forgot_password);

        // Back button functionality
        backArrow.setOnClickListener(v -> finish());

        // Submit button click listener
        submitButton.setOnClickListener(v -> updatePassword());

        forgotPassword.setOnClickListener(view -> resetPassword());
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


    public void logout(){
            mAuth.signOut(); // Firebase Logout
            Intent intent = new Intent(UpdatePassword.this, Login_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
    }
    private void resetPassword() {
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UpdatePassword.this,
                                            "Password reset email sent to "+email, Toast.LENGTH_LONG).show();
                                    logout();
                                } else {
                                    Toast.makeText(UpdatePassword.this,
                                            "Error: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
}
