package model;

public class Review {
    private String id;
    private String jobId;
    private String userId;
    private String workerId;
    private int rating;
    private String comment;


    public Review(String id, String jobId, String userId, String workerId, int rating, String comment) {
        this.id = id;
        this.jobId = jobId;
        this.userId = userId;
        this.workerId = workerId;
        this.rating = rating;
        this.comment = comment;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}