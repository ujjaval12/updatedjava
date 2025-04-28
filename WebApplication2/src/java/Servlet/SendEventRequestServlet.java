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
import javax.servlet.http.Part;

@WebServlet("/SendEventRequestServlet")
@MultipartConfig // Enable file uploads
public class SendEventRequestServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SendEventRequestServlet.class.getName());

    // !!! --- IMPORTANT: SET THIS PATH --- !!!
    // Directory where uploaded images will be stored. MUST exist. GlassFish needs WRITE permission.
    private static final String UPLOAD_DIR = "C:/petfesthub_uploads"; // Example: Windows
    // private static final String UPLOAD_DIR = "/var/www/petfesthub_uploads"; // Example: Linux

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure character encoding is set for request parameters
        request.setCharacterEncoding("UTF-8");

        String eventName = request.getParameter("eventName");
        String location = request.getParameter("location");
        String dateStr = request.getParameter("date");
        String description = request.getParameter("description");
        Part filePart = request.getPart("eventImage"); // Get the uploaded file part

        String redirectPage = "create-event.jsp";
        String finalUploadedFilename = null; // Store the name of the file saved on disk

        // --- Text Field Validation ---
        if (eventName == null || eventName.trim().isEmpty() ||
            location == null || location.trim().isEmpty() ||
            dateStr == null || dateStr.trim().isEmpty() ||
            description == null || description.trim().isEmpty()) {
            response.sendRedirect(redirectPage + "?error=Please+fill+in+all+required+text+fields.");
            return;
        }

        // --- File Upload Processing ---
        if (filePart != null && filePart.getSize() > 0) {
            String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            if (originalFileName != null && !originalFileName.trim().isEmpty()) {
                // Validate file extension
                String extension = "";
                int i = originalFileName.lastIndexOf('.');
                if (i > 0) { extension = originalFileName.substring(i + 1).toLowerCase(); }

                if (!extension.matches("jpg|jpeg|png|gif")) {
                    LOGGER.warning("Invalid file type upload attempt: " + originalFileName);
                    response.sendRedirect(redirectPage + "?error=Invalid+image+file+type.+Please+upload+JPG%2C+PNG%2C+or+GIF.");
                    return;
                }

                // Create unique filename
                String uniqueID = UUID.randomUUID().toString();
                // Sanitize filename (replace non-alphanumeric except ., - with _)
                String sanitizedOriginal = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                finalUploadedFilename = uniqueID + "_" + sanitizedOriginal;

                // Ensure upload directory exists
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    if (!uploadDir.mkdirs()) {
                        LOGGER.severe("CRITICAL: FAILED TO CREATE UPLOAD DIRECTORY: " + UPLOAD_DIR);
                        response.sendRedirect(redirectPage + "?error=Server+error%3A+Cannot+create+storage+directory.");
                        return; // Stop if we cannot save files
                    } else {
                        LOGGER.info("Created upload directory: " + UPLOAD_DIR);
                    }
                }

                // Save the file
                File uploadedFile = new File(uploadDir, finalUploadedFilename);
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("File uploaded and saved successfully: " + uploadedFile.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error saving uploaded file: " + finalUploadedFilename, e);
                    // Don't save filename to DB if save failed
                    finalUploadedFilename = null;
                    response.sendRedirect(redirectPage + "?error=Error+saving+uploaded+file.");
                    return;
                }
            }
        } else {
            LOGGER.info("No image file provided for event request: " + eventName);
        }
        // --- End File Upload Processing ---

        // --- Date Parsing ---
        java.sql.Date sqlDate = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false); // Disallow invalid dates like 2023-02-30
            java.util.Date parsedDate = format.parse(dateStr);
            sqlDate = new java.sql.Date(parsedDate.getTime());
        } catch (ParseException e) {
            LOGGER.warning("Invalid date format received: " + dateStr);
            response.sendRedirect(redirectPage + "?error=Invalid+date+format.+Please+use+YYYY-MM-DD.");
            return;
        }
        // --- End Date Parsing ---

        // --- Database Insertion ---
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                LOGGER.severe("DB connection failed for event request.");
                response.sendRedirect(redirectPage + "?error=Server+error.+Could+not+save+request.");
                return;
            }

            String sql = "INSERT INTO event_requests (event_name, location, requested_date, description, status, image_filename) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, eventName);
            pstmt.setString(2, location);
            pstmt.setDate(3, sqlDate);
            pstmt.setString(4, description);
            pstmt.setString(5, "PENDING");
            pstmt.setString(6, finalUploadedFilename); // Store unique filename or null

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Event request saved to DB: " + eventName + (finalUploadedFilename != null ? " with image " + finalUploadedFilename : ""));
                response.sendRedirect(redirectPage + "?message=Event+request+sent+successfully%21+An+admin+will+review+it.");
            } else {
                LOGGER.warning("Event request DB insertion failed for: " + eventName);
                response.sendRedirect(redirectPage + "?error=Failed+to+save+request+to+database.+Please+try+again.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error saving event request: " + eventName, e);
            response.sendRedirect(redirectPage + "?error=Database+error+during+request+submission.");
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e); }
            // Do not close shared connection
        }
        // --- End Database Insertion ---
    }
}