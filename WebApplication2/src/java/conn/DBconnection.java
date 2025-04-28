package conn; // Package name matching your structure

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBconnection {

    private static final String DB_USER = "root"; // CHANGE IF NEEDED
    private static final String DB_PASSWORD = "root"; // CHANGE IF NEEDED
    private static final String DB_NAME = "webappdb2"; // Your DB name
    private static final String DB_URL_SERVER = "jdbc:mysql://localhost:3306/?user=" + DB_USER + "&password=" + DB_PASSWORD + "&sslMode=DISABLED";
    private static final String DB_URL_APP = "jdbc:mysql://localhost:3306/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASSWORD + "&sslMode=DISABLED";
    private static final Logger LOGGER = Logger.getLogger(DBconnection.class.getName());
    private static volatile Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            synchronized (DBconnection.class) {
                if (connection == null) {
                    try {
                        LOGGER.info("Attempting database initialization for " + DB_NAME + "...");
                        initializeDatabase();
                        LOGGER.info("Database connection established successfully to " + DB_NAME + ".");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "FATAL: Database initialization failed for " + DB_NAME + "!", e);
                        return null; // Indicate failure
                    }
                }
            }
        }
        return connection;
    }


    private static void initializeDatabase() throws ClassNotFoundException, SQLException {
        LOGGER.info("Loading MySQL JDBC driver (com.mysql.cj.jdbc.Driver)...");
        Class.forName("com.mysql.cj.jdbc.Driver");
        LOGGER.info("Driver loaded.");

        // Step 1: Create Database if needed
        LOGGER.info("Connecting to MySQL server to check/create database...");
        try (Connection tempConn = DriverManager.getConnection(DB_URL_SERVER);
             Statement stmt = tempConn.createStatement()) {
            LOGGER.info("Executing: CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            LOGGER.info("Database '" + DB_NAME + "' checked/created.");
        } // tempConn and stmt automatically closed

        // Step 2: Connect to App Database
        LOGGER.info("Connecting to specific database: " + DB_NAME + "...");
        connection = DriverManager.getConnection(DB_URL_APP);
        LOGGER.info("Connected to " + DB_NAME + ".");

        // Step 3: Create user table if needed
        LOGGER.info("Checking/Creating 'user' table...");
        try (Statement stmtUser = connection.createStatement()) {
            String createUserTable = "CREATE TABLE IF NOT EXISTS user (" +
                                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                                     "name VARCHAR(100) NOT NULL," +
                                     "email VARCHAR(100) NOT NULL UNIQUE," +
                                     "password VARCHAR(255) NOT NULL" + // Plain text for now
                                     ")";
            stmtUser.executeUpdate(createUserTable);
            LOGGER.info("'user' table checked/created.");
        }

        // Step 4: Create/Update events table
        LOGGER.info("Checking/Creating/Updating 'events' table...");
        try (Statement stmtEvent = connection.createStatement()) {
            // --- Try to add columns, ignore 'Duplicate column' error ---
            try {
                stmtEvent.executeUpdate("ALTER TABLE events ADD COLUMN location VARCHAR(255) NULL");
                LOGGER.info("Column 'location' potentially added to 'events' table.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1060) { // MySQL error code for "Duplicate column name"
                    LOGGER.fine("Ignoring ALTER TABLE error for 'location' (column already exists).");
                } else {
                    throw e; // Re-throw other errors
                }
            }
            try {
                stmtEvent.executeUpdate("ALTER TABLE events ADD COLUMN image_filename VARCHAR(255) NULL");
                LOGGER.info("Column 'image_filename' potentially added to 'events' table.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1060) { // MySQL error code for "Duplicate column name"
                    LOGGER.fine("Ignoring ALTER TABLE error for 'image_filename' (column already exists).");
                } else {
                    throw e; // Re-throw other errors
                }
            }
            // --- End add columns ---

            // Create table if it doesn't exist
            String createEventsTable = "CREATE TABLE IF NOT EXISTS events (" +
                                       "event_id INT AUTO_INCREMENT PRIMARY KEY," +
                                       "title VARCHAR(255) NOT NULL," +
                                       "event_date DATE," +
                                       "location VARCHAR(255)," +
                                       "description TEXT," +
                                       "image_filename VARCHAR(255)," +
                                       "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                       ")";
            stmtEvent.executeUpdate(createEventsTable);
            LOGGER.info("'events' table checked/created/updated.");
        }

        // Step 5: Create/Update event_requests table
        LOGGER.info("Checking/Creating 'event_requests' table...");
        try (Statement stmtRequest = connection.createStatement()) {
             // --- Try to add column, ignore 'Duplicate column' error ---
             try {
                stmtRequest.executeUpdate("ALTER TABLE event_requests ADD COLUMN image_filename VARCHAR(255) NULL");
                LOGGER.info("Column 'image_filename' potentially added to 'event_requests' table.");
             } catch (SQLException e) {
                if (e.getErrorCode() == 1060) { // MySQL error code for "Duplicate column name"
                    LOGGER.fine("Ignoring ALTER TABLE error for 'image_filename' in event_requests (column already exists).");
                } else {
                    throw e; // Re-throw other errors
                }
            }
            // --- End add column ---

            // Create table if it doesn't exist
            String createRequestTable = "CREATE TABLE IF NOT EXISTS event_requests (" +
                                        "request_id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "event_name VARCHAR(255) NOT NULL," +
                                        "location VARCHAR(255)," +
                                        "requested_date DATE," +
                                        "description TEXT," +
                                        "image_filename VARCHAR(255) NULL," +
                                        "status VARCHAR(20) DEFAULT 'PENDING'," +
                                        "requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                        ")";
            stmtRequest.executeUpdate(createRequestTable);
            LOGGER.info("'event_requests' table checked/created.");
        }
    } // End initializeDatabase


    // Method to close connection
    public static void closeConnection() {
        synchronized (DBconnection.class) {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                    LOGGER.info("Database connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing database connection", e);
                }
            }
        }
    }
} // End class DBconnection