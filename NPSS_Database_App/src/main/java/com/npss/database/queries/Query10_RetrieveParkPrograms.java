package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 10: Retrieve all park programs for a specific park that started after a given date
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_national_parks_offers_program_park_name (for filtering by park)
 * - IX_national_parks_offers_program_program_name (for joining with Program)
 * - IX_program_start_date (for range query on start_date)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query10_RetrieveParkPrograms {
    private Connection connection;
    private Scanner scanner;

    public Query10_RetrieveParkPrograms(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 10: Retrieve all park programs for a specific park that started after a given date
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 10] Retrieve all park programs for a specific park that started after a given date");
        
        try {
            // Get user input
            System.out.print("Enter the park name: ");
            String parkName = scanner.nextLine().trim();
            
            if (parkName.isEmpty()) {
                System.out.println("Error: Park name cannot be empty.");
                return;
            }
            
            System.out.print("Enter the start date (YYYY-MM-DD) - programs starting after this date will be shown: ");
            String startDateStr = scanner.nextLine().trim();
            
            if (startDateStr.isEmpty()) {
                System.out.println("Error: Start date cannot be empty.");
                return;
            }
            
            java.sql.Date startDate;
            try {
                startDate = java.sql.Date.valueOf(startDateStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid date format. Please use YYYY-MM-DD format.");
                return;
            }
            
            // SQL query - leverages indexes on National_parks_offers_program and Program
            // Uses range query on start_date which benefits from IX_program_start_date index
            String SQL = 
                "SELECT p.program_name, " +
                "       p.type, " +
                "       p.start_date, " +
                "       p.duration " +
                "FROM National_parks_offers_program npop " +
                "INNER JOIN Program p ON npop.program_name = p.program_name " +
                "WHERE npop.park_name = ? " +
                "  AND p.start_date > ? " +
                "ORDER BY p.start_date, p.program_name";
            
            // Fill in the variables 
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, parkName);
            pstmt.setDate(2, startDate);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Display results
            boolean hasResults = false;
            System.out.println("Park Programs for: " + parkName);
            System.out.println("Programs starting after: " + startDate.toString());

            while (rs.next()) {
                if (!hasResults) {
                    hasResults = true;
                }
                
                System.out.println("Program Name: " + rs.getString("program_name"));
                System.out.println("Type: " + rs.getString("type"));
                System.out.println("Start Date: " + rs.getDate("start_date").toString());
                System.out.println("Duration: " + rs.getInt("duration") + " days");
            }
            
            if (!hasResults) {
                System.out.println("No programs found for park '" + parkName + "' starting after " + startDate.toString());
            }
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            throw e;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

