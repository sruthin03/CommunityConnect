package com.example.householdservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.householdservice.JobDetails;
import com.example.householdservice.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import model.Job;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private Context context;
    private FirebaseFirestore db;

    public JobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        // Set temporary loading state
        holder.title.setText("Loading...");
        holder.address.setText("Loading...");
        holder.image.setImageResource(R.drawable.repair_service);

        // Fetch service data
        db.collection("Service").document(job.getjobId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String address = documentSnapshot.getString("address");
                        String title = documentSnapshot.getString("issue");

                        holder.title.setText(title != null ? title : "No Title");
                        holder.address.setText(address != null ? address : "No Address");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.repair_service)
                                    .into(holder.image);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Optional: Log or handle error
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetails.class);
            intent.putExtra("jobId", job.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView title, address;
        ImageView image;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.jobTitle);
            address = itemView.findViewById(R.id.JobAddress);
            image = itemView.findViewById(R.id.JobImage);
        }
    }
}
