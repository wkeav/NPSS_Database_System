package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 7: Insert a new park program into the database for a specific park
 * If the park doesn't exist, it will be created automatically
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query7_InsertParkProgram {
    private Connection connection;
    private Scanner scanner;

    public Query7_InsertParkProgram(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Checks if a park exists in the database
     */
    private boolean parkExists(String parkName) throws SQLException {
        String checkSQL = "SELECT Name FROM National_parks WHERE Name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
            pstmt.setString(1, parkName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    /**
     * Executes Query 7: Insert a new park program into the database for a specific park
     * If the park doesn't exist, it will be created automatically
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 7] Insert a new park program into the database for a specific park");
        
        try {
            // User's input
            System.out.print("Enter program name: ");
            String programName = scanner.nextLine().trim();
            
            System.out.print("Enter program type: ");
            String programType = scanner.nextLine().trim();
            
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine().trim();
            
            System.out.print("Enter duration (in days): ");
            String durationStr = scanner.nextLine().trim();
            int duration = Integer.parseInt(durationStr);
            
            System.out.print("Enter park name: ");
            String parkName = scanner.nextLine().trim();
            
            // Create park if it does not exist 
            boolean parkNeedsCreation = !parkExists(parkName);
            String street = null, city = null, state = null, postalCode = null, establishmentDate = null;
            int capacity = 0;
            
            if (parkNeedsCreation) {
                System.out.println("\nPark '" + parkName + "' does not exist. Creating new park...");
                System.out.println("Please provide the following park information:");
                
                System.out.print("Enter street address: ");
                street = scanner.nextLine().trim();
                
                System.out.print("Enter city: ");
                city = scanner.nextLine().trim();
                
                System.out.print("Enter state: ");
                state = scanner.nextLine().trim();
                
                System.out.print("Enter postal code: ");
                postalCode = scanner.nextLine().trim();
                
                System.out.print("Enter establishment date (YYYY-MM-DD): ");
                establishmentDate = scanner.nextLine().trim();
                
                System.out.print("Enter capacity: ");
                String capacityStr = scanner.nextLine().trim();
                capacity = Integer.parseInt(capacityStr);
            } else {
                System.out.println("Park '" + parkName + "' found in database.");
            }
            
            // SQL queries
            String insertParkSQL = 
                "INSERT INTO National_parks(Name, Street, City, State, Postal_code, Establishment_date, Capacity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            String insertProgramSQL = 
                "INSERT INTO Program(program_name, type, start_date, duration) " +
                "VALUES (?, ?, ?, ?)";
            
            String linkParkProgramSQL = 
                "INSERT INTO National_parks_offers_program(park_name, program_name) " +
                "VALUES (?, ?)";
            
            // Executing
            connection.setAutoCommit(false);
            
            try {
                // Create park if it doesn't exist
                if (parkNeedsCreation) {
                    try (PreparedStatement pstmtPark = connection.prepareStatement(insertParkSQL)) {
                        pstmtPark.setString(1, parkName);
                        pstmtPark.setString(2, street);
                        pstmtPark.setString(3, city);
                        pstmtPark.setString(4, state);
                        pstmtPark.setString(5, postalCode);
                        pstmtPark.setDate(6, java.sql.Date.valueOf(establishmentDate));
                        pstmtPark.setInt(7, capacity);
                        
                        int parkRows = pstmtPark.executeUpdate();
                        System.out.println("Park '" + parkName + "' created successfully! (Rows affected: " + parkRows + ")");
                    }
                }
                
                // Create program
                try (PreparedStatement pstmtProgram = connection.prepareStatement(insertProgramSQL)) {
                    pstmtProgram.setString(1, programName);
                    pstmtProgram.setString(2, programType);
                    pstmtProgram.setDate(3, java.sql.Date.valueOf(startDate));
                    pstmtProgram.setInt(4, duration);
                    
                    int rows1 = pstmtProgram.executeUpdate();
                    
                    // Link program to park
                    try (PreparedStatement pstmtLink = connection.prepareStatement(linkParkProgramSQL)) {
                        pstmtLink.setString(1, parkName);
                        pstmtLink.setString(2, programName);
                        
                        int rows2 = pstmtLink.executeUpdate();
                        
                        connection.commit();
                        System.out.println("Park program inserted successfully! (Program rows: " + rows1 + ", Link rows: " + rows2 + ")");
                        
                        // Verify the insert
                        String verifySQL = "SELECT p.program_name, p.type, np.name as park_name " +
                                          "FROM Program p " +
                                          "INNER JOIN National_parks_offers_program npop ON p.program_name = npop.program_name " +
                                          "INNER JOIN National_parks np ON npop.park_name = np.name " +
                                          "WHERE p.program_name = ?";
                        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySQL)) {
                            verifyStmt.setString(1, programName);
                            try (ResultSet rs = verifyStmt.executeQuery()) {
                                if (rs.next()) {
                                    System.out.println("VERIFIED: Program '" + programName + "' is now linked to park '" + 
                                                     rs.getString("park_name") + "' in the database");
                                } else {
                                    System.out.println("WARNING: Program '" + programName + "' not found after insert!");
                                }
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

