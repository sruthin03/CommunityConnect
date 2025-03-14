package com.example.householdservice.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdservice.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import model.JobRequest;

public class JobRequestAdapter extends RecyclerView.Adapter<JobRequestAdapter.ViewHolder> {
    private List<JobRequest> jobRequestList;
    private Context context;

    public JobRequestAdapter(List<JobRequest> jobRequestList, Context context) {
        this.jobRequestList = jobRequestList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobRequest jobRequest = jobRequestList.get(position);
        holder.jobTitle.setText("Job: " + jobRequest.getJobTitle());
        holder.clientName.setText("Client: " + jobRequest.getClientName());

        holder.acceptBtn.setOnClickListener(v -> updateJobStatus(jobRequest.getId(), "accepted"));
        holder.declineBtn.setOnClickListener(v -> updateJobStatus(jobRequest.getId(), "declined"));
    }

    @Override
    public int getItemCount() {
        return jobRequestList.size();
    }

    private void updateJobStatus(String jobRequestId, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("JobNotifications").document(jobRequestId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Job " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("JobRequestAdapter", "Error updating job status", e));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, clientName;
        Button acceptBtn, declineBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            clientName = itemView.findViewById(R.id.clientName);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
        }
    }
}
