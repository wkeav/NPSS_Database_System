package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 13: Retrieve the names, IDs, contact information, and newsletter subscription status of all individuals in the database
 * 
 * This query leverages the following indexes for optimal performance:
 * - Clustered index on Individual.id_number (primary key)
 * - IX_individual_phone_numbers_id_number (for retrieving phone numbers)
 * - IX_individual_email_addresses_id_number (for retrieving email addresses)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query13_RetrieveAllIndividuals {
    private Connection connection;
    private Scanner scanner;

    public Query13_RetrieveAllIndividuals(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 13: Retrieve the names, IDs, contact information, and newsletter subscription status of all individuals in the database
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 13] Retrieve the names, IDs, contact information, and newsletter subscription status of all individuals in the database");
        
        try {
            // SQL query - Retrieve all individuals with their contact information
            // Uses LEFT JOIN for phone numbers and emails since they are multi-valued attributes
            String SQL = 
                "SELECT " +
                "    i.id_number, " +
                "    i.first_name, " +
                "    i.last_name, " +
                "    CONCAT(i.first_name, ' ', i.last_name) AS full_name, " +
                "    i.newsletter_status, " +
                "    ipn.phone_number, " +
                "    iea.email_address " +
                "FROM Individual i " +
                "LEFT JOIN Individual_phone_numbers ipn ON i.id_number = ipn.id_number " +
                "LEFT JOIN Individual_email_addresses iea ON i.id_number = iea.id_number " +
                "ORDER BY i.last_name, i.first_name, ipn.phone_number, iea.email_address";
            
            // Fill in the variables
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Display results
            boolean hasResults = false;
            String currentIndividualId = null;
            String currentIndividualName = null;
            Boolean currentNewsletterStatus = null;
            boolean firstIndividual = true;
            boolean firstPhone = true;
            boolean firstEmail = true;
        
            System.out.println("All Individuals in Database");
            
            while (rs.next()) {
                String individualId = rs.getString("id_number");
                
                if (currentIndividualId == null || !individualId.equals(currentIndividualId)) {
                    if (!firstIndividual) {
                        System.out.println(); // New line after contact info
                        System.out.println("-".repeat(80));
                    }
                    firstIndividual = false;
                    firstPhone = true;
                    firstEmail = true;
                    hasResults = true;
                    
                    currentIndividualId = individualId;
                    currentIndividualName = rs.getString("full_name");
                    currentNewsletterStatus = rs.getBoolean("newsletter_status");
                    
                    System.out.println("ID Number: " + currentIndividualId);
                    System.out.println("Name: " + currentIndividualName);
                    System.out.println("Newsletter Status: " + (currentNewsletterStatus ? "Subscribed" : "Not Subscribed"));
                    System.out.print("Phone Numbers: ");
                }
                
                // Display phone number, may be null 
                String phoneNumber = rs.getString("phone_number");
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    if (firstPhone) {
                        // First phone number for this individual
                        System.out.print(phoneNumber);
                        firstPhone = false;
                    } else {
                        // Additional phone number
                        System.out.print(", " + phoneNumber);
                    }
                } else {
                    // No phone numbers found
                    if (firstPhone) {
                        System.out.print("None");
                        firstPhone = false;
                    }
                }
                
                // Display email address 
                String emailAddress = rs.getString("email_address");
                if (emailAddress != null && !emailAddress.isEmpty()) {
                    if (firstEmail) {
                        System.out.print(" | Email: " + emailAddress);
                        firstEmail = false;
                    } else {
                        // Additional email 
                        System.out.print(", " + emailAddress);
                    }
                } else {
                    // No email found 
                    if (firstEmail) {
                        System.out.print(" | Email: None");
                        firstEmail = false;
                    }
                }
            }
            
            if (hasResults) {
                System.out.println(); // New line after last contact info
            } else {
                System.out.println("No individuals found in the database.");
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

