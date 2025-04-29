package Servlet; // Your package name

import conn.DBconnection; // Your DB connection class

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
import java.util.logging.Level;
import java.util.logging.Logger;

// Maps to the user login form action in login.jsp
@WebServlet("/UserLoginServlet")
// Class name matches your filename
public class UserLogin extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(UserLogin.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password"); // Entered plain password

        String redirectPage = "login.jsp"; // Redirect back here on failure

        if (email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            response.sendRedirect(redirectPage + "?error=Email+and+Password+are+required."); // URL encode message
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBconnection.getConnection();
             if (conn == null) {
                 LOGGER.severe("Failed to get database connection for login.");
                 response.sendRedirect(redirectPage + "?error=Server+error+during+login.+Please+try+again+later."); // URL encode
                 return;
            }

            String sql = "SELECT id, name, password FROM user WHERE email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found, check password
                String storedPassword = rs.getString("password"); // Get stored plain password
                int userId = rs.getInt("id");
                String userName = rs.getString("name");

                // Direct comparison of plain text passwords
                if (password.equals(storedPassword)) {
                    // Passwords match - Login successful
                    LOGGER.info("User login successful: " + email);

                    // Create or get session and store user info
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", userId);
                    session.setAttribute("userName", userName);
                    session.setAttribute("userEmail", email);
                    session.setMaxInactiveInterval(1800); // Session timeout 30 minutes

                    // **** MODIFIED: Redirect to homepage servlet mapping ****
                    response.sendRedirect(request.getContextPath() + "/home"); // Use context path and map to HomeServlet
                    // **** END MODIFICATION ****

                } else {
                    // Password mismatch
                    LOGGER.warning("Invalid password attempt for user: " + email);
                    response.sendRedirect(redirectPage + "?error=Invalid+email+or+password."); // URL encode
                }
            } else {
                // User not found
                LOGGER.warning("Login attempt for non-existent user: " + email);
                response.sendRedirect(redirectPage + "?error=Invalid+email+or+password."); // URL encode
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during user login for: " + email, e);
            response.sendRedirect(redirectPage + "?error=Database+error+during+login."); // URL encode
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e); }
             // Do not close the shared connection from DBconnection
        }
    }
}