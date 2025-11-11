package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 11: Retrieve the total and average donation amount received in a month from all anonymous donors
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_donation_date (for filtering by month/year)
 * - IX_donation_donor_id_number (for joining with Donor)
 * - IX_donor_preference (for filtering anonymous donors)
 * - Clustered index on Donor.id_number (for grouping by donor)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query11_RetrieveDonationStats {
    private Connection connection;
    private Scanner scanner;

    public Query11_RetrieveDonationStats(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 11: Retrieve the total and average donation amount received in a month from all anonymous donors
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 11] Retrieve the total and average donation amount received in a month from all anonymous donors");
        
        try{
            // Get user input
            System.out.println("Enter the month (MM)");
            String month = scanner.nextLine().trim();
            System.out.print("Enter the year (YYYY): ");
            String year = scanner.nextLine().trim();

            if(month.isEmpty() || year.isEmpty()){
                System.out.println("Error: Month and year cannot be empty.");
                return;
            }

            // Logic check 
            int monthInt, yearInt; 
            try {
                monthInt = Integer.parseInt(month);
                yearInt = Integer.parseInt(year);
                
                if (monthInt < 1 || monthInt > 12) {
                    System.out.println("Error: Month must be between 01 and 12.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
                return;
            }

            // SQL query - Retrieve the total and average donation amount received in a 
            // month from all anonymous donors
            String SQL = "SELECT "  +
            "dr.id_number AS donor_id, " + 
            "SUM(d.amount) AS total_amount, " + 
            "AVG(d.amount) AS average_amount, " + 
            "COUNT(d.donation_id) AS donation_count " + 
            "FROM Donation d " + 
            "INNER JOIN Donor dr ON d.donor_id_number = dr.id_number " +
            "WHERE YEAR(d.date) = ? " + 
            "AND MONTH(d.date) = ? " + 
            "AND (dr.preference IS NULL OR dr.preference = 'Anonymous') " +
            "GROUP BY dr.id_number " + "ORDER BY SUM(d.amount) DESC"; 

            // Fill in the variables 
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setInt(1, yearInt);
            pstmt.setInt(2, monthInt);

            ResultSet rs = pstmt.executeQuery(); 

            // Display results 
            boolean hasResults = false;

            System.out.println("Anonymous Donor Statistics for " + month + "/" + year);
            System.out.println("Sorted by Total Amount (Descending)");

            while (rs.next()) {
                hasResults = true;
                
                System.out.println("Donor ID: " + rs.getString("donor_id"));
                System.out.println("Total Amount: $" + String.format("%.2f", rs.getDouble("total_amount")));
                System.out.println("Average Amount: $" + String.format("%.2f", rs.getDouble("average_amount")));
                System.out.println("Number of Donations: " + rs.getInt("donation_count"));
            }

            if (!hasResults) {
                System.out.println("No anonymous donations found for " + month + "/" + year);
            }
            
            rs.close();
            pstmt.close();

        }catch(SQLException e){
            System.err.println("Database error: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            throw e;
        }catch(Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
}

