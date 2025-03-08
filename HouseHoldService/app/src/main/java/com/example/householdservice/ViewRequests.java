package com.example.householdservice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdservice.adapter.ServiceAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import model.ServiceModel;

public class ViewRequests extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<ServiceModel> serviceList;
    private FirebaseFirestore db;
    private String userId;
    private TextView emptyView;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backArrow = findViewById(R.id.backArrow);

        serviceList = new ArrayList<>();
        adapter = new ServiceAdapter(serviceList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Get logged-in user ID
        backArrow.setOnClickListener(v ->finish());

        loadUserServices();
    }

    private void loadUserServices() {
        db.collection("Service")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)// Filter services by current user ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ServiceModel service = document.toObject(ServiceModel.class);
                        service.setServiceId(document.getId()); // Store document ID
                        serviceList.add(service);
                    }
                    if (serviceList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load services", e));
    }
}
