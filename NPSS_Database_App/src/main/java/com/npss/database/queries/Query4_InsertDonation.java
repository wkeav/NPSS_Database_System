package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 4: Insert a new donation from a donor
 * If the donor doesn't exist, it will be created automatically
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query4_InsertDonation {
    private Connection connection;
    private Scanner scanner;

    public Query4_InsertDonation(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    // Check if donor exists
    private boolean donorExists(String donorId) throws SQLException {
        String checkSQL = "SELECT id_number FROM Donor WHERE id_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
            pstmt.setString(1, donorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check if individual exists first
    private boolean individualExists(String idNumber) throws SQLException {
        String checkSQL = "SELECT id_number FROM Individual WHERE id_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
            pstmt.setString(1, idNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Executes Query 4: Insert a new donation from a donor
     * If the donor doesn't exist, it will be created automatically
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 4] Insert a new donation from a donor");
        
        try {
            // User's input
            System.out.print("Enter donation ID: ");
            String donationId = scanner.nextLine().trim();
            
            System.out.print("Enter donor ID number: ");
            String donorId = scanner.nextLine().trim();
            
            // Check if donor exists, if not, collect information to create it
            boolean donorNeedsCreation = !donorExists(donorId);
            boolean individualNeedsCreation = false;
            
            // Variables to store donor/individual information if needed
            String firstName = null, lastName = null, gender = null;
            String street = null, city = null, state = null, postalCode = null;
            String dateOfBirth = null;
            boolean newsletterStatus = false;
            String preference = null;
            
            if (donorNeedsCreation) {
                System.out.println("\nDonor '" + donorId + "' does not exist. Creating new donor...");
                System.out.println("Please provide the following information:");
                
                // Check if Individual exists
                individualNeedsCreation = !individualExists(donorId);
                
                if (individualNeedsCreation) {
                    System.out.println("Individual record not found. Creating Individual first...");
                    
                    System.out.print("Enter first name: ");
                    firstName = scanner.nextLine().trim();
                    
                    System.out.print("Enter last name: ");
                    lastName = scanner.nextLine().trim();
                    
                    System.out.print("Enter gender (M/F/O): ");
                    gender = scanner.nextLine().trim();
                    
                    System.out.print("Enter street address: ");
                    street = scanner.nextLine().trim();
                    
                    System.out.print("Enter city: ");
                    city = scanner.nextLine().trim();
                    
                    System.out.print("Enter state: ");
                    state = scanner.nextLine().trim();
                    
                    System.out.print("Enter postal code: ");
                    postalCode = scanner.nextLine().trim();
                    
                    System.out.print("Enter date of birth (YYYY-MM-DD): ");
                    dateOfBirth = scanner.nextLine().trim();
                    
                    System.out.print("Subscribe to newsletter? (true/false): ");
                    newsletterStatus = Boolean.parseBoolean(scanner.nextLine().trim());
                } else {
                    System.out.println("Individual record found for ID: " + donorId);
                }
                
                // Get donor preference
                System.out.print("Enter donor preference (or press Enter for NULL): ");
                preference = scanner.nextLine().trim();
                if (preference.isEmpty()) preference = null;
            } else {
                System.out.println("Donor '" + donorId + "' found in database.");
            }
            
            System.out.print("Enter donation date (YYYY-MM-DD): ");
            String donationDate = scanner.nextLine().trim();
            
            System.out.print("Enter donation amount: ");
            String amountStr = scanner.nextLine().trim();
            double amount = Double.parseDouble(amountStr);
            
            System.out.print("Enter campaign name (or press Enter for NULL): ");
            String campaignName = scanner.nextLine().trim();
            if (campaignName.isEmpty()) campaignName = null;
            
            System.out.print("Enter payment method (check/card): ");
            String paymentMethod = scanner.nextLine().trim().toLowerCase();
            
            // SQL queries
            String insertIndividualSQL = 
                "INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, " +
                "postal_code, date_of_birth, newsletter_status) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            String insertDonorSQL = "INSERT INTO Donor(id_number, preference) VALUES (?, ?)";
            
            String insertDonationSQL = 
                "INSERT INTO Donation(donation_id, donor_id_number, date, amount, campaign_name) " +
                "VALUES (?, ?, ?, ?, ?)";
            
            // Executing
            connection.setAutoCommit(false);
            
            try {
                // Create Individual if needed
                if (individualNeedsCreation) {
                    try (PreparedStatement pstmt = connection.prepareStatement(insertIndividualSQL)) {
                        pstmt.setString(1, donorId);
                        pstmt.setString(2, firstName);
                        pstmt.setString(3, lastName);
                        pstmt.setString(4, gender);
                        pstmt.setString(5, street);
                        pstmt.setString(6, city);
                        pstmt.setString(7, state);
                        pstmt.setString(8, postalCode);
                        pstmt.setDate(9, java.sql.Date.valueOf(dateOfBirth));
                        pstmt.setBoolean(10, newsletterStatus);
                        
                        int rowsAffected = pstmt.executeUpdate();
                        System.out.println("Individual record created successfully! (Rows affected: " + rowsAffected + ")");
                    }
                }
                
                // Create Donor if needed
                if (donorNeedsCreation) {
                    try (PreparedStatement pstmt = connection.prepareStatement(insertDonorSQL)) {
                        pstmt.setString(1, donorId);
                        if (preference != null) {
                            pstmt.setString(2, preference);
                        } else {
                            pstmt.setNull(2, java.sql.Types.VARCHAR);
                        }
                        
                        int rowsAffected = pstmt.executeUpdate();
                        System.out.println("Donor '" + donorId + "' created successfully! (Rows affected: " + rowsAffected + ")");
                    }
                }
                
                // Create donation
                try (PreparedStatement pstmtDonation = connection.prepareStatement(insertDonationSQL)) {
                    pstmtDonation.setString(1, donationId);
                    pstmtDonation.setString(2, donorId);
                    pstmtDonation.setDate(3, java.sql.Date.valueOf(donationDate));
                    pstmtDonation.setDouble(4, amount);
                    if (campaignName != null) {
                        pstmtDonation.setString(5, campaignName);
                    } else {
                        pstmtDonation.setNull(5, java.sql.Types.VARCHAR);
                    }
                    
                    int rows1 = pstmtDonation.executeUpdate();
                    
                    // Insert payment method details
                    int rows2 = 0;
                    if (paymentMethod.equals("check")) {
                        System.out.print("Enter check number: ");
                        String checkNumber = scanner.nextLine().trim();
                        
                        String insertCheckSQL = 
                            "INSERT INTO Check_donation(donation_id, check_number) " +
                            "VALUES (?, ?)";
                        try (PreparedStatement pstmtCheck = connection.prepareStatement(insertCheckSQL)) {
                            pstmtCheck.setString(1, donationId);
                            pstmtCheck.setString(2, checkNumber);
                            rows2 = pstmtCheck.executeUpdate();
                        }
                        
                    } else if (paymentMethod.equals("card")) {
                        System.out.print("Enter card type: ");
                        String cardType = scanner.nextLine().trim();
                        
                        System.out.print("Enter last four digits: ");
                        String lastFour = scanner.nextLine().trim();
                        
                        System.out.print("Enter expiration date (YYYY-MM-DD): ");
                        String expDate = scanner.nextLine().trim();
                        
                        String insertCardSQL = 
                            "INSERT INTO Card_number(donation_id, card_type, last_four_digits, expiration_date) " +
                            "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement pstmtCard = connection.prepareStatement(insertCardSQL)) {
                            pstmtCard.setString(1, donationId);
                            pstmtCard.setString(2, cardType);
                            pstmtCard.setString(3, lastFour);
                            pstmtCard.setDate(4, java.sql.Date.valueOf(expDate));
                            rows2 = pstmtCard.executeUpdate();
                        }
                    }
                    
                    connection.commit();
                    System.out.println("Donation inserted successfully! (Donation rows: " + rows1 + ", Payment rows: " + rows2 + ")");
                    
                    // Verify the insert
                    String verifySQL = "SELECT d.donation_id, d.amount, dr.id_number as donor_id " +
                                      "FROM Donation d " +
                                      "INNER JOIN Donor dr ON d.donor_id_number = dr.id_number " +
                                      "WHERE d.donation_id = ?";
                    try (PreparedStatement verifyStmt = connection.prepareStatement(verifySQL)) {
                        verifyStmt.setString(1, donationId);
                        try (ResultSet rs = verifyStmt.executeQuery()) {
                            if (rs.next()) {
                                System.out.println("VERIFIED: Donation " + donationId + " ($" + 
                                                 String.format("%.2f", rs.getDouble("amount")) + 
                                                 ") from donor " + rs.getString("donor_id") + " is in the database");
                            } else {
                                System.out.println("WARNING: Donation " + donationId + " not found after insert!");
                            }
                        }
                    }
                }
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            throw e;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid date format: " + e.getMessage());
            System.err.println("Please use YYYY-MM-DD format for dates.");
            throw new SQLException("Invalid input: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error: " + e.getMessage(), e);
        }
    }
}

