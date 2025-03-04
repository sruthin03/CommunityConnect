package com.example.householdservice;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePassword extends AppCompatActivity {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;
    private String userId;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        userId = getIntent().getStringExtra("USER_ID");// Change to your layout file name

        currentPasswordEditText = findViewById(R.id.textView51);
        newPasswordEditText = findViewById(R.id.textView55);
        confirmPasswordEditText = findViewById(R.id.textView25);
        submitButton = findViewById(R.id.button6);

        firebaseAuth = FirebaseAuth.getInstance();

        submitButton.setOnClickListener(v -> updatePassword());
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the currently signed-in user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Re-authenticate user before updating password
            firebaseAuth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update password
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(UpdatePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            // Optionally, you can finish the activity or navigate to another screen
                                            finish();
                                        } else {
                                            Toast.makeText(UpdatePassword.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(UpdatePassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
