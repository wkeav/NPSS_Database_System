package com.npss.database;
import com.npss.database.NPSS_DBApp;
import java.sql.SQLException;

/**
 * Main entry point for the National Park Service System (NPSS) Database Application
 * 
 * This application connects to Azure SQL Database and provides a menu-driven interface
 * for managing park data, visitors, rangers, researchers, donations, and programs.
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Main {
    /**
     * Main method 
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        NPSS_DBApp npssApp = new NPSS_DBApp();
        
        try {
            // Connect to database
            boolean connected = npssApp.connectToDatabase();
            
            if (connected) {
                // Start the application loop
                npssApp.run();
            } else {
                System.err.println("Failed to connect to database. Please check your connection settings.");
                System.err.println("Make sure environment variables are set:");
                System.err.println("  - DB_URL");
                System.err.println("  - DB_USERNAME");
                System.err.println("  - DB_PASSWORD");
            }
            
        } catch (SQLException e) {
            System.err.println("Database connection error:");
            System.err.println("  Message: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("  SQL State: " + e.getSQLState());
            }
            if (e.getErrorCode() != 0) {
                System.err.println("  Error Code: " + e.getErrorCode());
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure connection is closed
            npssApp.closeConnection();
        }
    }
}
