package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home_page extends AppCompatActivity {

    private ImageView backArrow;
    private FirebaseAuth mAuth;
    private TextView createAccountText;
    private Button requestServiceButton;
    private Button viewStatusButton;
    private Button profileButton;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ensure your XML layout file is named correctly

        // Initialize views
        backArrow = findViewById(R.id.backArrow);
        createAccountText = findViewById(R.id.createAccountText);
        requestServiceButton = findViewById(R.id.requestservice);
        viewStatusButton = findViewById(R.id.viewstatus);
        profileButton = findViewById(R.id.profile);

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back Arrow functionality
        backArrow.setOnClickListener(view -> finish()); // Closes the current activity

        // "Request Service" button functionality
        requestServiceButton.setOnClickListener(view -> {
            // Navigate to the service request activity or show a message
            Intent intent = new Intent(Home_page.this, RequestServiceActivity.class);
            startActivity(intent);
        });

        // "View Status" button functionality
        viewStatusButton.setOnClickListener(view -> {
            // Navigate to the status view activity or show a message
            Intent intent = new Intent(Home_page.this, ViewRequests.class);
            startActivity(intent);
        });

        // "Login as Worker" button functionality
        profileButton.setOnClickListener(view -> {
            // Navigate to worker login or registration activity
            Intent intent = new Intent(Home_page.this, ProfileView.class);
            startActivity(intent);
        });
    }
}
