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

@WebServlet("/DeleteEventServlet") // Matches the new form action
public class DeleteEventServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DeleteEventServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 1. Authorization Check: Only Admin
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?adminError=Authentication+required+to+delete+events.");
            return;
        }

        // 2. Get Event ID
        String eventIdStr = request.getParameter("eventId");
        String redirectPage = "AdminDashboardServlet"; // Redirect back to dashboard
        int eventId;

        if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
             response.sendRedirect(redirectPage + "?error=Missing+event+ID+for+deletion.");
             return;
        }
        try {
            eventId = Integer.parseInt(eventIdStr);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid event ID format for deletion: " + eventIdStr);
            response.sendRedirect(redirectPage + "?error=Invalid+event+ID+format.");
            return;
        }

        // 3. Delete Event from DB
        Connection conn = null;
        PreparedStatement deleteStmt = null;
        String feedbackMessage = null;
        String feedbackType = "error"; // Default to error

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                feedbackMessage = "Database+connection+failed.";
                LOGGER.severe("DB connection failed for deleting event ID: " + eventId);
            } else {
                // Delete the event. Associated participant records should be deleted automatically
                // if ON DELETE CASCADE was set correctly on the foreign key in event_participants table.
                String deleteSql = "DELETE FROM events WHERE event_id = ?";
                deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, eventId);

                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    feedbackMessage = "Event+ID+" + eventId + "+deleted+successfully.";
                    feedbackType = "message";
                    LOGGER.info("Event ID " + eventId + " deleted by admin.");
                    // ** TODO Optional: Delete associated image file from UPLOAD_DIR **
                    // This requires fetching the image_filename BEFORE deleting the row
                    // and then using java.io.File.delete() on the file in UPLOAD_DIR.
                    // Need to add error handling for file deletion.
                } else {
                    // Event might have already been deleted by someone else
                    feedbackMessage = "Event+ID+" + eventId + "+not+found+or+could+not+be+deleted.";
                    LOGGER.warning("Attempt to delete non-existent event ID: " + eventId);
                }
            }
        } catch (SQLException e) {
            feedbackMessage = "Database+error+while+deleting+event.";
            // Log FK constraint errors separately if needed
            LOGGER.log(Level.SEVERE, "SQL Error deleting event ID: " + eventId, e);
        } finally {
            try { if (deleteStmt != null) deleteStmt.close(); } catch (SQLException e) { /* Log */ }
            // Do not close shared connection
        }

        // 4. Redirect back to dashboard with feedback
        if(feedbackType.equals("error")) {
             response.sendRedirect(redirectPage + "?error=" + feedbackMessage);
        } else {
             response.sendRedirect(redirectPage + "?message=" + feedbackMessage);
        }
    }

     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect GET requests to dashboard, as deletion should be via POST
        response.sendRedirect("AdminDashboardServlet?error=Deletion+must+be+performed+via+POST.");
    }
}