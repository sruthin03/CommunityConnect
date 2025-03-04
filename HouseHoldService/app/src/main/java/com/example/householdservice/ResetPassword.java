package com.example.householdservice;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPassword extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button submitButton;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password); // Replace with your layout file

        // Initialize views
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        submitButton = findViewById(R.id.submitButton);
        errorTextView = findViewById(R.id.errorText);

        // Handle reset password click
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Check if fields are empty
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill both password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        // If passwords match, reset password logic
        // You can add your API call or backend code here to reset the password

        // For now, display success message
        errorTextView.setVisibility(View.GONE);
        Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();

        // You can redirect the user to login activity or finish the activity after successful reset
        finish();
    }
}
