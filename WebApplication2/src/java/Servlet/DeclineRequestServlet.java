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

@WebServlet("/DeclineRequestServlet") // Matches the new form action
public class DeclineRequestServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DeclineRequestServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 1. Authorization Check: Only Admin
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?adminError=Authentication+required.");
            return;
        }

        // 2. Get Request ID
        String requestIdStr = request.getParameter("requestId");
        String redirectPage = "AdminDashboardServlet"; // Redirect back to dashboard
        int requestId;

        if (requestIdStr == null || requestIdStr.trim().isEmpty()) {
             response.sendRedirect(redirectPage + "?error=Missing+request+ID+for+decline.");
             return;
        }
        try {
            requestId = Integer.parseInt(requestIdStr);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid request ID format for decline: " + requestIdStr);
            response.sendRedirect(redirectPage + "?error=Invalid+request+ID+format.");
            return;
        }

        // 3. Update Status in DB
        Connection conn = null;
        PreparedStatement updateStmt = null;
        String feedbackMessage = null;
        String feedbackType = "error"; // Default to error

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                feedbackMessage = "Database+connection+failed.";
                LOGGER.severe("DB connection failed for declining request ID: " + requestId);
            } else {
                // Update status to 'REJECTED' only if it's currently 'PENDING'
                String updateSql = "UPDATE event_requests SET status = ? WHERE request_id = ? AND status = ?";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, "REJECTED"); // Set new status
                updateStmt.setInt(2, requestId);
                updateStmt.setString(3, "PENDING");  // Condition to prevent accidental updates

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    feedbackMessage = "Event+request+ID+" + requestId + "+declined+successfully.";
                    feedbackType = "message"; // Use success message param
                    LOGGER.info("Event request ID " + requestId + " declined.");
                } else {
                    // Could mean request wasn't found or wasn't pending anymore
                    feedbackMessage = "Request+ID+" + requestId + "+not+found+or+already+processed.";
                    LOGGER.warning("Attempt to decline non-pending or non-existent request ID: " + requestId);
                }
            }
        } catch (SQLException e) {
            feedbackMessage = "Database+error+while+declining+request.";
            LOGGER.log(Level.SEVERE, "SQL Error declining event request ID: " + requestId, e);
        } finally {
            try { if (updateStmt != null) updateStmt.close(); } catch (SQLException e) { /* Log */ }
            // Do not close shared connection
        }

        // 4. Redirect back to dashboard with feedback
        if(feedbackType.equals("error")) {
             response.sendRedirect(redirectPage + "?error=" + feedbackMessage);
        } else {
             response.sendRedirect(redirectPage + "?message=" + feedbackMessage);
        }
    }

     // Optional: Handle GET request if needed, perhaps redirect to dashboard
     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("AdminDashboardServlet?error=Decline+action+must+be+POST.");
    }
}