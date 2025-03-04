package com.example.householdservice;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
//import com.squareup.picasso.Picasso;
import android.app.Service;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewStatusActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private LinearLayout issuesContainer;
    private String userId;// LinearLayout to display issues

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_status);
        userId = getIntent().getStringExtra("USER_ID");

        firestore = FirebaseFirestore.getInstance();
        issuesContainer = findViewById(R.id.issuesContainer);  // Initialize the LinearLayout

        // Fetch issues and listen for real-time updates
        fetchIssues();
    }

    private void fetchIssues() {
        CollectionReference issuesRef = firestore.collection("Service");

        // Listen for real-time changes to the issues collection
        issuesRef.whereEqualTo("userId", userId)  // Assuming that each issue document has a 'userId' field
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
            public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    return;
                }

                // Clear the current list of issues to avoid duplicates
                issuesContainer.removeAllViews();

                // Iterate through the fetched issues and display them
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String issueName = document.getString("issueName");
                    String reportedTime = document.getString("reportedTime");
                    String status = document.getString("status");
                    String url = document.getString("imageUrl");

                    // Create and add a new TextView for each issue
                    addIssueToUI(issueName, reportedTime, status,url);
                }
            }
        });
    }

    private void addIssueToUI(String issueName, String reportedTime, String status,String url) {
        // Create a new TextView for the issue details
        TextView issueTextView = new TextView(this);
        issueTextView.setText("Issue: " + issueName + "\nReported at: " + reportedTime + "\nStatus: " + status);
        issueTextView.setPadding(0, 10, 0, 10);
        issueTextView.setTextSize(18);
        ImageView issueImageView = new ImageView(this);
        issueImageView.setLayoutParams(new LinearLayout.LayoutParams(144, 134));
        issueImageView.setImageResource(R.drawable.repair_service);


            // Optionally, style the TextView (for example, setting colors or background)
        issueTextView.setBackgroundColor(getResources().getColor(R.color.light_gray)); // Sample background

        // Add the TextView to the LinearLayout
        issuesContainer.addView(issueTextView);
    }
}
