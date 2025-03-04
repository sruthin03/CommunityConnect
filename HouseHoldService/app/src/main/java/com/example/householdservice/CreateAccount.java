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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    private EditText nameEditText, ageEditText, addressEditText, mobileEditText, emailEditText, passwordEditText;
    private Spinner genderSpinner, districtSpinner;
    private Button createAccountButton;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Firestore instance
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

        // Create Account button click listener
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Collect data from input fields
                String name = nameEditText.getText().toString().trim();
                String age = ageEditText.getText().toString().trim();
                String gender = genderSpinner.getSelectedItem().toString();
                String address = addressEditText.getText().toString().trim();
                String district = districtSpinner.getSelectedItem().toString();
                String mobile = mobileEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check for empty fields
                if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || address.isEmpty() ||
                        district.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(CreateAccount.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Basic validation
                if (!isEmailValid(email)) {
                    Toast.makeText(CreateAccount.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isMobileValid(mobile)) {
                    Toast.makeText(CreateAccount.this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a map for user data
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("name", name);
                customerData.put("age", age);
                customerData.put("gender", gender);
                customerData.put("address", address);
                customerData.put("district", district);
                customerData.put("mobile", mobile);
                customerData.put("email", email);
                customerData.put("password", password);  // Note: Storing plain passwords is not recommended

                // Generate a unique customer ID, e.g., using mobile number
                String customerID = mobile; // Assuming mobile is unique per customer

                // Save data to Firestore
                firestore.collection("users").document(customerID) // Use customerID as the document ID
                        .set(customerData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateAccount.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                    // Navigate to login page or home screen
                                    Intent intent = new Intent(CreateAccount.this, Login_page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(CreateAccount.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Utility method to validate email format
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Utility method to validate mobile number format
    private boolean isMobileValid(String mobile) {
        return mobile.length() == 10 && mobile.matches("\\d{10}");
    }
}
