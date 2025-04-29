package Servlet;

import conn.DBconnection;
import model.EventRequest;
import model.Event;

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

        HttpSession session = request.getSession(false);

        // Authorization Check (remains same)
        if (session == null || session.getAttribute("isAdmin") == null || !((Boolean) session.getAttribute("isAdmin"))) {
             response.sendRedirect("login.jsp?adminError=Please login as admin.");
             return;
        }
        LOGGER.info("Admin dashboard accessed by: " + session.getAttribute("adminEmail"));

        List<EventRequest> pendingRequests = new ArrayList<>();
        List<Event> approvedEvents = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmtRequests = null;
        ResultSet rsRequests = null;
        PreparedStatement pstmtEvents = null;
        ResultSet rsEvents = null;
        String dbError = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                dbError = "Database connection failed.";
                LOGGER.severe("Failed to get DB connection for admin dashboard.");
            } else {
                // --- Fetch PENDING requests with Requester Info ---
                 // **** MODIFIED SQL: LEFT JOIN with user table ****
                 String sqlRequests = "SELECT req.request_id, req.event_name, req.location, req.requested_date, " +
                                      "req.description, req.requested_at, req.image_filename, " +
                                      "usr.id AS user_id, usr.name AS user_name, usr.email AS user_email " + // Get user details
                                      "FROM event_requests req " +
                                      "LEFT JOIN user usr ON req.requester_user_id = usr.id " + // LEFT JOIN here
                                      "WHERE req.status = ? ORDER BY req.requested_at ASC";
                pstmtRequests = conn.prepareStatement(sqlRequests);
                pstmtRequests.setString(1, "PENDING");
                rsRequests = pstmtRequests.executeQuery();
                while (rsRequests.next()) {
                    EventRequest req = new EventRequest();
                    req.setRequestId(rsRequests.getInt("request_id"));
                    req.setEventName(rsRequests.getString("event_name"));
                    req.setLocation(rsRequests.getString("location"));
                    req.setRequestedDate(rsRequests.getDate("requested_date"));
                    req.setDescription(rsRequests.getString("description"));
                    req.setRequestedAt(rsRequests.getTimestamp("requested_at"));
                    req.setImageFilename(rsRequests.getString("image_filename"));
                    req.setStatus("PENDING");
                    // **** NEW: Populate requester info (handle potential NULLs from LEFT JOIN) ****
                    req.setRequesterUserId(rsRequests.getInt("user_id")); // Might be 0 if user was deleted and FK is NULL
                    req.setRequesterName(rsRequests.getString("user_name")); // Might be null
                    req.setRequesterEmail(rsRequests.getString("user_email")); // Might be null
                    // **** END NEW ****
                    pendingRequests.add(req);
                }
                LOGGER.info("Fetched " + pendingRequests.size() + " pending event requests with user info.");
                rsRequests.close();
                pstmtRequests.close();
                // --- End Fetch PENDING requests ---


                // --- Fetch APPROVED events (remains same) ---
                // ... (existing code to fetch approved events) ...
                LOGGER.info("Fetching approved events for dashboard...");
                 String sqlEvents = "SELECT event_id, title, event_date, location FROM events ORDER BY event_date DESC";
                 pstmtEvents = conn.prepareStatement(sqlEvents);
                 rsEvents = pstmtEvents.executeQuery();
                 while(rsEvents.next()){
                     Event event = new Event();
                     event.setEventId(rsEvents.getInt("event_id"));
                     event.setTitle(rsEvents.getString("title"));
                     event.setEventDate(rsEvents.getDate("event_date"));
                     event.setLocation(rsEvents.getString("location"));
                     approvedEvents.add(event);
                 }
                 LOGGER.info("Fetched " + approvedEvents.size() + " approved events.");
                 rsEvents.close();
                 pstmtEvents.close();

            }

        } catch (SQLException e) {
            dbError = "Error fetching data for admin dashboard.";
            LOGGER.log(Level.SEVERE, "SQL error fetching admin dashboard data", e);
        } catch (Exception e) {
             dbError = "An unexpected error occurred while fetching dashboard data.";
            LOGGER.log(Level.SEVERE, "Unexpected error fetching admin dashboard data", e);
        } finally {
            // Close all resources carefully
            try { if (rsRequests != null && !rsRequests.isClosed()) rsRequests.close(); } catch (SQLException e) { /* Log */ }
            try { if (pstmtRequests != null && !pstmtRequests.isClosed()) pstmtRequests.close(); } catch (SQLException e) { /* Log */ }
            try { if (rsEvents != null && !rsEvents.isClosed()) rsEvents.close(); } catch (SQLException e) { /* Log */ }
            try { if (pstmtEvents != null && !pstmtEvents.isClosed()) pstmtEvents.close(); } catch (SQLException e) { /* Log */ }
            // Not closing shared connection
        }

        // Set attributes for the JSP
         if (dbError != null) { request.setAttribute("dashboardError", dbError); }
        request.setAttribute("pendingRequests", pendingRequests);
        request.setAttribute("approvedEvents", approvedEvents);

        // Forward to the admin JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_dashboard.jsp");
        dispatcher.forward(request, response);
    }

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         doGet(request, response);
     }
}