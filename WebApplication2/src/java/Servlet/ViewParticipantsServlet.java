package Servlet;

import conn.DBconnection;
import model.Event; // To hold event details
import model.ParticipantInfo; // To hold participant details

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ViewParticipantsServlet") // Matches link from admin dashboard
public class ViewParticipantsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ViewParticipantsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 1. Authorization Check: Only Admin
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?adminError=Authentication+required.");
            return;
        }

        // 2. Get Event ID from request parameter
        String eventIdStr = request.getParameter("eventId");
        int eventId;
        if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
            request.setAttribute("error", "No Event ID specified.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_dashboard.jsp"); // Go back
            dispatcher.forward(request, response);
            return;
        }
        try {
            eventId = Integer.parseInt(eventIdStr);
        } catch (NumberFormatException e) {
             request.setAttribute("error", "Invalid Event ID format.");
             RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_dashboard.jsp"); // Go back
             dispatcher.forward(request, response);
            return;
        }

        // 3. Fetch Event Details and Participant List from DB
        Event eventDetails = null;
        List<ParticipantInfo> participants = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmtEvent = null;
        ResultSet rsEvent = null;
        PreparedStatement pstmtParticipants = null;
        ResultSet rsParticipants = null;
        String dbError = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                dbError = "Database connection failed.";
                LOGGER.severe("DB connection failed for ViewParticipantsServlet.");
            } else {
                // Fetch event details
                String sqlEvent = "SELECT event_id, title, event_date, location FROM events WHERE event_id = ?";
                pstmtEvent = conn.prepareStatement(sqlEvent);
                pstmtEvent.setInt(1, eventId);
                rsEvent = pstmtEvent.executeQuery();

                if (rsEvent.next()) {
                    eventDetails = new Event();
                    eventDetails.setEventId(rsEvent.getInt("event_id"));
                    eventDetails.setTitle(rsEvent.getString("title"));
                    eventDetails.setEventDate(rsEvent.getDate("event_date"));
                    eventDetails.setLocation(rsEvent.getString("location"));

                    // Fetch participants for this event
                    // Join event_participants with user table to get user details
                    String sqlParticipants = "SELECT ep.registration_id, ep.registration_time, " +
                                             "u.id, u.name, u.email " +
                                             "FROM event_participants ep " +
                                             "JOIN user u ON ep.user_id = u.id " +
                                             "WHERE ep.event_id = ? " +
                                             "ORDER BY ep.registration_time ASC"; // Order by registration time

                    pstmtParticipants = conn.prepareStatement(sqlParticipants);
                    pstmtParticipants.setInt(1, eventId);
                    rsParticipants = pstmtParticipants.executeQuery();

                    while (rsParticipants.next()) {
                        ParticipantInfo participant = new ParticipantInfo();
                        participant.setRegistrationId(rsParticipants.getInt("registration_id"));
                        participant.setRegistrationTime(rsParticipants.getTimestamp("registration_time"));
                        participant.setUserId(rsParticipants.getInt("id"));
                        participant.setUserName(rsParticipants.getString("name"));
                        participant.setUserEmail(rsParticipants.getString("email"));
                        participants.add(participant);
                    }
                    LOGGER.info("Fetched " + participants.size() + " participants for event ID " + eventId);

                } else {
                    // Event with the given ID was not found
                    dbError = "Event with ID " + eventId + " not found.";
                    LOGGER.warning("Event not found for participation view: ID " + eventId);
                }
            }

        } catch (SQLException e) {
            dbError = "Error fetching participant data from database.";
            LOGGER.log(Level.SEVERE, "SQL error fetching participants for event ID " + eventId, e);
        } finally {
            // Close all resources carefully
            try { if (rsParticipants != null) rsParticipants.close(); } catch (SQLException e) { /* Log */ }
            try { if (pstmtParticipants != null) pstmtParticipants.close(); } catch (SQLException e) { /* Log */ }
            try { if (rsEvent != null) rsEvent.close(); } catch (SQLException e) { /* Log */ }
            try { if (pstmtEvent != null) pstmtEvent.close(); } catch (SQLException e) { /* Log */ }
            // Do not close shared connection
        }

        // 4. Set attributes for the JSP
        if (dbError != null) {
            request.setAttribute("error", dbError); // General error message attribute
        }
        request.setAttribute("eventDetails", eventDetails); // Event object (or null if not found)
        request.setAttribute("participants", participants); // List of ParticipantInfo objects

        // 5. Forward to the new JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view_participants.jsp"); // Forward to the new JSP
        dispatcher.forward(request, response);
    }
}