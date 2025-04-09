package model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobMatcher {
    private FirebaseFirestore db;

    public JobMatcher() {
        db = FirebaseFirestore.getInstance();
    }

    // Match a single job to all available workers
    public void matchSingleJob(String jobId) {
        db.collection("Service").document(jobId).get().addOnSuccessListener(jobDoc -> {
            if (jobDoc.exists()) {
                GeoPoint jobLocation = jobDoc.getGeoPoint("location");
                String requiredSkill = jobDoc.getString("service");

                db.collection("workerId").get().addOnSuccessListener(workerSnapshot -> {
                    for (DocumentSnapshot workerDoc : workerSnapshot.getDocuments()) {
                        GeoPoint workerLocation = workerDoc.getGeoPoint("location");
                        String skills = (String) workerDoc.get("profession");
                        List<String> declinedJobs = (List<String>) workerDoc.get("declinedJobs");

                        // Skip if job was previously declined
                        if (declinedJobs != null && declinedJobs.contains(jobId)) {
                            continue;
                        }

                        if (workerLocation != null && skills != null && skills.equals(requiredSkill)) {
                            double distance = GeoUtils.distanceInKm(
                                    jobLocation.getLatitude(), jobLocation.getLongitude(),
                                    workerLocation.getLatitude(), workerLocation.getLongitude());

                            if (distance <= 10.0) {
                                saveMatch(jobId, workerDoc.getId(), distance);
                            }
                        }
                    }
                });
            }
        });
    }

    // Match all open jobs to a specific worker
    public void matchJobsToWorker(String workerId) {
        db.collection("workerId").document(workerId).get().addOnSuccessListener(workerDoc -> {
            if (workerDoc.exists()) {
                GeoPoint workerLocation = workerDoc.getGeoPoint("location");
                String skills = (String) workerDoc.get("profession");
                List<String> declinedJobs = (List<String>) workerDoc.get("declinedJobs");

                db.collection("Service").whereEqualTo("status","pending")
                        .get().addOnSuccessListener(jobSnapshot -> {
                    for (DocumentSnapshot jobDoc : jobSnapshot.getDocuments()) {
                        GeoPoint jobLocation = jobDoc.getGeoPoint("location");
                        String requiredSkill = jobDoc.getString("service");
                        String jobId = jobDoc.getId();

                        // Skip if job was previously declined
                        if (declinedJobs != null && declinedJobs.contains(jobId)) {
                            continue;
                        }

                        if (workerLocation != null && skills != null && skills.equals(requiredSkill)) {
                            double distance = GeoUtils.distanceInKm(
                                    jobLocation.getLatitude(), jobLocation.getLongitude(),
                                    workerLocation.getLatitude(), workerLocation.getLongitude());

                            if (distance <= 10.0) {
                                saveMatch(jobId, workerId, distance);
                            }
                        }
                    }
                });
            }
        });
    }

    private void saveMatch(String jobId, String workerId, double distance) {
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("jobId", jobId);
        matchData.put("workerId", workerId);
        matchData.put("matchedAt", FieldValue.serverTimestamp());
        matchData.put("status", "pending");
        matchData.put("distance", distance);

        db.collection("MatchedJobs")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        db.collection("MatchedJobs").add(matchData);
                    }
                });
    }
}
