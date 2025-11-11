package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 8: Retrieve the names and contact information of all emergency contacts for a specific person
 * 
 * This query leverages the IX_emergency_contact_id_number index for efficient lookups.
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query8_RetrieveEmergencyContacts {
    private Connection connection;
    private Scanner scanner;

    public Query8_RetrieveEmergencyContacts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 8: Retrieve the names and contact information of all emergency contacts for a specific person
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 8] Retrieve the names and contact information of all emergency contacts for a specific person");
        
        try {
            // Get user input
            System.out.print("Enter the person's ID number: ");
            String idNumber = scanner.nextLine().trim();
            
            if (idNumber.isEmpty()) {
                System.out.println("Error: ID number cannot be empty.");
                return;
            }
            
            // SQL query - leverages IX_emergency_contact_id_number index
            // ec is table alias for Emergency_contact table 
            // Also joins with Individual to get the person's name for context
            String SQL = 
                "SELECT ec.name AS emergency_contact_name, " + 
                "       ec.relationship, " +
                "       ec.phone_number, " +
                "       CONCAT(i.first_name, ' ', i.last_name) AS person_name " + //first name & last name from individual alias 
                "FROM Emergency_contact ec " +
                "INNER JOIN Individual i ON ec.id_number = i.id_number " + //joins with individual table
                "WHERE ec.id_number = ? " +
                "ORDER BY ec.relationship, ec.name"; //sort results 
            
            // Fill in the variables 
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, idNumber);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Display results
            boolean hasResults = false;
            String personName = null;
            
            System.out.println("Emergency Contacts");
            
            while (rs.next()) {
                if (!hasResults) {
                    personName = rs.getString("person_name");
                    System.out.println("- Person: " + personName + " (ID: " + idNumber + ")");
                    hasResults = true;
                }
                
                System.out.println("- Contact Name: " + rs.getString("emergency_contact_name"));
                System.out.println("- Relationship: " + rs.getString("relationship"));
                System.out.println("- Phone Number: " + rs.getString("phone_number"));
            }
            
            if (!hasResults) {
                System.out.println("- No emergency contacts found for ID number: " + idNumber);
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

