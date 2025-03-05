package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    private EditText nameEditText, ageEditText, addressEditText, mobileEditText, emailEditText, passwordEditText;
    private Spinner genderSpinner, districtSpinner;
    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextText);
        ageEditText = findViewById(R.id.editTextNumber3);
        addressEditText = findViewById(R.id.editTextPhone4);
        mobileEditText = findViewById(R.id.editTextNumber4);
        emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        genderSpinner = findViewById(R.id.spinnerGender);
        districtSpinner = findViewById(R.id.spinner2);
        createAccountButton = findViewById(R.id.button8);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString().trim();
        String district = districtSpinner.getSelectedItem().toString();
        String mobile = mobileEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validation
        if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || address.isEmpty() ||
                district.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(mobile)) {
            Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register User in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), name, age, gender, address, district, mobile, email);
                            }
                        } else {
                            Toast.makeText(CreateAccount.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Save user details in Firestore
    private void saveUserToFirestore(String userId, String name, String age, String gender, String address, String district, String mobile, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("address", address);
        userData.put("district", district);
        userData.put("mobile", mobile);
        userData.put("email", email);

        firestore.collection("users").document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateAccount.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateAccount.this, Login_page.class));
                        finish();
                    } else {
                        Toast.makeText(CreateAccount.this, "Firestore Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Email Validation
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Mobile Number Validation
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^[6-9]\\d{9}$");
    }
}
