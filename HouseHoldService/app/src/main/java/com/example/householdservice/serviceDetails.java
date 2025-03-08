package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class serviceDetails extends AppCompatActivity {

    private ImageView Image;
    private TextView service, issue, status,date;
    private FirebaseFirestore db;
    private ImageView backArrow;
    private ImageView fullImageView;
    private View fullImageLayout;
    private ImageView closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_details);

        Image = findViewById(R.id.serviceImage);
        service = findViewById(R.id.serviceTitle);
        issue = findViewById(R.id.serviceDescription);
        date = findViewById(R.id.serviceDate);
        status = findViewById(R.id.servicePrice);
        backArrow = findViewById(R.id.backArrow);

        fullImageLayout = findViewById(R.id.fullImageLayout); // The overlay container
        fullImageView = findViewById(R.id.fullImageView);
        closeButton = findViewById(R.id.closeButton);

        db = FirebaseFirestore.getInstance();

        // Get service ID from intent
        String serviceId = getIntent().getStringExtra("serviceId");

        backArrow.setOnClickListener(v-> finish());


        if (serviceId != null) {
            loadServiceDetails(serviceId);
        }
    }

    private void loadServiceDetails(String serviceId) {
        db.collection("Service").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("service");
                        String description = documentSnapshot.getString("issue");
                        String stat = documentSnapshot.getString("status");
                        Date reqDate= documentSnapshot.getDate("timestamp");
                        String imageUrl = documentSnapshot.getString("imageUrl"); // Cloudinary URL

                        service.setText(description);
                        issue.setText(title);
                        status.setText(stat);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                        String FormattedDate = sdf.format(reqDate);
                        date.setText(FormattedDate);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Image.setVisibility(View.VISIBLE);
                            Glide.with(this).load(imageUrl).into(Image);
                        }
                        else{
                            Image.setImageResource(R.drawable.repair_service);
                        }
                        Image.setOnClickListener(v -> {
                            if (imageUrl != null) {
                                Glide.with(this).load(imageUrl).into(fullImageView);
                                fullImageLayout.setVisibility(View.VISIBLE); // Show overlay
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    service.setText("Failed to load service details");
                });
        closeButton.setOnClickListener(v -> fullImageLayout.setVisibility(View.GONE));
    }

}
