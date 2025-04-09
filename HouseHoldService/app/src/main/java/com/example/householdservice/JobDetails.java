package com.example.householdservice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

public class JobDetails extends AppCompatActivity {

    private TextView jobTitleTextView, jobAddressTextView, jobDescriptionTextView;
    private ImageView jobImageView;
    private Button acceptButton, declineButton;

    private FirebaseFirestore db;
    private String Id;        // Document ID in MatchedJobs
    private String jobId;     // Actual jobId from Service collection
    private String workerId;
    private ImageView fullImageView;
    private View fullImageLayout;
    private ImageView closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // UI references
        jobTitleTextView = findViewById(R.id.jobTitleDetail);
        jobAddressTextView = findViewById(R.id.jobAddressDetail);
        jobDescriptionTextView = findViewById(R.id.jobDescriptionDetail);
        jobImageView = findViewById(R.id.jobImageDetail);
        acceptButton = findViewById(R.id.btnAcceptJob);
        declineButton = findViewById(R.id.btnDeclineJob);

        fullImageLayout = findViewById(R.id.fullImageLayout); // The overlay container
        fullImageView = findViewById(R.id.fullImageView);
        closeButton = findViewById(R.id.closeButton);

        // Get the document ID from intent
        Id = getIntent().getStringExtra("jobId");

        // Load jobId from MatchedJobs and then load full job details
        db.collection("MatchedJobs").document(Id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        jobId = documentSnapshot.getString("jobId");
                        if (jobId != null) {
                            loadJobDetailsFromService(jobId);

                            acceptButton.setOnClickListener(v -> checkIfWorkerHasAcceptedJob());
                            declineButton.setOnClickListener(v -> declineJob());
                        } else {
                            Toast.makeText(this, "Job ID missing", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Matched Job not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading matched job", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadJobDetailsFromService(String jobId) {
        db.collection("Service").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("issue");
                        String address = documentSnapshot.getString("address");
                        String client = documentSnapshot.getString("name");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        GeoPoint location = documentSnapshot.getGeoPoint("location");
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        jobTitleTextView.setText(title != null ? title : "No Title");
                        jobAddressTextView.setText(address != null ? address : "No Address");
                        jobDescriptionTextView.setText(client != null ? client : "No Description");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(jobImageView);
                        }
                        if (latitude != 0.0 && longitude != 0.0) {
                            loadMapLocation(latitude, longitude);
                        }
                        jobImageView.setOnClickListener(v -> {
                            if (imageUrl != null) {
                                Glide.with(this).load(imageUrl).into(fullImageView);
                                fullImageLayout.setVisibility(View.VISIBLE); // Show overlay
                            }
                        });
                    } else {
                        Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load job details", Toast.LENGTH_SHORT).show());
        closeButton.setOnClickListener(v -> fullImageLayout.setVisibility(View.GONE));
    }


    private void loadMapLocation(double lat, double lng) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.jobLocationMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                LatLng jobLocation = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions().position(jobLocation).title("Job Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jobLocation, 15));
            });
        }
    }


    private void checkIfWorkerHasAcceptedJob() {
        db.collection("workerId").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String jobId = documentSnapshot.getString("jobId");
                    if (jobId != null && !jobId.isEmpty()) {
                            Toast.makeText(this, "You already accepted another job", Toast.LENGTH_SHORT).show();
                            acceptButton.setEnabled(false);

                    }
                    else{
                        acceptJob();
                    }
                });
    }

    private void acceptJob() {
        // 1. Update status in MatchedJobs
        db.collection("MatchedJobs").document(Id)
                .update("status", "accepted")
                .addOnSuccessListener(unused -> {
                    // 2. Add jobId to worker document
                    db.collection("workerId").document(workerId)
                            .update("jobId", jobId)
                            .addOnSuccessListener(unused2 -> {
                                // 3. Update job status in Service
                                db.collection("Service").document(jobId)
                                        .update(
                                                "status", "worker_assigned",
                                                "workerId", workerId
                                        )
                                        .addOnSuccessListener(unused3 -> {
                                            Toast.makeText(this, "Job Accepted", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to update service status", Toast.LENGTH_SHORT).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to assign job to worker", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to accept matched job", Toast.LENGTH_SHORT).show()
                );
    }


    private void declineJob() {
        // Add the declined jobId to worker's declinedJobs list
        db.collection("workerId").document(workerId)
                .update("declinedJobs", FieldValue.arrayUnion(jobId))
                .addOnSuccessListener(unused -> {
                    // Then remove from matched jobs
                    db.collection("MatchedJobs").document(Id)
                            .delete()
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Job Declined", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error declining job", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating declined jobs", Toast.LENGTH_SHORT).show());
    }

}
