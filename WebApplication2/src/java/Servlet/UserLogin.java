package Servlet;

// REMOVE THIS IMPORT: import util.PasswordUtil;
import conn.DBconnection;

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

@WebServlet("/UserLoginServlet")
public class UserLogin extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(UserLogin.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password"); // Entered plain password

        String redirectPage = "login.jsp";

        if (email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            response.sendRedirect(redirectPage + "?error=Email and Password are required.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBconnection.getConnection();
             if (conn == null) {
                 LOGGER.severe("Failed to get database connection for login.");
                 response.sendRedirect(redirectPage + "?error=Server error during login. Please try again later.");
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

                // REMOVE HASHING: String enteredHashedPassword = PasswordUtil.hashPassword(password);

                // Compare the entered plain password directly with the stored plain password
                if (password.equals(storedPassword)) { // Direct comparison
                    // Passwords match - Login successful
                    LOGGER.info("User login successful: " + email);
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", userId);
                    session.setAttribute("userName", userName);
                    session.setAttribute("userEmail", email);
                    session.setMaxInactiveInterval(1800);

                    response.sendRedirect("welcome.jsp");
                } else {
                    // Password mismatch
                    LOGGER.warning("Invalid password attempt for user: " + email);
                    response.sendRedirect(redirectPage + "?error=Invalid email or password.");
                }
            } else {
                // User not found
                LOGGER.warning("Login attempt for non-existent user: " + email);
                response.sendRedirect(redirectPage + "?error=Invalid email or password.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during user login for: " + email, e);
            response.sendRedirect(redirectPage + "?error=Database error during login.");
        } finally {
            // Close resources (same as before)
             try { if (rs != null) rs.close(); } catch (SQLException e) { /* Log */ }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { /* Log */ }
        }
    }
}