package Servlet;

import conn.DBconnection;
// Correct import for your Event bean
import model.Event;

import javax.servlet.RequestDispatcher;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ViewEventsServlet")
public class ViewEventsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ViewEventsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info("ViewEventsServlet processing GET request...");
        List<model.Event> allEvents = new ArrayList<>(); // Use model.Event
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String dbError = null;

        try {
            conn = DBconnection.getConnection();
            if (conn == null) {
                dbError = "Database connection failed. Cannot load events.";
                LOGGER.severe("Failed to get DB connection for ViewEventsServlet.");
            } else {
                // **** CORRECTED SQL: Select image_filename ****
                String sql = "SELECT event_id, title, event_date, location, description, image_filename " +
                             "FROM events " +
                             "ORDER BY event_date ASC";

                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    model.Event event = new model.Event(); // Use model.Event
                    event.setEventId(rs.getInt("event_id"));
                    event.setTitle(rs.getString("title"));
                    event.setEventDate(rs.getDate("event_date"));
                    event.setLocation(rs.getString("location"));
                    event.setDescription(rs.getString("description"));
                    // **** CORRECTED: Get image_filename and use setImageFilename ****
                    event.setImageFilename(rs.getString("image_filename"));
                    allEvents.add(event);
                }
                LOGGER.info("Fetched " + allEvents.size() + " total events.");
            }
        } catch (SQLException e) {
            dbError = "Error fetching events from database.";
            LOGGER.log(Level.SEVERE, "SQL Error fetching all events", e);
        } catch (Exception e) {
             dbError = "An unexpected error occurred while fetching events.";
            LOGGER.log(Level.SEVERE, "Unexpected error fetching all events", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e); }
        }

        // Set attributes for the JSP
        if (dbError != null) {
            request.setAttribute("eventsError", dbError);
        }
        request.setAttribute("allEvents", allEvents); // Attribute name matches events.jsp

        // Forward the request to the events JSP page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/events.jsp");
        dispatcher.forward(request, response);
    }

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
     }
}