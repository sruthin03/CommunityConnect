package model;

public class JobRequest {
    private String id;
    private String jobTitle;
    private String clientName;

    public JobRequest() {}

    public JobRequest(String id, String jobTitle, String clientName) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.clientName = clientName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
}
