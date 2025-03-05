package com.example.householdservice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerification extends AppCompatActivity {

    private EditText otpInput;
    private Button verifyOtpButton;
    private String verificationId;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_verification);

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        verificationId = getIntent().getStringExtra("verificationId");

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpInput.getText().toString().trim();
                if (otp.isEmpty() || otp.length() < 6) {
                    Toast.makeText(OtpVerification.this, "Enter a valid OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadingBar.setTitle("Verifying OTP");
                loadingBar.setMessage("Please wait while we verify your OTP...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                verifyOtp(otp);
            }
        });
    }

    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    loadingBar.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(OtpVerification.this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OtpVerification.this, Home_page.class));
                        finish();
                    } else {
                        Toast.makeText(OtpVerification.this, "Invalid OTP, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
