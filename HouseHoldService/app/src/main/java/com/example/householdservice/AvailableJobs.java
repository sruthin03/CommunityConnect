package com.example.householdservice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdservice.adapter.JobAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import model.Job;
import model.JobMatcher;

public class AvailableJobs extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentWorkerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_jobs);

        recyclerView = findViewById(R.id.recyclerViewJobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter(this, jobList);
        recyclerView.setAdapter(jobAdapter);

        db = FirebaseFirestore.getInstance();
        currentWorkerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Match jobs to worker every time screen opens, then load available jobs
        new JobMatcher().matchJobsToWorker(currentWorkerId);
        loadAvailableJobs();
    }

    private void loadAvailableJobs() {
        db.collection("MatchedJobs")
                .whereEqualTo("workerId", currentWorkerId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        Timestamp timestamp = doc.getTimestamp("matchedAt");

                        // Delete job if it's older than 12 hours
                        if (timestamp != null && Timestamp.now().getSeconds() - timestamp.getSeconds() > 43200) {
                            db.collection("MatchedJobs").document(doc.getId()).delete();
                        } else {
                            jobList.add(job);
                        }
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }
}