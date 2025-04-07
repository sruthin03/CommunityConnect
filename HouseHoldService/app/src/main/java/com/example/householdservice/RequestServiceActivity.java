package com.example.householdservice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import model.JobMatcher;
import okhttp3.*;

public class RequestServiceActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private Spinner serviceSpinner, imageSpinner;
    private EditText issueDescription;
    private Button submitButton;
    private ImageView imageView;
    private Uri selectedImageUri;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        setContentView(R.layout.activity_request_service);

        serviceSpinner = findViewById(R.id.spinner4);
        imageSpinner = findViewById(R.id.spinner);
        issueDescription = findViewById(R.id.editTextTextIssue);
        submitButton = findViewById(R.id.button9);
        imageView = findViewById(R.id.selectedImageView);
        db = FirebaseFirestore.getInstance();

        ImageButton backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        // Handle Image Selection
        imageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                if (selectedOption.equals("Choose from gallery")) {
                    openGallery();
                } else if (selectedOption.equals("Take a photo")) {
                    openCamera();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle Submit Button Click
        submitButton.setOnClickListener(v -> {
            String selectedService = serviceSpinner.getSelectedItem().toString();
            String issueText = issueDescription.getText().toString().trim();

            if (selectedService.isEmpty() || issueText.isEmpty()) {
                Toast.makeText(RequestServiceActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageToCloudinary(selectedImageUri, selectedService, issueText);
            } else {
                submitRequest(selectedService, issueText, "");
            }
        });

    }

    // ✅ Image Picker for Gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imageView.setImageURI(selectedImageUri);
                    }
                }
            });

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        }
    }

    // ✅ Camera Launcher - Handles Bitmap Properly
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            imageView.setImageBitmap(imageBitmap);
                            selectedImageUri = getImageUriFromBitmap(imageBitmap);
                        }
                    }
                }
            });

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        }
    }

    // ✅ Convert Bitmap to URI
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        try {
            File tempFile = File.createTempFile("camera_image", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Upload Image to Cloudinary
    private void uploadImageToCloudinary(Uri imageUri, String service, String issue) {
        new Thread(() -> {
            try {
                File imageFile = getFileFromUri(imageUri);
                if (imageFile == null) {
                    runOnUiThread(() -> Toast.makeText(RequestServiceActivity.this,
                            "Error: Cannot get file from URI!", Toast.LENGTH_SHORT).show());
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                String cloudinaryUrl = "https://api.cloudinary.com/v1_1/drvnwl8p2/image/upload";

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", imageFile.getName(),
                                RequestBody.create(MediaType.parse("image/*"), imageFile))
                        .addFormDataPart("upload_preset", "images")
                        .build();

                Request request = new Request.Builder()
                        .url(cloudinaryUrl)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    String uploadedImageUrl = extractImageUrl(responseBody);

                    runOnUiThread(() -> submitRequest(service, issue, uploadedImageUrl));
                } else {
                    runOnUiThread(() -> Toast.makeText(RequestServiceActivity.this,
                            "Image Upload Failed!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(RequestServiceActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ✅ Convert URI to File
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Extract URL from Cloudinary Response
    private String extractImageUrl(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getString("secure_url");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // ✅ Submit Request to Firestore
    private void submitRequest(String service, String issue, String imageUrl) {
        userId = currentUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get user details from the Firestore document
                String User =documentSnapshot.getString("name") != null ? documentSnapshot.getString("name") : "Unknown";
                GeoPoint geoPoint = documentSnapshot.getGeoPoint("location");
                assert geoPoint != null;
                double latitude = geoPoint.getLatitude();
                double longitude = geoPoint.getLongitude();
                String address = documentSnapshot.getString("address") != null ? documentSnapshot.getString("address") : "Unknown";

        Map<String, Object> request = new HashMap<>();
        request.put("service", service);
        request.put("issue", issue);
        request.put("imageUrl", imageUrl);
        request.put("address", address);
        request.put("name", User);  // Add user name
        request.put("location",new GeoPoint(latitude,longitude));
        request.put("userId", userId);
        request.put("status","pending");
        request.put("workerId","");
        request.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Service").add(request)
                .addOnSuccessListener(documentReference -> {
                    String jobId = documentReference.getId();
                    new JobMatcher().matchSingleJob(jobId);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding document", e));
                // After job is added to Firestore


            }
});
    }
}
