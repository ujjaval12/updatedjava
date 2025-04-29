package Servlet;

import conn.DBconnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Session import needed
import javax.servlet.http.Part;

@WebServlet("/SendEventRequestServlet")
@MultipartConfig
public class SendEventRequestServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SendEventRequestServlet.class.getName());
    private static final String UPLOAD_DIR = "C:/petfesthub_uploads"; // CHANGE IF NEEDED

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 1. Authorization Check (remains the same)
        if (session == null || session.getAttribute("userId") == null) {
             response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+to+request+an+event.");
             return;
        }
        // **** NEW: Get user ID from session ****
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) { // Should not happen if first check passed, but safety check
             LOGGER.severe("User ID not found in session after login check!");
             response.sendRedirect(request.getContextPath() + "/login.jsp?error=Session+error.+Please+login+again.");
             return;
        }
        // **** END NEW ****


        request.setCharacterEncoding("UTF-8");
        String eventName = request.getParameter("eventName");
        String location = request.getParameter("location");
        String dateStr = request.getParameter("date");
        String description = request.getParameter("description");
        Part filePart = request.getPart("eventImage");

        String redirectPage = "create-event.jsp";
        String finalUploadedFilename = null;

        // --- Text Field Validation (remains same) ---
        // ...

        // --- File Upload Processing (remains same) ---
        // ...

        // --- Date Parsing (remains same) ---
         java.sql.Date sqlDate = null;
         // ...


        // --- Database Insertion ---
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBconnection.getConnection();
            if (conn == null) { /* ... handle error ... */ return;}

             // **** MODIFIED SQL: Add requester_user_id column ****
             String sql = "INSERT INTO event_requests " +
                          "(event_name, location, requested_date, description, status, image_filename, requester_user_id) " + // Added column
                          "VALUES (?, ?, ?, ?, ?, ?, ?)"; // Added placeholder

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, eventName);
            pstmt.setString(2, location);
            pstmt.setDate(3, sqlDate);
            pstmt.setString(4, description);
            pstmt.setString(5, "PENDING");
            pstmt.setString(6, finalUploadedFilename);
            // **** NEW: Set user ID parameter ****
            pstmt.setInt(7, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                 LOGGER.info("New event request submitted by User ID " + userId + ": " + eventName);
                 response.sendRedirect(redirectPage + "?message=Event+request+sent+successfully%21+An+admin+will+review+it.");
             } else { /* ... handle error ... */ }

        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "SQL Error saving event request for user ID " + userId, e);
             response.sendRedirect(redirectPage + "?error=Database+error+during+request+submission.");
         } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { /* Log */ }
        }
        // --- End Database Insertion ---
    }
}