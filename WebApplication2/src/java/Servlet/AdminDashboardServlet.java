package Servlet;

import conn.DBconnection;
import model.EventRequest; // Import the bean

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

@WebServlet("/AdminDashboardServlet")
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false); // Don't create if it doesn't exist

        // Authorization Check: Ensure user is admin
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
            LOGGER.warning("Unauthorized access attempt to admin dashboard.");
            response.sendRedirect("login.jsp?adminError=Please login as admin.");
            return;
        }

        LOGGER.info("Admin dashboard accessed by: " + session.getAttribute("adminEmail"));

        List<EventRequest> pendingRequests = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String dbError = null; // Variable for error message

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                dbError = "Database connection failed.";
                LOGGER.severe("Failed to get DB connection for admin dashboard.");
            } else {
                // **** MODIFIED SQL: Select image_filename ****
                String sql = "SELECT request_id, event_name, location, requested_date, description, requested_at, image_filename " + // Select the filename
                             "FROM event_requests WHERE status = ? ORDER BY requested_at ASC";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "PENDING");
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    EventRequest req = new EventRequest();
                    req.setRequestId(rs.getInt("request_id"));
                    req.setEventName(rs.getString("event_name"));
                    req.setLocation(rs.getString("location"));
                    req.setRequestedDate(rs.getDate("requested_date"));
                    req.setDescription(rs.getString("description"));
                    req.setRequestedAt(rs.getTimestamp("requested_at"));
                    // **** ADDED: Get and set image filename from ResultSet ****
                    req.setImageFilename(rs.getString("image_filename"));
                    req.setStatus("PENDING"); // Explicitly set status for clarity if needed later
                    pendingRequests.add(req);
                }
                LOGGER.info("Fetched " + pendingRequests.size() + " pending event requests.");
            }

        } catch (SQLException e) {
            dbError = "Error fetching event requests.";
            LOGGER.log(Level.SEVERE, "SQL error fetching pending requests", e);
        } catch (Exception e) {
             dbError = "An unexpected error occurred while fetching requests.";
            LOGGER.log(Level.SEVERE, "Unexpected error fetching pending requests", e);
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e); }
            // Not closing shared connection
        }

        // Set attributes for the JSP
         if (dbError != null) {
            request.setAttribute("dashboardError", dbError); // Attribute name matches JSP
        }
        request.setAttribute("pendingRequests", pendingRequests); // Attribute name matches JSP

        // Forward to the admin JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_dashboard.jsp");
        dispatcher.forward(request, response);
    }

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         // Forward POST requests to GET for this dashboard view
         doGet(request, response);
     }
}