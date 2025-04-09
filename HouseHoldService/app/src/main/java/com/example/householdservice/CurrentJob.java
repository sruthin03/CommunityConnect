package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class CurrentJob extends AppCompatActivity {

    private FirebaseFirestore db;
    private String workerId, jobId;

    private TextView jobTitleTextView, jobAddressTextView, clientNameTextView, clientPhoneTextView;
    private ImageView jobImageView, fullImageView, closeButton;
    private View fullImageLayout;
    private Button completeJobButton, findJobsButton;
    private LinearLayout jobDetailsLayout,noJobLayout;
    private TextView noJobText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_job);

        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // UI Elements
        jobTitleTextView = findViewById(R.id.jobTitleDetail);
        jobAddressTextView = findViewById(R.id.jobAddressDetail);
        clientNameTextView = findViewById(R.id.clientNameDetail);
        clientPhoneTextView = findViewById(R.id.clientPhoneDetail);
        jobImageView = findViewById(R.id.jobImageDetail);
        fullImageView = findViewById(R.id.fullImageView);
        closeButton = findViewById(R.id.closeButton);
        fullImageLayout = findViewById(R.id.fullImageLayout);
        completeJobButton = findViewById(R.id.btnCompleteJob);
        findJobsButton = findViewById(R.id.findJobsButton);
        jobDetailsLayout = findViewById(R.id.jobDetailsLayout);
        noJobLayout = findViewById(R.id.noJobLayout);
        noJobText = findViewById(R.id.noJobText);

        findJobsButton.setOnClickListener(v -> {
            startActivity(new Intent(CurrentJob.this, AvailableJobs.class));
            finish();
        });

        closeButton.setOnClickListener(v -> fullImageLayout.setVisibility(View.GONE));

        checkCurrentJob();
    }

    private void checkCurrentJob() {
        db.collection("workerId").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    jobId = documentSnapshot.getString("jobId");
                    if (jobId != null && !jobId.isEmpty()) {
                        loadJobDetails(jobId);
                    } else {
                        showNoJobUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching current job", Toast.LENGTH_SHORT).show();
                    showNoJobUI();
                });
    }

    private void showNoJobUI() {
        jobDetailsLayout.setVisibility(View.GONE);
        fullImageLayout.setVisibility(View.GONE);
        noJobLayout.setVisibility(View.VISIBLE);
    }

    private void loadJobDetails(String jobId) {
        db.collection("Service").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        jobDetailsLayout.setVisibility(View.VISIBLE);
                        noJobText.setVisibility(View.GONE);
                        findJobsButton.setVisibility(View.GONE);

                        String title = documentSnapshot.getString("issue");
                        String address = documentSnapshot.getString("address");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        GeoPoint location = documentSnapshot.getGeoPoint("location");
                        String userId = documentSnapshot.getString("userId");

                        jobTitleTextView.setText(title != null ? title : "No Title");
                        jobAddressTextView.setText(address != null ? "Address : "+address : "No Address");

                        if (userId != null && !userId.isEmpty()) {
                            db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String phone = userDoc.getString("mobile");
                                            clientPhoneTextView.setText("Contact : "+(phone != null ? phone : "N/A"));
                                            String clientName = userDoc.getString("name");
                                            clientNameTextView.setText("Client: " + (clientName != null ? clientName : "N/A"));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        clientPhoneTextView.setText("Phone : N/A");
                                        clientNameTextView.setText("Client : N/A");
                                    });
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(jobImageView);
                            jobImageView.setOnClickListener(v -> {
                                Glide.with(this).load(imageUrl).into(fullImageView);
                                fullImageLayout.setVisibility(View.VISIBLE);
                            });
                        }

                        if (location != null) {
                            loadMapLocation(location.getLatitude(), location.getLongitude());
                        }

                        completeJobButton.setOnClickListener(v -> showCompleteJobPopup(jobId));
                    } else {
                        showNoJobUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load job details", Toast.LENGTH_SHORT).show();
                    showNoJobUI();
                });
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

    private void showCompleteJobPopup(String jobId) {
        new AlertDialog.Builder(this)
                .setTitle("Complete Job")
                .setMessage("Are you sure you have completed this job?")
                .setPositiveButton("Yes", (dialog, which) -> completeJob(jobId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void completeJob(String jobId) {
        db.collection("Service").document(jobId)
                .update("status", "completed")
                .addOnSuccessListener(unused -> {
                    WriteBatch batch = db.batch();

                    DocumentReference workerRef = db.collection("workerId").document(workerId);
                    DocumentReference matchedJobRef = db.collection("matchedJobs").document(jobId);

                    batch.update(workerRef, "jobId", FieldValue.delete());
                    batch.update(workerRef, "completedJobs", FieldValue.arrayUnion(jobId));
                    batch.delete(matchedJobRef);

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Job completed", Toast.LENGTH_SHORT).show();
                                recreate();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error finalizing job", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error completing job", Toast.LENGTH_SHORT).show());
    }
}
