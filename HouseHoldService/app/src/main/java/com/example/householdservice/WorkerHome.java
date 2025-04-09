package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import model.JobMatcher;

public class WorkerHome extends AppCompatActivity {

    private ImageView backArrow;
    private FirebaseAuth mAuth;
    private Button AvailableJobs;
    private Button joDetails;
    private Button profileButton;
    private Button completedJobs;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        String workerId = mAuth.getUid();
        new JobMatcher().matchJobsToWorker(workerId);
        // Ensure your XML layout file is named correctly

        // Initialize views
        backArrow = findViewById(R.id.backArrow);
        AvailableJobs = findViewById(R.id.requestservice);
        joDetails = findViewById(R.id.viewstatus);
        completedJobs = findViewById(R.id.completed);
        profileButton = findViewById(R.id.profile);

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back Arrow functionality
        backArrow.setOnClickListener(view -> finish()); // Closes the current activity

        // "Request Service" button functionality
        AvailableJobs.setOnClickListener(view -> {
            // Navigate to the service request activity or show a message
            Intent intent = new Intent(WorkerHome.this, AvailableJobs.class);
            startActivity(intent);
        });

        // "View Status" button functionality
        joDetails.setOnClickListener(view -> {
            // Navigate to the status view activity or show a message
            Intent intent = new Intent(WorkerHome.this, CurrentJob.class);
            startActivity(intent);
        });

        completedJobs.setOnClickListener(view -> {
            // Navigate to the status view activity or show a message
            Intent intent = new Intent(WorkerHome.this, Completed_jobs.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(WorkerHome.this, ProfileView.class);
            startActivity(intent);
        });
    }
}
