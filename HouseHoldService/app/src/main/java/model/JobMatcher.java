package model;

import android.os.Build;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import java.util.*;

public class JobMatcher {
    private FirebaseFirestore db;
    private static final double MAX_DISTANCE_KM = 15.0;

    public JobMatcher() {
        db = FirebaseFirestore.getInstance();
    }

    public void matchJobToWorker(String jobId) {
        DocumentReference jobRef = db.collection("Service").document(jobId);
        jobRef.get().addOnSuccessListener(jobSnapshot -> {
            if (jobSnapshot.exists()) {
                String requiredSkill = jobSnapshot.getString("profession");
                GeoPoint jobLocation = jobSnapshot.getGeoPoint("location");

                if (requiredSkill != null && jobLocation != null) {
                    findAvailableWorkers(requiredSkill, jobLocation, jobId);
                }
            }
        }).addOnFailureListener(e -> Log.e("JobMatcher", "Error fetching job details", e));
    }

    public void findMatchingJobs(String workerId) {
        DocumentReference workerRef = db.collection("workerId").document(workerId);
        workerRef.get().addOnSuccessListener(workerSnapshot -> {
            if (workerSnapshot.exists()) {
                GeoPoint workerLocation = workerSnapshot.getGeoPoint("location");
                List<String> workerSkills = (List<String>) workerSnapshot.get("profession");

                if (workerLocation != null && workerSkills != null) {
                    findUnassignedJobs(workerLocation, workerSkills, workerId);
                }
            }
        }).addOnFailureListener(e -> Log.e("JobMatcher", "Error fetching worker details", e));
    }

    private void findAvailableWorkers(String skill, GeoPoint jobLocation, String jobId) {
        db.collection("workerId")
                .whereArrayContains("profession", skill)
                .whereEqualTo("jobId", null) // Only available workers
                .get()
                .addOnSuccessListener(workerSnapshots -> {
                    List<Map<String, Object>> suitableWorkers = new ArrayList<>();

                    for (DocumentSnapshot workerSnapshot : workerSnapshots) {
                        GeoPoint workerLocation = workerSnapshot.getGeoPoint("location");
                        if (workerLocation != null) {
                            double distance = calculateDistance(jobLocation, workerLocation);
                            if (distance <= MAX_DISTANCE_KM) {
                                Map<String, Object> workerData = new HashMap<>();
                                workerData.put("id", workerSnapshot.getId());
                                workerData.put("distance", distance);
                                suitableWorkers.add(workerData);
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        suitableWorkers.sort(Comparator.comparingDouble(w -> (double) w.get("distance")));
                    }

                    sendJobNotificationToBestWorker(suitableWorkers, jobId);
                })
                .addOnFailureListener(e -> Log.e("JobMatcher", "Error fetching workers", e));
    }

    private void findUnassignedJobs(GeoPoint workerLocation, List<String> skills, String workerId) {
        db.collection("Service")
                .whereEqualTo("workerId", "") // Only unassigned jobs
                .get()
                .addOnSuccessListener(jobSnapshots -> {
                    for (DocumentSnapshot jobSnapshot : jobSnapshots) {
                        String requiredSkill = jobSnapshot.getString("profession");
                        GeoPoint jobLocation = jobSnapshot.getGeoPoint("location");

                        if (jobLocation != null && requiredSkill != null && skills.contains(requiredSkill)) {
                            double distance = calculateDistance(jobLocation, workerLocation);
                            if (distance <= MAX_DISTANCE_KM) {
                                sendJobNotificationToWorker(jobSnapshot.getId(), workerId, requiredSkill, jobLocation, distance);
                            }
                        }
                    }
                });
    }

    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();
        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat /2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void sendJobNotificationToBestWorker(List<Map<String, Object>> workers, String jobId) {
        if (!workers.isEmpty()) {
            String bestWorkerId = (String) workers.get(0).get("id");
            Log.d("DEBUG", "Best worker selected: " + bestWorkerId);

            db.collection("Service").document(jobId).get().addOnSuccessListener(jobSnapshot -> {
                if (jobSnapshot.exists()) {
                    String jobTitle = jobSnapshot.getString("issue");
                    String clientId = jobSnapshot.getString("userId");
                    String profession = jobSnapshot.getString("profession");
                    GeoPoint jobLocation = jobSnapshot.getGeoPoint("location");

                    if (jobTitle != null && clientId != null && profession != null && jobLocation != null) {
                        double distance = (double) workers.get(0).get("distance");

                        Log.d("DEBUG", "Creating job notification for worker: " + bestWorkerId);

                        Map<String, Object> notificationData = new HashMap<>();
                        notificationData.put("jobId", jobId);
                        notificationData.put("workerId", bestWorkerId);
                        notificationData.put("userId", clientId);
                        notificationData.put("jobTitle", jobTitle);
                        notificationData.put("location", jobLocation);
                        notificationData.put("distance", distance);
                        notificationData.put("profession", profession);
                        notificationData.put("status", "pending");
                        notificationData.put("timestamp", Timestamp.now());

                        db.collection("JobNotifications").add(notificationData)
                                .addOnSuccessListener(docRef -> Log.d("DEBUG", "Notification sent to worker: " + bestWorkerId))
                                .addOnFailureListener(e -> Log.e("ERROR", "Error sending notification", e));
                    } else {
                        Log.e("ERROR", "Missing job details in Firestore for job ID: " + jobId);
                    }
                } else {
                    Log.e("ERROR", "Job not found in Firestore: " + jobId);
                }
            }).addOnFailureListener(e -> Log.e("ERROR", "Error fetching job details", e));
        } else {
            Log.d("DEBUG", "No suitable worker found within 15km.");
        }
    }



    private void sendJobNotificationToWorker(String jobId, String workerId, String profession, GeoPoint jobLocation, double distance) {
        db.collection("Service").document(jobId).get().addOnSuccessListener(jobSnapshot -> {
            if (jobSnapshot.exists()) {
                String jobTitle = jobSnapshot.getString("issue");
                String clientId = jobSnapshot.getString("userId");

                // Create notification data
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("jobId", jobId);
                notificationData.put("workerId", workerId);
                notificationData.put("userId", clientId);
                notificationData.put("jobTitle", jobTitle);
                notificationData.put("location", jobLocation);
                notificationData.put("distance", distance);
                notificationData.put("profession", profession);
                notificationData.put("status", "pending"); // Worker needs to accept or decline
                notificationData.put("timestamp", Timestamp.now());

                // Store job notification in Firestore
                db.collection("JobNotifications")
                        .add(notificationData)
                        .addOnSuccessListener(documentReference ->
                                Log.d("WorkerListener", "Job notification sent to worker: " + workerId))
                        .addOnFailureListener(e ->
                                Log.e("WorkerListener", "Error sending job notification", e));
            }
        });
    }
}

