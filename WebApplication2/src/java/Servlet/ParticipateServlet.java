package Servlet;

import conn.DBconnection; // Your DB connection class

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/ParticipateServlet") // Matches links in JSPs
public class ParticipateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ParticipateServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 1. Check if user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            LOGGER.warning("Attempt to participate without login.");
             // Redirect to login, maybe pass original request target later
             response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+to+participate+in+events.");
            return;
        }

        // 2. Get User ID and Event ID
        Integer userId = (Integer) session.getAttribute("userId");
        String eventIdStr = request.getParameter("eventId");
        int eventId;

        if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/ViewEventsServlet?error=Invalid+event+ID."); // Go back to events list
            return;
        }

        try {
            eventId = Integer.parseInt(eventIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/ViewEventsServlet?error=Invalid+event+ID+format."); // Go back to events list
            return;
        }

        // 3. Attempt to register participation in DB
        Connection conn = null;
        PreparedStatement pstmt = null;
        String successMessage = null;
        String errorMessage = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                errorMessage = "Database+connection+failed.";
                LOGGER.severe("DB connection failed for participation.");
            } else {
                 // Use INSERT IGNORE or check first to handle duplicate entries gracefully
                 // due to the UNIQUE constraint (event_id, user_id)
                 // INSERT IGNORE is simpler for this case
                 String sql = "INSERT IGNORE INTO event_participants (event_id, user_id) VALUES (?, ?)";
                 pstmt = conn.prepareStatement(sql);
                 pstmt.setInt(1, eventId);
                 pstmt.setInt(2, userId);

                 int rowsAffected = pstmt.executeUpdate();

                 if (rowsAffected > 0) {
                     successMessage = "Successfully+registered+for+the+event!";
                     LOGGER.info("User ID " + userId + " registered for Event ID " + eventId);
                 } else {
                     // If 0 rows affected with INSERT IGNORE, it means the record already existed
                     errorMessage = "You+are+already+registered+for+this+event.";
                     LOGGER.info("User ID " + userId + " attempted duplicate registration for Event ID " + eventId);
                 }
            }
        } catch (SQLException e) {
             errorMessage = "Database+error+during+registration.";
             LOGGER.log(Level.SEVERE, "SQL Error during participation registration for user " + userId + " event " + eventId, e);
         } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { /* Log */ }
             // Do not close shared connection
         }

        // 4. Redirect back to the events page (or maybe event details page later) with a message
        String redirectUrl = request.getContextPath() + "/ViewEventsServlet"; // Default back to list
        if (successMessage != null) {
             redirectUrl += "?message=" + successMessage;
        } else if (errorMessage != null) {
             redirectUrl += "?error=" + errorMessage;
        }
        response.sendRedirect(redirectUrl);
    }

     // Handle POST identically to GET for simple link clicks
     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}