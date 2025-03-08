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
import com.example.householdservice.R;
import com.example.householdservice.serviceDetails;

import java.util.List;

import model.ServiceModel;
import java.text.SimpleDateFormat;

import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> serviceList;
    private Context context;


    public ServiceAdapter(List<ServiceModel> serviceList, Context context) {
        this.serviceList = serviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = serviceList.get(position);
        holder.title.setText(service.getIssue());
        holder.status.setText(service.getStatus());

        if (service.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.date.setText(sdf.format(service.getTimestamp()));
        } else {
            holder.date.setText("No date available");
        }

        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Glide.with(context).load(service.getImageUrl()).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.repair_service);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, serviceDetails.class);
            intent.putExtra("serviceId", service.getServiceId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView title, status,date;
        ImageView image;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.serviceTitle);
            status = itemView.findViewById(R.id.serviceStatus);
            image = itemView.findViewById(R.id.serviceImage);
            date = itemView.findViewById(R.id.serviceDate);        }
    }
}
