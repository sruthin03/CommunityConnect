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

public class WorkerLoginActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText, mobileEditText, emailEditText, passwordEditText;
    private Spinner genderSpinner, districtSpinner, professionSpinner;
    private Button createWorkerAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextText2);
        ageEditText = findViewById(R.id.editTextNumber);
        mobileEditText = findViewById(R.id.editTextPhone);
        emailEditText = findViewById(R.id.editTextText4);
        passwordEditText = findViewById(R.id.editTextText76);
        genderSpinner = findViewById(R.id.spinner5);
        districtSpinner = findViewById(R.id.spinner);
        professionSpinner = findViewById(R.id.spinner3);
        createWorkerAccountButton = findViewById(R.id.button8);

        createWorkerAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWorker();
            }
        });
    }

    private void registerWorker() {
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String district = districtSpinner.getSelectedItem().toString();
        String mobile = mobileEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String profession = professionSpinner.getSelectedItem().toString();

        // Validation
        if (name.isEmpty() || age.isEmpty() || gender.isEmpty() ||
                district.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || profession.isEmpty()) {
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

        // Register Worker in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveWorkerToFirestore(user.getUid(), name, age, gender, district, mobile, email, profession);
                            }
                        } else {
                            Toast.makeText(WorkerLoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Save worker details in Firestore
    private void saveWorkerToFirestore(String workerId, String name, String age, String gender, String district, String mobile, String email, String profession) {
        Map<String, Object> workerData = new HashMap<>();
        workerData.put("name", name);
        workerData.put("age", age);
        workerData.put("gender", gender);
        workerData.put("district", district);
        workerData.put("profession", profession);
        workerData.put("mobile", mobile);
        workerData.put("email", email);

        firestore.collection("workerId").document(workerId)
                .set(workerData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(WorkerLoginActivity.this, "Worker account created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(WorkerLoginActivity.this, Login_page.class));
                        finish();
                    } else {
                        Toast.makeText(WorkerLoginActivity.this, "Firestore Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
