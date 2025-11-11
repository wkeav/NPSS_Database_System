package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 2: Insert a new ranger into the database and assign them to a ranger team
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query2_InsertRanger {
    private Connection connection;
    private Scanner scanner;

    public Query2_InsertRanger(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 2: Insert a new ranger into the database and assign them to a ranger team
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 2] Insert a new ranger into the database and assign them to a ranger team");
        
        try {
            // User's input 
            System.out.print("Enter ranger ID number: ");
            String idNumber = scanner.nextLine().trim();
            
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine().trim();
            
            System.out.print("Enter gender (M/F/O): ");
            String gender = scanner.nextLine().trim();
            
            System.out.print("Enter street address: ");
            String street = scanner.nextLine().trim();
            
            System.out.print("Enter city: ");
            String city = scanner.nextLine().trim();
            
            System.out.print("Enter state: ");
            String state = scanner.nextLine().trim();
            
            System.out.print("Enter postal code: ");
            String postalCode = scanner.nextLine().trim();
            
            System.out.print("Enter date of birth (YYYY-MM-DD): ");
            String dateOfBirth = scanner.nextLine().trim();
            
            System.out.print("Subscribe to newsletter? (true/false): ");
            boolean newsletterStatus = Boolean.parseBoolean(scanner.nextLine().trim());
            
            // Team assignment information
            System.out.print("Enter team ID to assign ranger to: ");
            String teamId = scanner.nextLine().trim();
            
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine().trim();
            
            System.out.print("Enter status (e.g., Active, On Leave, etc.): ");
            String status = scanner.nextLine().trim();
            
            // Optional certifications
            System.out.print("How many certifications? (0 or more): ");
            int certCount = Integer.parseInt(scanner.nextLine().trim());
            
            // SQL queries 
            String insertIndividualSQL = 
                "INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, " +
                "postal_code, date_of_birth, newsletter_status) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            String insertRangerSQL = 
                "INSERT INTO Ranger(id_number) " + 
                "VALUES (?)";
            
            String assignTeamSQL = 
                "INSERT INTO Ranger_assigned_ranger_team(ranger_id_number, team_id, start_date, status) " +
                "VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmtIndividual = connection.prepareStatement(insertIndividualSQL);
            pstmtIndividual.setString(1, idNumber);
            pstmtIndividual.setString(2, firstName);
            pstmtIndividual.setString(3, lastName);
            pstmtIndividual.setString(4, gender);
            pstmtIndividual.setString(5, street);
            pstmtIndividual.setString(6, city);
            pstmtIndividual.setString(7, state);
            pstmtIndividual.setString(8, postalCode);
            pstmtIndividual.setDate(9, java.sql.Date.valueOf(dateOfBirth));
            pstmtIndividual.setBoolean(10, newsletterStatus);
            
            PreparedStatement pstmtRanger = connection.prepareStatement(insertRangerSQL);
            pstmtRanger.setString(1, idNumber);
            
            PreparedStatement pstmtAssignTeam = connection.prepareStatement(assignTeamSQL);
            pstmtAssignTeam.setString(1, idNumber);
            pstmtAssignTeam.setString(2, teamId);
            pstmtAssignTeam.setDate(3, java.sql.Date.valueOf(startDate));
            pstmtAssignTeam.setString(4, status);
            
            // Excuting 
            connection.setAutoCommit(false); // Start transaction
            
            try {
                pstmtIndividual.executeUpdate();
                pstmtRanger.executeUpdate();
                pstmtAssignTeam.executeUpdate();
                
                // Add certifications if any
                if (certCount > 0) {
                    String insertCertSQL = 
                        "INSERT INTO Ranger_certifications(id_number, certification) " +
                        "VALUES (?, ?)";
                    PreparedStatement pstmtCert = connection.prepareStatement(insertCertSQL);
                    
                    for (int i = 0; i < certCount; i++) {
                        System.out.print("Enter certification " + (i + 1) + ": ");
                        String certification = scanner.nextLine().trim();
                        pstmtCert.setString(1, idNumber);
                        pstmtCert.setString(2, certification);
                        pstmtCert.executeUpdate();
                    }
                    pstmtCert.close();
                }
                
                connection.commit(); // Commit transaction
                System.out.println("Ranger inserted and assigned to team successfully!");
                
            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
                throw e; // Re-throw to be caught by outer catch
            } finally {
                connection.setAutoCommit(true); // Reset auto-commit
                pstmtIndividual.close();
                pstmtRanger.close();
                pstmtAssignTeam.close();
            }
            
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

