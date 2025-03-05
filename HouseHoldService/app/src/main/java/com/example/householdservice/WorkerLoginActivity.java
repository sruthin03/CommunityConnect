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

public class WorkerLoginActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText, mobileEditText, emailEditText, passwordEditText;
    private Spinner genderSpinner, districtSpinner, professionSpinner;
    private Button registerWorkerButton;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_login);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextText2); // Name
        ageEditText = findViewById(R.id.editTextNumber); // Age
        mobileEditText = findViewById(R.id.editTextPhone); // Mobile
        emailEditText = findViewById(R.id.editTextText4); // Email
        passwordEditText = findViewById(R.id.editTextText76); // Password
        genderSpinner = findViewById(R.id.spinner5); // Gender
        districtSpinner = findViewById(R.id.spinner); // District
        professionSpinner = findViewById(R.id.spinner3); // Profession
        registerWorkerButton = findViewById(R.id.button8); // Register Button

        // Register button click listener
        registerWorkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Collect data from input fields
                String name = nameEditText.getText().toString().trim();
                String age = ageEditText.getText().toString().trim();
                String profession = professionSpinner.getSelectedItem().toString();
                String gender = genderSpinner.getSelectedItem().toString();
                String district = districtSpinner.getSelectedItem().toString();
                String mobile = mobileEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check for empty fields
                if (name.isEmpty() || age.isEmpty() || profession.isEmpty() || gender.isEmpty() ||
                        district.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(WorkerLoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Basic validation
                if (!isEmailValid(email)) {
                    Toast.makeText(WorkerLoginActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isMobileValid(mobile)) {
                    Toast.makeText(WorkerLoginActivity.this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a map for worker data
                Map<String, Object> workerData = new HashMap<>();
                workerData.put("name", name);
                workerData.put("age", age);
                workerData.put("profession", profession);
                workerData.put("gender", gender);
                workerData.put("district", district);
                workerData.put("mobile", mobile);
                workerData.put("email", email);
                workerData.put("password", password);  // Note: Storing plain passwords is not recommended

                // Generate a unique worker ID, e.g., using mobile number
                String workerID = mobile; // Assuming mobile is unique per worker

                // Save data to Firestore in the 'workerId' collection
                firestore.collection("workerId").document(workerID) // Use workerID as the document ID
                        .set(workerData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(WorkerLoginActivity.this, "Worker registered successfully", Toast.LENGTH_SHORT).show();
                                    // Navigate to login page or main screen
                                    Intent intent = new Intent(WorkerLoginActivity.this, Login_page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(WorkerLoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
