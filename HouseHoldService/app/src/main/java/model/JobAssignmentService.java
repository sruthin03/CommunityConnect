package model;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class JobAssignmentService {

    private FirebaseFirestore db;

    public JobAssignmentService() {
        db = FirebaseFirestore.getInstance();
    }

    public void assignWorkerToJob(String jobId) {
        // Get the job details
        DocumentReference jobRef = db.collection("jobs").document(jobId);
        jobRef.get().addOnSuccessListener(jobSnapshot -> {
            if (jobSnapshot.exists()) {
                String district = jobSnapshot.getString("district");
                String profession = jobSnapshot.getString("profession");

                // Find an available worker with matching district and profession
                findMatchingWorker(district, profession, jobId);
            } else {
                System.out.println("Job not found!");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Failed to retrieve job: " + e.getMessage());
        });
    }

    private void findMatchingWorker(String district, String profession, String jobId) {
        CollectionReference workersRef = db.collection("workers");

        Query query = workersRef
                .whereEqualTo("district", district)
                .whereEqualTo("profession", profession)
                .whereEqualTo("isAvailable", true)
                .limit(1); // Limit to one worker

        query.get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                // Get the first available worker
                QueryDocumentSnapshot workerSnapshot = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                String workerId = workerSnapshot.getId();

                // Update job and worker status in Firestore
                assignJobToWorker(jobId, workerId);
            } else {
                System.out.println("No matching worker found.");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Failed to retrieve workers: " + e.getMessage());
        });
    }

    private void assignJobToWorker(String jobId, String workerId) {
        WriteBatch batch = db.batch();

        // Update job to set assigned worker ID
        DocumentReference jobRef = db.collection("jobs").document(jobId);
        batch.update(jobRef, "assignedWorkerId", workerId);
        batch.update(jobRef, "status", "Assigned");

        // Update worker availability status
        DocumentReference workerRef = db.collection("workers").document(workerId);
        batch.update(workerRef, "isAvailable", false);

        // Commit the batch update
        batch.commit().addOnSuccessListener(aVoid -> {
            System.out.println("Worker assigned successfully.");
        }).addOnFailureListener(e -> {
            System.out.println("Failed to assign worker: " + e.getMessage());
        });
    }
}

