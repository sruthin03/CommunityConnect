package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import model.JobMatchingService;
import model.WorkerListener;

public class WorkerHome extends AppCompatActivity {

    private ImageView backArrow;
    private FirebaseAuth mAuth;
    private Button jobRequest;
    private Button joDetails;
    private Button profileButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home);
        Intent serviceIntent = new Intent(this, JobMatchingService.class);
        startService(serviceIntent);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ensure your XML layout file is named correctly

        // Initialize views
        backArrow = findViewById(R.id.backArrow);
        jobRequest = findViewById(R.id.requestservice);
        joDetails = findViewById(R.id.viewstatus);
        profileButton = findViewById(R.id.profile);

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back Arrow functionality
        backArrow.setOnClickListener(view -> finish()); // Closes the current activity

        // "Request Service" button functionality
        jobRequest.setOnClickListener(view -> {
            // Navigate to the service request activity or show a message
            Intent intent = new Intent(WorkerHome.this, JobRequests.class);
            startActivity(intent);
        });

        // "View Status" button functionality
        joDetails.setOnClickListener(view -> {
            // Navigate to the status view activity or show a message
            Intent intent = new Intent(WorkerHome.this, JobDetails.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(WorkerHome.this, ProfileView.class);
            startActivity(intent);
        });
    }
}
