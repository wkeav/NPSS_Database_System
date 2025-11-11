package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 5: Insert a new researcher into the database and associate them with one or more ranger teams
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query5_InsertResearcher {
    private Connection connection;
    private Scanner scanner;

    public Query5_InsertResearcher(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 5: Insert a new researcher into the database and associate them with one or more ranger teams
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 5] Insert a new researcher into the database and associate them with one or more ranger teams");
        
        try {
            // User's input
            System.out.print("Enter researcher ID number: ");
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
            
            System.out.print("Enter research field: ");
            String researchField = scanner.nextLine().trim();
            
            System.out.print("Enter hire date (YYYY-MM-DD): ");
            String hireDate = scanner.nextLine().trim();
            
            System.out.print("Enter salary: ");
            String salaryStr = scanner.nextLine().trim();
            double salary = Double.parseDouble(salaryStr);
            
            System.out.print("How many ranger teams to associate? (1 or more): ");
            int teamCount = Integer.parseInt(scanner.nextLine().trim());
            
            // SQL queries
            String insertIndividualSQL = 
                "INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, " +
                "postal_code, date_of_birth, newsletter_status) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            String insertResearcherSQL = 
                "INSERT INTO Researcher(id_number, research_field, hire_date, salary) " +
                "VALUES (?, ?, ?, ?)";
            
            // Set parameters
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
            
            PreparedStatement pstmtResearcher = connection.prepareStatement(insertResearcherSQL);
            pstmtResearcher.setString(1, idNumber);
            pstmtResearcher.setString(2, researchField);
            pstmtResearcher.setDate(3, java.sql.Date.valueOf(hireDate));
            pstmtResearcher.setDouble(4, salary);
            
            // Executing
            connection.setAutoCommit(false);
            
            try {
                pstmtIndividual.executeUpdate();
                pstmtResearcher.executeUpdate();
                
                // Associate with ranger teams
                String associateTeamSQL = 
                    "INSERT INTO Researcher_reports_ranger_team(researcher_id_number, team_id, date, summary) " +
                    "VALUES (?, ?, ?, ?)";
                PreparedStatement pstmtAssociate = connection.prepareStatement(associateTeamSQL);
                
                for (int i = 0; i < teamCount; i++) {
                    System.out.print("Enter team ID " + (i + 1) + ": ");
                    String teamId = scanner.nextLine().trim();
                    
                    System.out.print("Enter report date (YYYY-MM-DD): ");
                    String reportDate = scanner.nextLine().trim();
                    
                    System.out.print("Enter summary (or press Enter for NULL): ");
                    String summary = scanner.nextLine().trim();
                    if (summary.isEmpty()) summary = null;
                    
                    pstmtAssociate.setString(1, idNumber);
                    pstmtAssociate.setString(2, teamId);
                    pstmtAssociate.setDate(3, java.sql.Date.valueOf(reportDate));
                    if (summary != null) {
                        pstmtAssociate.setString(4, summary);
                    } else {
                        pstmtAssociate.setNull(4, java.sql.Types.VARCHAR);
                    }
                    pstmtAssociate.executeUpdate();
                }
                pstmtAssociate.close();
                
                connection.commit();
                System.out.println("Researcher inserted and associated with teams successfully!");
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
                pstmtIndividual.close();
                pstmtResearcher.close();
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


