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


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileView extends AppCompatActivity {

    private TextView mobileTextView, emailTextView, addressTextView, nameTextView;
    private Button changePassword;
    private Button editProfile;
    private Button logOut;
    private FirebaseFirestore firestore;
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ImageButton backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view); // Set your actual layout name here

        // Initialize Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        mobileTextView = findViewById(R.id.mobileTextView);
        emailTextView = findViewById(R.id.emailTextView);
        addressTextView = findViewById(R.id.addressTextView);
        nameTextView = findViewById(R.id.textView12);
        changePassword = findViewById(R.id.updatePassword);
        editProfile = findViewById(R.id.editProfile);// This is where the name will be displayed
        logOut = findViewById(R.id.logout);
        backArrow = findViewById(R.id.backArrow);
        // Assuming userId is passed from the previous activity

        // Load user details from Firestore
        loadUserProfile();

        backArrow.setOnClickListener(v -> finish());

        editProfile.setOnClickListener(view->{
            Intent intent = new Intent(ProfileView.this, ProfileEdit.class);
            startActivity(intent);
        });
        changePassword.setOnClickListener(view->{
            Intent intent = new Intent(ProfileView.this, UpdatePassword.class);
            startActivity(intent);
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut(); // Firebase Logout
                Intent intent = new Intent(ProfileView.this, Login_page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            });
        }
        private void loadUserProfile() {
        userId = currentUser.getUid();
        firestore.collection("users").document(userId) // Adjust collection name if needed
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Retrieve data from Firestore and set to TextViews
                                String name = document.getString("name");
                                String mobile = document.getString("mobile");
                                String email = document.getString("email");
                                String address = document.getString("address");

                                // Set data to the TextViews
                                nameTextView.setText(name);
                                mobileTextView.setText(mobile);
                                emailTextView.setText(email);
                                addressTextView.setText(address);
                            } else {
                                Toast.makeText(ProfileView.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileView.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
