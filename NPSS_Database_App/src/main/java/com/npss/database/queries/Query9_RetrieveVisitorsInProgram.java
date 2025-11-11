package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 9: Retrieve the list of visitors enrolled in a specific park program, including their accessibility needs
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_visitor_enrolls_program_program_name (for filtering by program)
 * - IX_visitor_enrolls_program_visitor_id_number (for joining with Visitor)
 * - Clustered index on Individual.id_number (for joining with Individual)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query9_RetrieveVisitorsInProgram {
    private Connection connection;
    private Scanner scanner;

    public Query9_RetrieveVisitorsInProgram(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 9: Retrieve the list of visitors enrolled in a specific park program, including their accessibility needs
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 9] Retrieve the list of visitors enrolled in a specific park program, including their accessibility needs");
        
        try {
            // Get user input
            System.out.print("Enter the program name: ");
            String programName = scanner.nextLine().trim();
            
            // SQL query - leverages indexes on Visitor_enrolls_program and joins with Visitor and Individual
            String SQL = 
                "SELECT i.id_number, " +
                "       i.first_name, " +
                "       i.last_name, " +
                "       CONCAT(i.first_name, ' ', i.last_name) AS full_name, " +
                "       v.accessibility_needs, " +
                "       v.visit_date " +
                "FROM Visitor_enrolls_program vep " +
                "INNER JOIN Visitor v ON vep.visitor_id_number = v.id_number " +
                "INNER JOIN Individual i ON v.id_number = i.id_number " +
                "WHERE vep.program_name = ? " +
                "ORDER BY i.last_name, i.first_name"; // sort the results 

            // Fill in the variables & displaying 
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, programName);
            
            ResultSet rs = pstmt.executeQuery();
        
            boolean hasResults = false;
            
            System.out.println("Visitors Enrolled in Program: " + programName);
            
            while (rs.next()) {
                if (!hasResults) {
                    hasResults = true;
                }
                
                System.out.println("ID Number: " + rs.getString("id_number"));
                System.out.println("Name: " + rs.getString("full_name"));
                
                String accessibilityNeeds = rs.getString("accessibility_needs");
                if (accessibilityNeeds != null && !accessibilityNeeds.isEmpty()) {
                    System.out.println("Accessibility Needs: " + accessibilityNeeds);
                } else {
                    System.out.println("Accessibility Needs: None ");
                }
                
                java.sql.Date visitDate = rs.getDate("visit_date");
                if (visitDate != null) {
                    System.out.println("Visit Date: " + visitDate.toString());
                } else {
                    System.out.println("Visit Date: Not specified");
                }
            }
            
            if (!hasResults) {
                System.out.println("No visitors found enrolled in program: " + programName);
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

