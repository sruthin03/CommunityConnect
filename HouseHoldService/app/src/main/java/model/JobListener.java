package model;

import android.util.Log;
import com.google.firebase.firestore.*;

public class JobListener {
    private FirebaseFirestore db;

    public JobListener() {
        db = FirebaseFirestore.getInstance();
        listenForNewJobs();
    }

    private void listenForNewJobs() {
        db.collection("Service")
                .whereEqualTo("workerId", null)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("JobListener", "Error listening for new jobs", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String jobId = dc.getDocument().getId();
                            new JobMatcher().matchJobToWorker(jobId);
                        }
                    }
                });
    }
}
