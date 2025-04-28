package Servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/ImageServlet/*") // Handles URLs like /ImageServlet/image.jpg
public class ImageServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ImageServlet.class.getName());

    // !!! --- IMPORTANT: SET THIS PATH (Must match SendEventRequestServlet) --- !!!
    private static final String UPLOAD_DIR = "C:/petfesthub_uploads"; // Example: Windows
    // private static final String UPLOAD_DIR = "/var/www/petfesthub_uploads"; // Example: Linux

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestedFilename = request.getPathInfo(); // Gets the part after /ImageServlet/

        // Basic Validation
        if (requestedFilename == null || requestedFilename.isEmpty() || requestedFilename.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image filename not specified.");
            return;
        }

        // Remove leading slash
        if (requestedFilename.startsWith("/")) {
            requestedFilename = requestedFilename.substring(1);
        }

        // ** Security Check: Prevent path traversal **
        // Normalize path and ensure it stays within the upload directory
        Path requestedPath = Paths.get(UPLOAD_DIR, requestedFilename).normalize();
        Path uploadPath = Paths.get(UPLOAD_DIR).normalize();

        if (!requestedPath.startsWith(uploadPath) || requestedFilename.contains("..")) {
             LOGGER.warning("Path traversal attempt detected for image request: " + requestedFilename);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image path specified.");
             return;
        }

        File imageFile = requestedPath.toFile();

        // Check file existence and readability
        if (!imageFile.exists() || !imageFile.isFile() || !imageFile.canRead()) {
            LOGGER.warning("Requested image not found or not readable: " + requestedPath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image resource not found.");
            return;
        }

        // Determine content type (MIME)
        String contentType = getServletContext().getMimeType(imageFile.getName());
        if (contentType == null) {
            contentType = "application/octet-stream"; // Default binary type
        }
        response.setContentType(contentType);
        response.setContentLengthLong(imageFile.length());

        // Optional: Set cache headers
        // response.setHeader("Cache-Control", "public, max-age=86400"); // Cache for 1 day

        // Stream the file content
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(requestedPath, out);
            out.flush();
            LOGGER.fine("Successfully served image: " + requestedFilename);
        } catch (NoSuchFileException e) {
             LOGGER.warning("NoSuchFileException while streaming (file likely deleted after check): " + requestedPath);
             // Cannot send 404 here as response might be committed
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IOException during image streaming for: " + requestedFilename, e);
            // Client likely disconnected, cannot change response status
        }
    }
}