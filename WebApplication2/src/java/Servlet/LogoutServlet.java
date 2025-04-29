package Servlet; // Your package name

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LogoutServlet") // URL for the logout link
public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // It's good practice to handle POST as well, in case a form is used
        handleLogout(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get the current session, but don't create one if it doesn't exist
        HttpSession session = request.getSession(false);

        String userIdentifier = "User"; // Default identifier for logging
        if (session != null) {
            // Get user/admin identifier for logging before invalidating
            if (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin")) {
                userIdentifier = "Admin (" + session.getAttribute("adminEmail") + ")";
            } else if (session.getAttribute("userEmail") != null) {
                userIdentifier = "User (" + session.getAttribute("userEmail") + ")";
            }

            LOGGER.info("Logging out " + userIdentifier);
            // Invalidate the session, removing all attributes
            session.invalidate();
        } else {
             LOGGER.info("Logout request received, but no active session found.");
        }

        // Redirect to the login page (or homepage) after logout
        // Add a message indicating successful logout
        response.sendRedirect(request.getContextPath() + "/login.jsp?message=You+have+been+logged+out+successfully.");
    }
}