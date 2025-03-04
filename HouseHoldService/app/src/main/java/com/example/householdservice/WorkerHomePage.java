package com.example.householdservice;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class WorkerHomePage extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView jobTitle, jobDescription, jobAddress, jobIssues, jobUser;
    private Spinner statusSpinner;
    private Button updateStatusButton;

    private String jobId;  // The job ID, passed from the previous activity or fetched from Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home_page);  // Set the correct layout XML

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        jobTitle = findViewById(R.id.jobTitle);
        jobDescription = findViewById(R.id.jobDescription);
        jobAddress = findViewById(R.id.jobAddress);
        jobIssues = findViewById(R.id.jobIssues);
        jobUser = findViewById(R.id.jobUser);
        statusSpinner = findViewById(R.id.statusSpinner);
        updateStatusButton = findViewById(R.id.updateStatusButton);

        // Assume jobId is passed through intent or fetched from Firestore
        jobId = "your_job_id_here";  // Replace this with the actual job ID

        // Fetch and display the job details from Firestore (for example, you can get the job details here)
        loadJobDetails();

        // Update status button click listener
        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected status from the spinner
                String selectedStatus = statusSpinner.getSelectedItem().toString();

                // Update the status in Firestore
                updateJobStatus(selectedStatus);
            }
        });
    }

    private void loadJobDetails() {
        // Fetch the job details from Firestore using the jobId
        DocumentReference jobRef = db.collection("jobs").document(jobId);

        jobRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Populate the job details in the UI
                jobTitle.setText(documentSnapshot.getString("title"));
                jobDescription.setText(documentSnapshot.getString("description"));
                jobAddress.setText("Address: " + documentSnapshot.getString("address"));
                jobIssues.setText("Issues: " + documentSnapshot.getString("issues"));
                jobUser.setText("User: " + documentSnapshot.getString("user"));
            } else {
                Toast.makeText(WorkerHomePage.this, "Job not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(WorkerHomePage.this, "Error loading job", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateJobStatus(String status) {
        // Reference to the job document in Firestore
        DocumentReference jobRef = db.collection("jobs").document(jobId);

        // Update the status field in Firestore
        jobRef.update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WorkerHomePage.this, "Status updated to " + status, Toast.LENGTH_SHORT).show();

                    // If status is "Completed", remove the job
                    if ("Completed".equals(status)) {
                        deleteJobFromFirestore();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WorkerHomePage.this, "Error updating status", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteJobFromFirestore() {
        // Delete the job from Firestore
        DocumentReference jobRef = db.collection("jobs").document(jobId);
        jobRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WorkerHomePage.this, "Job completed and removed", Toast.LENGTH_SHORT).show();
                    finish();  // Finish the activity and go back to previous page
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WorkerHomePage.this, "Error removing job", Toast.LENGTH_SHORT).show();
                });
    }
}
