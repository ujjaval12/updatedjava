package Servlet;

// REMOVE THIS IMPORT: import util.PasswordUtil;
import conn.DBconnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/SignupServlet")
public class SignUp extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SignUp.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password"); // Get the plain password

        // REMOVE HASHING STEP: String hashedPassword = PasswordUtil.hashPassword(password);

        String redirectPage = "signup.jsp";

        // Basic validation (check if fields are empty)
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) { // Just check if password is empty
            response.sendRedirect(redirectPage + "?error=Please fill in all fields.");
            return;
        }

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                 LOGGER.severe("Failed to get database connection for signup.");
                 response.sendRedirect(redirectPage + "?error=Server error during signup. Please try again later.");
                 return;
            }

            // 1. Check if email already exists (remains the same)
            String checkSql = "SELECT COUNT(*) FROM user WHERE email = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                response.sendRedirect(redirectPage + "?error=Email already registered.");
            } else {
                // 2. Insert new user with PLAIN TEXT PASSWORD
                String insertSql = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
                insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password); // Store the plain password directly

                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    LOGGER.info("New user registered (PLAIN PASSWORD): " + email); // Log warning
                    response.sendRedirect("login.jsp?message=Signup successful! Please login.");
                } else {
                    LOGGER.warning("User insertion failed for email: " + email);
                    response.sendRedirect(redirectPage + "?error=Registration failed. Please try again.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error during user signup for email: " + email, e);
            response.sendRedirect(redirectPage + "?error=Database error during registration.");
        } finally {
            // Close resources (same as before)
             try { if (rs != null) rs.close(); } catch (SQLException e) { /* Log */ }
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException e) { /* Log */ }
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException e) { /* Log */ }
        }
    }
}