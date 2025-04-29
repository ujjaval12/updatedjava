package conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            LOGGER.info("Database '" + DB_NAME + "' checked/created.");
        }

        // Step 2: Connect to App Database
        LOGGER.info("Connecting to database: " + DB_NAME);
        connection = DriverManager.getConnection(DB_URL_APP);
        LOGGER.info("Connected to database: " + DB_NAME + ".");

        // Step 3: Create 'user' table
        LOGGER.info("Checking/Creating 'user' table...");
        try (Statement stmtUser = connection.createStatement()) {
            String createUserTable = "CREATE TABLE IF NOT EXISTS user (" +
                                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                                     "name VARCHAR(100) NOT NULL," +
                                     "email VARCHAR(100) NOT NULL UNIQUE," +
                                     "password VARCHAR(255) NOT NULL)"; // Plain text for now
            stmtUser.executeUpdate(createUserTable);
            LOGGER.info("'user' table checked/created.");
        }

        // Step 4: Create/Update 'events' table
        LOGGER.info("Checking/Creating/Updating 'events' table...");
        try (Statement stmtEvent = connection.createStatement()) {
            // Create table first if it doesn't exist
            String createEventsTable = "CREATE TABLE IF NOT EXISTS events (" +
                                       "event_id INT AUTO_INCREMENT PRIMARY KEY," +
                                       "title VARCHAR(255) NOT NULL," +
                                       "event_date DATE," +
                                       "description TEXT," +
                                       "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                       ")";
            stmtEvent.executeUpdate(createEventsTable);

            // Add columns if they don't exist
            addColumnIfNotExists(stmtEvent, "events", "location", "VARCHAR(255) NULL");
            addColumnIfNotExists(stmtEvent, "events", "image_filename", "VARCHAR(255) NULL");

            LOGGER.info("'events' table checked/created/updated.");
        }

        // **** Step 5: Create/Update 'event_requests' table (with requester_user_id) ****
        LOGGER.info("Checking/Creating/Updating 'event_requests' table...");
        try (Statement stmtRequest = connection.createStatement()) {
            // Create table first if it doesn't exist
             String createRequestTable = "CREATE TABLE IF NOT EXISTS event_requests (" +
                                        "request_id INT AUTO_INCREMENT PRIMARY KEY," +
                                        "event_name VARCHAR(255) NOT NULL," +
                                        "location VARCHAR(255)," +
                                        "requested_date DATE," +
                                        "description TEXT," +
                                        "status VARCHAR(20) DEFAULT 'PENDING'," +
                                        "requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                        ")";
            stmtRequest.executeUpdate(createRequestTable);

             // Add columns if they don't exist
            addColumnIfNotExists(stmtRequest, "event_requests", "image_filename", "VARCHAR(255) NULL");
            // **** ADD requester_user_id column ****
            addColumnIfNotExists(stmtRequest, "event_requests", "requester_user_id", "INT NULL");

            // **** ADD Foreign Key constraint (best effort) ****
            try {
                 stmtRequest.executeUpdate("ALTER TABLE event_requests ADD CONSTRAINT fk_requester_user " +
                                           "FOREIGN KEY (requester_user_id) REFERENCES user(id) ON DELETE SET NULL");
                 LOGGER.info("Foreign key 'fk_requester_user' potentially added to 'event_requests'.");
             } catch (SQLException e) {
                 // MySQL Error Code 1061: Duplicate key name
                 // Error Code 1826: Duplicate foreign key (newer MySQL)
                 // Some error messages might contain specific strings
                 if (e.getErrorCode() == 1061 || e.getErrorCode() == 1826 || e.getMessage().contains("Duplicate key name") || e.getMessage().contains("CONSTRAINT `fk_requester_user` already exists")) {
                      LOGGER.fine("Ignoring ALTER TABLE error for 'fk_requester_user' (constraint likely already exists).");
                 } else {
                      LOGGER.log(Level.WARNING, "Error adding foreign key 'fk_requester_user'. It might fail if 'user' table doesn't exist yet on first run.", e);
                      // Consider if lack of FK is critical; maybe retry later? For now, just log.
                 }
             }

            LOGGER.info("'event_requests' table checked/created/updated.");
        } // **** END Step 5 ****


        // Step 6: Create event_participants table
        LOGGER.info("Checking/Creating 'event_participants' table...");
        try (Statement stmtParticipate = connection.createStatement()) {
            String createParticipantTable = "CREATE TABLE IF NOT EXISTS event_participants (" +
                                            "registration_id INT AUTO_INCREMENT PRIMARY KEY," +
                                            "event_id INT NOT NULL," +
                                            "user_id INT NOT NULL," +
                                            "registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                            "FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE," +
                                            "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE," +
                                            "UNIQUE KEY unique_participation (event_id, user_id)" +
                                            ")";
            stmtParticipate.executeUpdate(createParticipantTable);
            LOGGER.info("'event_participants' table checked/created.");
        }

    } // End initializeDatabase


    // Helper method to add column if it doesn't exist using information_schema
    private static void addColumnIfNotExists(Statement stmt, String tableName, String columnName, String columnDefinition) {
        try {
            // Check if column exists in the current database
            ResultSet rs = stmt.executeQuery(
               "SELECT 1 FROM information_schema.columns " +
               "WHERE table_schema = DATABASE() " +
               "AND table_name = '" + tableName + "' " +
               "AND column_name = '" + columnName + "'"
            );

            if (!rs.next()) { // Column does not exist, so add it
               stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
               LOGGER.info("Column '" + columnName + "' added successfully to table '" + tableName + "'.");
            } else {
                 LOGGER.fine("Column '" + columnName + "' already exists in table '" + tableName + "'. No action taken.");
            }
            rs.close();
        } catch (SQLException e) {
           // Log errors during the check or alter process
            LOGGER.log(Level.WARNING, "Could not check/add column " + columnName + " for table " + tableName, e);
        }
    } // End addColumnIfNotExists


    // Method to close connection
    public static void closeConnection() {
        synchronized (DBconnection.class) {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null; // Reset static variable
                    LOGGER.info("Database connection closed.");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing database connection", e);
                }
            }
        }
    } // End closeConnection

} // End class DBconnection