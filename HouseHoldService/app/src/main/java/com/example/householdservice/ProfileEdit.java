package com.example.householdservice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileEdit extends AppCompatActivity {

    private EditText editName, editEmail, editPhone, editAddress;
    private Button updateProfileBtn;
    private ImageButton backArrow;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Firebaseimport java.util.Map;
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize UI Elements
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        backArrow = findViewById(R.id.backArrow);
        updateProfileBtn = findViewById(R.id.updateProfileBtn);

        loadUserData();

        backArrow.setOnClickListener(v ->finish());


        // Update Profile Button
        updateProfileBtn.setOnClickListener(view -> updateProfile());
    }

    private void loadUserData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(userId);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    editName.setText(documentSnapshot.getString("name"));
                    editEmail.setText(documentSnapshot.getString("email"));
                    editPhone.setText(documentSnapshot.getString("mobile"));
                    editAddress.setText(documentSnapshot.getString("address"));
                }
            });
        }
    }

    private void updateProfile() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", editName.getText().toString());
            updates.put("email", editEmail.getText().toString());
            updates.put("phone", editPhone.getText().toString());
            updates.put("address", editAddress.getText().toString());

            docRef.update(updates).addOnSuccessListener(unused ->
                    Toast.makeText(ProfileEdit.this, "Profile Updated", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(ProfileEdit.this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            );
            finish();
        }
    }
}
