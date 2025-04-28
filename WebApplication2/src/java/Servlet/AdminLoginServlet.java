package Servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/AdminLoginServlet") // Matches admin form action in login.jsp
public class AdminLoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminLoginServlet.class.getName());

    // --- HARDCODED ADMIN CREDENTIALS ---
    // !! Change these in a real application !!
    private static final String ADMIN_EMAIL = "admin@petfesthub.com";
    private static final String ADMIN_PASSWORD = "adminpassword"; // Use plain text for now

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("adminEmail"); // Matches input name in login.jsp
        String password = request.getParameter("adminPassword"); // Matches input name

        String redirectPage = "login.jsp"; // Redirect back to login on failure

        if (email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            response.sendRedirect(redirectPage + "?adminError=Admin Email and Password are required.");
            return;
        }

        // Check against hardcoded credentials
        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            // Admin login successful
            LOGGER.info("Admin login successful: " + email);

            HttpSession session = request.getSession(true);
            session.setAttribute("isAdmin", true); // Flag to identify admin session
            session.setAttribute("adminEmail", email);
             session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout

            // Redirect to the admin dashboard
            response.sendRedirect("AdminDashboardServlet"); // Redirect to the dashboard servlet

        } else {
            // Admin login failed
            LOGGER.warning("Admin login failed for email: " + email);
            response.sendRedirect(redirectPage + "?adminError=Invalid Admin credentials.");
        }
    }
}