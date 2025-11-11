package com.npss.database.queries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Export Service: Retrieve names and mailing addresses of all people on the mailing list
 * and output them to a data file instead of screen
 * 
 * The mailing list consists of all individuals with newsletter_status = true
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class ExportService {
    private Connection connection;
    private Scanner scanner;

    public ExportService(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes the export functionality: Retrieve names and mailing addresses of all people on the mailing list
     * and output them to a data file instead of screen
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Export] Retrieve names and mailing addresses of all people on the mailing list");
        System.out.print("Please enter the output file name: ");
        String outputFileName = scanner.nextLine().trim();
        
        if (outputFileName.isEmpty()) {
            System.out.println("Error: File name cannot be empty.");
            return;
        }
        
        // Determine file path (current directory or project root)
        Path filePath = findFile(outputFileName);
        
        // SQL query to retrieve all people on the mailing list (newsletter_status = true)
        String SQL = 
            "SELECT " +
            "    CONCAT(i.first_name, ' ', i.last_name) AS full_name, " +
            "    i.street, " +
            "    i.city, " +
            "    i.state, " +
            "    i.postal_code " +
            "FROM Individual i " +
            "WHERE i.newsletter_status = 1 " +
            "ORDER BY i.last_name, i.first_name";
        
        int recordCount = 0;
        
        try (PreparedStatement pstmt = connection.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            
            // Write header line
            writer.write("Name,Street,City,State,Postal Code");
            writer.newLine();
            
            // Write each record
            while (rs.next()) {
                String fullName = rs.getString("full_name");
                String street = rs.getString("street");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String postalCode = rs.getString("postal_code");
                
                // Format as CSV: Name,Street,City,State,Postal Code
                writer.write(escapeCSV(fullName) + ",");
                writer.write(escapeCSV(street) + ",");
                writer.write(escapeCSV(city) + ",");
                writer.write(escapeCSV(state) + ",");
                writer.write(escapeCSV(postalCode));
                writer.newLine();
                
                recordCount++;
            }
            
            System.out.println("Export completed successfully!");
            System.out.println("File saved to: " + filePath.toAbsolutePath());
            System.out.println("Total records exported: " + recordCount);
            
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw new SQLException("File I/O error: " + e.getMessage(), e);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            throw e;
        }
    }
    
    /**
     * Finds the file path in current directory or project root
     */
    private Path findFile(String fileName) {
        // Try current directory first
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentDir.resolve(fileName);

        // If in the project root, use it
        if (currentDir.getFileName().toString().equals("NPSS_Database_App")) {
            return filePath;
        } else {
            // Try going up one level to find NPSS_Database_App
            Path projectRoot = currentDir.resolve("NPSS_Database_App");
            if (Files.exists(projectRoot)) {
                return projectRoot.resolve(fileName);
            }
        }
        return filePath;
    }
    
    /**
     * Escapes CSV values (handles commas, quotes, and NULL values)
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

