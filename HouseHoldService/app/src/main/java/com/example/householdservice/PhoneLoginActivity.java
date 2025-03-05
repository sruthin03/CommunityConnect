package com.example.householdservice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText phoneInput;
    private Button sendOtpButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_login);

        phoneInput = findViewById(R.id.phoneNumber);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString().trim();
                if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
                    Toast.makeText(PhoneLoginActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Format phone number correctly with country code
                String fullPhoneNumber = "+91" + phoneNumber; // Change country code as needed

                loadingBar.setTitle("Phone Verification");
                loadingBar.setMessage("Please wait while we authenticate your phone...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                sendOtp(fullPhoneNumber);
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Verification Completed!", Toast.LENGTH_SHORT).show();
            }

            @Override

            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Log.e("OTP Error", "Verification Failed: " + e.getMessage(), e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneLoginActivity.this, "Invalid request. Check phone number format.", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneLoginActivity.this, "Too many requests. Try again later.", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    Toast.makeText(PhoneLoginActivity.this, "ReCAPTCHA required but missing.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PhoneLoginActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.dismiss();
                PhoneLoginActivity.this.verificationId = verificationId;
                Toast.makeText(PhoneLoginActivity.this, "OTP Sent!", Toast.LENGTH_SHORT).show();

                // Navigate to OTP Verification screen
                Intent intent = new Intent(PhoneLoginActivity.this, OtpVerification.class);
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("phoneNumber", phoneInput.getText().toString().trim());
                startActivity(intent);
            }
        };
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
