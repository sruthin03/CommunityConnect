package model;

import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WorkerNotificationListener {
    private FirebaseFirestore db;

    public WorkerNotificationListener() {
        db = FirebaseFirestore.getInstance();
        listenForWorkerResponses();
    }

    private void listenForWorkerResponses() {
        db.collection("JobNotifications")
                .whereEqualTo("status", "pending") // Listen for pending job notifications
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("WorkerNotificationListener", "Error listening for worker responses", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            String notificationId = dc.getDocument().getId();
                            String workerId = dc.getDocument().getString("workerId");
                            String jobId = dc.getDocument().getString("jobId");
                            String status = dc.getDocument().getString("status");

                            if ("accepted".equals(status)) {
                                assignJobToWorker(jobId, workerId, notificationId);
                            } else if ("declined".equals(status)) {
                                findNextWorker(jobId, workerId, notificationId);
                            }
                        }
                    }
                });
    }

    private void assignJobToWorker(String jobId, String workerId, String notificationId) {
        DocumentReference jobRef = db.collection("Service").document(jobId);
        jobRef.update("workerId", workerId);
                jobRef.update("status","Worker Assigned")
                .addOnSuccessListener(aVoid -> {
                    Log.d("WorkerNotificationListener", "Job assigned successfully to worker: " + workerId);
                    // Remove the job notification after assignment
                    db.collection("JobNotifications").document(notificationId).delete();
                })
                .addOnFailureListener(e -> Log.e("WorkerNotificationListener", "Error assigning job", e));
    }

    private void findNextWorker(String jobId, String declinedWorkerId, String notificationId) {
        Log.d("WorkerNotificationListener", "Worker declined. Searching for next available worker...");

        // Remove the declined notification
        db.collection("JobNotifications").document(notificationId).delete();

        // Find next worker for the job (re-run job matching)
        new JobMatcher().matchJobToWorker(jobId);
    }
}
