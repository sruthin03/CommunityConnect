package com.example.householdservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class ProfileEdit extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 1; // Request code for location selection

    private EditText editName, editEmail, editPhone, editInfo;
    private Button updateProfileBtn, changeAddressBtn;
    private ImageButton backArrow;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userType = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();

        // Initialize UI Elements
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editInfo = findViewById(R.id.editAddress);
        changeAddressBtn = findViewById(R.id.changeAddressBtn);
        backArrow = findViewById(R.id.backArrow);
        updateProfileBtn = findViewById(R.id.updateProfileBtn);

        backArrow.setOnClickListener(v -> finish());

        if (currentUser != null) {
            getUserType(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        changeAddressBtn.setOnClickListener(view -> openLocationSelectActivity());

        // Update Profile Button
        updateProfileBtn.setOnClickListener(view -> updateProfile());
    }

    private void getUserType(String userId) {

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userType = "users";
                        editInfo.setVisibility(View.VISIBLE);
                    } else {
                        db.collection("workerId").document(userId).get()
                                .addOnSuccessListener(workerSnapshot -> {
                                    if (workerSnapshot.exists()) {
                                        userType = "workerId";
                                        editInfo.setVisibility(View.GONE);
                                    }
                                });

                    }
                    loadUserData();
                });
    }

    private void loadUserData() {
        if (userType == null || userType.equals("invalid")) return;
        DocumentReference docRef = db.collection(userType).document(currentUser.getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (userType.equals("users")) {
                    editInfo.setText(documentSnapshot.getString("address"));
                }
                editName.setText(documentSnapshot.getString("name"));
                editEmail.setText(documentSnapshot.getString("email"));
                editPhone.setText(documentSnapshot.getString("mobile"));

                // Retrieve location properly
                GeoPoint geoPoint = documentSnapshot.getGeoPoint("location");
                if (geoPoint != null) {
                    selectedLatitude = geoPoint.getLatitude();
                    selectedLongitude = geoPoint.getLongitude();
                    Log.d("FirestoreDebug", "Loaded Location: Lat " + selectedLatitude + ", Lon " + selectedLongitude);
                } else {
                    Log.d("FirestoreDebug", "Location is null in Firestore");
                }
            } else {
                Log.d("FirestoreDebug", "Document does not exist!");
            }
        }).addOnFailureListener(e -> Log.e("FirestoreDebug", "Error fetching data", e));
    }


    private void openLocationSelectActivity(                                ) {
        Intent intent = new Intent(ProfileEdit.this, LocationSelectionActivity.class);
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);

        }
    }

    private void updateProfile() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection(userType).document(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", editName.getText().toString());
            updates.put("email", editEmail.getText().toString());
            updates.put("mobile", editPhone.getText().toString());

            if (userType.equals("users")) {
                updates.put("address", editInfo.getText().toString());
            }

            GeoPoint geoPoint = new GeoPoint(selectedLatitude, selectedLongitude);
            updates.put("location", geoPoint);

            docRef.update(updates)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(ProfileEdit.this, "Profile Updated", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(ProfileEdit.this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    );
            finish();
        }
    }
}
