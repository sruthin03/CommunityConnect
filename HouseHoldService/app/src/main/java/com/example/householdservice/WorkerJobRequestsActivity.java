package com.example.householdservice;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdservice.adapter.JobRequestAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import model.JobRequest;

public class WorkerJobRequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private JobRequestAdapter jobRequestAdapter;
    private List<JobRequest> jobRequestList;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_job_requests);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        jobRequestList = new ArrayList<>();
        jobRequestAdapter = new JobRequestAdapter(jobRequestList, this);
        recyclerView.setAdapter(jobRequestAdapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            fetchJobRequests();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchJobRequests() {
        String workerId = currentUser.getUid();
        CollectionReference jobRequestsRef = db.collection("JobNotifications");

        jobRequestsRef.whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobRequestList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        JobRequest jobRequest = document.toObject(JobRequest.class);
                        jobRequest.setId(document.getId()); // Store document ID for updates
                        jobRequestList.add(jobRequest);
                    }
                    jobRequestAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("WorkerJobRequests", "Error fetching job requests", e));
    }
}
