package model;

import java.util.Date;

public class EventRequest {
    private int requestId;
    private String eventName;
    private String location;
    private Date requestedDate;
    private String description;
    private String imageFilename; // Added
    private String status;
    private Date requestedAt;

    // --- Getters and Setters ---
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Date getRequestedDate() { return requestedDate; }
    public void setRequestedDate(Date requestedDate) { this.requestedDate = requestedDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageFilename() { return imageFilename; } // Added
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; } // Added
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Date requestedAt) { this.requestedAt = requestedAt; }
}