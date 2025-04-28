package Servlet;

import conn.DBconnection;
import java.io.IOException;
import java.sql.*; // Uses Date
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ApproveEventServlet")
public class ApproveEventServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ApproveEventServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Authorization Check
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
            response.sendRedirect("login.jsp?adminError=Authentication+required.");
            return;
        }

        String requestIdStr = request.getParameter("requestId");
        String redirectPage = "AdminDashboardServlet"; // Redirect back to dashboard

        int requestId;
        try {
            requestId = Integer.parseInt(requestIdStr);
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.warning("Invalid request ID for approval: " + requestIdStr);
            response.sendRedirect(redirectPage + "?error=Invalid+request+ID.");
            return;
        }

        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                LOGGER.severe("DB connection failed for event approval.");
                response.sendRedirect(redirectPage + "?error=Database+connection+failed.");
                return;
            }

            conn.setAutoCommit(false); // Start transaction

            // 1. Fetch request details including image filename
            String selectSql = "SELECT event_name, location, requested_date, description, image_filename " +
                               "FROM event_requests WHERE request_id = ? AND status = ?";
            selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, requestId);
            selectStmt.setString(2, "PENDING");
            rs = selectStmt.executeQuery();

            if (rs.next()) {
                String eventName = rs.getString("event_name");
                String location = rs.getString("location");
                Date eventDate = rs.getDate("requested_date");
                String description = rs.getString("description");
                String imageFilename = rs.getString("image_filename"); // Get filename

                // 2. Insert into events table including image filename
                String insertSql = "INSERT INTO events (title, event_date, location, description, image_filename) VALUES (?, ?, ?, ?, ?)";
                insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, eventName);
                insertStmt.setDate(2, eventDate);
                insertStmt.setString(3, location);
                insertStmt.setString(4, description);
                insertStmt.setString(5, imageFilename); // Store filename

                int insertRows = insertStmt.executeUpdate();

                if (insertRows > 0) {
                    // 3. Update request status
                    String updateSql = "UPDATE event_requests SET status = ? WHERE request_id = ?";
                    updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, "APPROVED");
                    updateStmt.setInt(2, requestId);
                    int updateRows = updateStmt.executeUpdate();

                    if (updateRows > 0) {
                        conn.commit(); // Commit successful transaction
                        LOGGER.info("Event request ID " + requestId + " approved. Event created: " + eventName);
                        response.sendRedirect(redirectPage + "?message=Event+approved+successfully!");
                    } else {
                        conn.rollback();
                        LOGGER.severe("DB ERROR: Failed to update request status after inserting event for request ID: " + requestId);
                        response.sendRedirect(redirectPage + "?error=Failed+to+update+request+status.");
                    }
                } else {
                     conn.rollback();
                     LOGGER.severe("DB ERROR: Failed to insert event into events table for request ID: " + requestId);
                     response.sendRedirect(redirectPage + "?error=Failed+to+create+event+from+request.");
                }
            } else {
                 conn.rollback(); // Request not found or not pending
                 LOGGER.warning("Approve attempt on non-existent/processed request ID: " + requestId);
                 response.sendRedirect(redirectPage + "?error=Request+not+found+or+already+processed.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during event approval for request ID: " + requestId, e);
             try { if (conn != null) conn.rollback(); } // Attempt rollback on SQL error
             catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Failed to rollback transaction", ex); }
            response.sendRedirect(redirectPage + "?error=Database+error+during+approval.");
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing ResultSet", e); }
            try { if (selectStmt != null) selectStmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing selectStmt", e); }
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing insertStmt", e); }
            try { if (updateStmt != null) updateStmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing updateStmt", e); }
            // Reset auto-commit but don't close shared connection
            try { if (conn != null) conn.setAutoCommit(true); }
            catch (SQLException e) { LOGGER.log(Level.SEVERE, "Failed to reset auto-commit", e); }
        }
    }
}