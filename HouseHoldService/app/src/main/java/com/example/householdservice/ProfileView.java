package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileView extends AppCompatActivity {

    private TextView mobileTextView, emailTextView, infoTextView, nameTextView;
    private Button changePassword, editProfile, logOut;
    private ImageButton backArrow;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userType = null; // Ensure it's initialized

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view); // Ensure correct layout file

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        mobileTextView = findViewById(R.id.mobileTextView);
        emailTextView = findViewById(R.id.emailTextView);
        infoTextView = findViewById(R.id.addressTextView); // Renamed for flexibility
        nameTextView = findViewById(R.id.textView12);
        changePassword = findViewById(R.id.updatePassword);
        editProfile = findViewById(R.id.editProfile);
        logOut = findViewById(R.id.logout);
        backArrow = findViewById(R.id.backArrow);

        // Ensure user is logged in before proceeding
        if (currentUser != null) {
            getUserType(); // Fetch user type before loading profile
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        backArrow.setOnClickListener(v -> finish());

        editProfile.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileView.this, ProfileEdit.class);
            startActivity(intent);
        });

        changePassword.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileView.this, UpdatePassword.class);
            startActivity(intent);
        });

        logOut.setOnClickListener(view -> {
            mAuth.signOut(); // Firebase Logout
            Intent intent = new Intent(ProfileView.this, Login_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Retrieves the user type (either "users" or "workers") before loading the profile.
     */
    private void getUserType() {
        String userId = currentUser.getUid();

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userType = "users";
                        loadUserProfile();
                    } else {
                        firestore.collection("workerId").document(userId) // Fixed collection name
                                .get()
                                .addOnSuccessListener(workerSnapshot -> {
                                    if (workerSnapshot.exists()) {
                                        userType = "workerId";
                                    } else {
                                        userType = "invalid";
                                        Toast.makeText(ProfileView.this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                    loadUserProfile(); // Load profile only after userType is set
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProfileView.this, "Error checking workers", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileView.this, "Error checking users", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads the user's profile details from Firestore based on their user type.
     */
    private void loadUserProfile() {
        if (userType == null || userType.equals("invalid")) {
            Toast.makeText(this, "User type invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection(userType).document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve data from Firestore
                            String name = document.getString("name");
                            String mobile = document.getString("mobile");
                            String email = document.getString("email");

                            // Set data to the TextViews
                            nameTextView.setText(name != null ? name : "N/A");
                            mobileTextView.setText(mobile != null ? mobile : "N/A");
                            emailTextView.setText(email != null ? email : "N/A");

                            // Display address for users, profession for workers
                            if (userType.equals("users")) {
                                String address = document.getString("address");
                                infoTextView.setText(address != null ? address : "N/A");
                            } else {
                                String profession = document.getString("profession");
                                infoTextView.setText(profession != null ? profession : "N/A");
                            }
                        } else {
                            Toast.makeText(ProfileView.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileView.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
