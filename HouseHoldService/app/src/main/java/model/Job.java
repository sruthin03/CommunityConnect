package model;

public class Job {
    private String id;
    private String workerId;
    private String status;
    private String jobId;
    private boolean bb;

    // Required no-arg constructor for Firestore
    public Job() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getjobId() {
        return jobId;
    }

    public void setjobId(String jobId) {
        this.jobId = jobId;
    }

    public void setCurrent(boolean bb) {
        this.bb = bb;
    }
}
