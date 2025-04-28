package model;

import java.util.Date;

public class Event {
    private int eventId;
    private String title;
    private Date eventDate;
    private String location;
    private String description;
    private String imageFilename; // Renamed
    private Date createdAt;

    public Event() { }

    // --- Getters and Setters ---
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageFilename() { return imageFilename; } // Renamed
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; } // Renamed
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Event [eventId=" + eventId + ", title=" + title + ", eventDate=" + eventDate + ", location=" + location + "]";
    }
}