package model;

import android.util.Log;
import com.google.firebase.firestore.*;

public class WorkerListener {
    private FirebaseFirestore db;

    public WorkerListener() {
        db = FirebaseFirestore.getInstance();
        listenForNewWorkers();
    }

    private void listenForNewWorkers() {
        db.collection("workerId")
                .whereEqualTo("jobId", null)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("WorkerListener", "Error listening for new workers", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String workerId = dc.getDocument().getId();
                            new JobMatcher().findMatchingJobs(workerId);
                        }
                    }
                });
    }
}
