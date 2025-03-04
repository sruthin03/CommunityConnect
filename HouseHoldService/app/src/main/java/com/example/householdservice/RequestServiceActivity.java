package com.example.householdservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import model.Job;

public class RequestServiceActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private static final int GALLERY_REQUEST_CODE = 102;
    private static final int GALLERY_PERMISSION_CODE = 103;
    private Uri imageUri;  // Store the selected image URI

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service);
        userId = getIntent().getStringExtra("USER_ID");

        //mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //currentUser = mAuth.getCurrentUser();  // Get the current user from FirebaseAuth

        Spinner imageOptionsSpinner = findViewById(R.id.spinner);
        Button submitButton = findViewById(R.id.button9);
        EditText editTextIssue = findViewById(R.id.editTextTextIssue);
        Spinner serviceTypeSpinner = findViewById(R.id.spinner4);

        // Spinner item selection listener to select image option
        imageOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {

                    requestCameraPermission();
                } else if (position == 2) {
                    // Option to upload from gallery
                    openGallery();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        submitButton.setOnClickListener(v -> {
            // Get the job details (e.g., from EditText and Spinner)
            String serviceType = serviceTypeSpinner.getSelectedItem().toString();
            String issue = editTextIssue.getText().toString().trim();

            // Validate input fields
            if (serviceType.isEmpty() || issue.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return; // Prevents further execution
            }

            // Upload the job details and match with the user
            uploadJobDetails(serviceType, issue);
        });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // Permission already granted, open the camera
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openGallery();
            } else {
                // Permission denied
                Toast.makeText(this, "Gallery permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, GALLERY_PERMISSION_CODE);
        } else {
            // Permission already granted, open the camera
            openGallery();
        }
    }


    // Open the camera to capture a photo
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }


    // Open the gallery to select an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    // Handle the result from camera or gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Camera returned a photo
                // imageUri is already set in the openCamera method
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Gallery returned a photo
                if (data != null) {
                    imageUri = data.getData();
                }
            }
        }
    }

    private File createImageFile() {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Upload job details including image to Firestore and Firebase Storage
    private void uploadJobDetails(String serviceType, String issue) {
        // Fetch user data from Firestore based on the current user ID
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get user details from the Firestore document
                String User = documentSnapshot.getString("name") != null ? documentSnapshot.getString("name") : "Unknown";
                String userDistrict = documentSnapshot.getString("district") != null ? documentSnapshot.getString("district") : "Unknown";
                String address = documentSnapshot.getString("address") != null ? documentSnapshot.getString("address") : "Unknown";


                // Proceed with the job details upload
                if (imageUri != null) {
                    // Reference to Firebase Storage
                    StorageReference storageRef = storage.getReference().child("job_images/" + UUID.randomUUID().toString());

                    // Upload image to Firebase Storage
                    storageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        // Image URL retrieved from Firebase Storage
                                        String imageUrl = uri.toString();

                                        // Create a job object to save in Firestore
                                        Map<String, Object> jobDetails = new HashMap<>();
                                        jobDetails.put("job", serviceType);
                                        jobDetails.put("issue", issue);
                                        jobDetails.put("address", address);
                                        jobDetails.put("name", User);  // Add user name
                                        jobDetails.put("userDistrict", userDistrict);  // Add user district
                                        jobDetails.put("imageUrl", imageUrl);
                                        jobDetails.put("userId", userId);
                                        jobDetails.put("timestamp", FieldValue.serverTimestamp());

                                        // Save job details to Firestore
                                        firestore.collection("Service")
                                                .add(jobDetails)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Job successfully saved
                                                    Toast.makeText(this, "Job request submitted!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure
                                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                } else {
                    // If no image is selected, just save the job details without the image
                    Map<String, Object> jobDetails = new HashMap<>();
                    jobDetails.put("serviceType", serviceType);
                    jobDetails.put("address", address);
                    jobDetails.put("name", User);  // Add user name
                    jobDetails.put("userDistrict", userDistrict);  // Add user district
                    jobDetails.put("imageUrl", null); // No image URL
                    jobDetails.put("timestamp", FieldValue.serverTimestamp());

                    firestore.collection("Service")
                            .add(jobDetails)
                            .addOnSuccessListener(documentReference -> {
                                // Job successfully saved
                                Toast.makeText(this, "Job request submitted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                // User document doesn't exist
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle failure to fetch user data
            Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
