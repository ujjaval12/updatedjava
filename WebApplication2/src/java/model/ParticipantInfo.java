package model;

import java.util.Date; // For registration time

public class ParticipantInfo {
    // User Details
    private int userId;
    private String userName;
    private String userEmail;

    // Participation Details
    private int registrationId;
    private Date registrationTime;

    public ParticipantInfo() {}

    // --- Getters and Setters ---
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public Date getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(Date registrationTime) { this.registrationTime = registrationTime; }
}