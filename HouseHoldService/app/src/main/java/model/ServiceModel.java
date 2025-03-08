package model;

import java.util.Date;

public class ServiceModel {
    private String serviceId;
    private String service;
    private String issue;
    private String status;
    private String imageUrl;
    private Date timestamp;

    public ServiceModel() {
        // Default constructor for Firestore
    }

    public ServiceModel(String serviceId, String service, String issue, String status, String imageUrl, Date timestamp) {
        this.serviceId = serviceId;
        this.service = service;
        this.issue = issue;
        this.status = status;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
