package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 12: Retrieve the list of rangers in a team, including their certifications, years of service and their role in the team
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_ranger_assigned_ranger_team_team_id (for filtering by team)
 * - IX_ranger_assigned_ranger_team_ranger_id_number (for joining with Ranger)
 * - IX_ranger_certifications_id_number (for retrieving certifications)
 * - Clustered index on Individual.id_number (for joining with Individual)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query12_RetrieveRangersInTeam {
    private Connection connection;
    private Scanner scanner;

    public Query12_RetrieveRangersInTeam(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 12: Retrieve the list of rangers in a team, including their certifications, years of service and their role in the team
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 12] Retrieve the list of rangers in a team, including their certifications, years of service and their role in the team");
        
        try {
            // Get user input
            System.out.print("Enter the team ID: ");
            String teamId = scanner.nextLine().trim();
            
            if (teamId.isEmpty()) {
                System.out.println("Error: Team ID cannot be empty.");
                return;
            }
            
            // SQL query - Retrieve rangers in team with certifications, years of service, and status
            // Uses LEFT JOIN for certifications since a ranger may have no certifications
            String SQL = 
                "SELECT DISTINCT " +
                "    i.id_number, " +
                "    i.first_name, " +
                "    i.last_name, " +
                "    CONCAT(i.first_name, ' ', i.last_name) AS full_name, " +
                "    rart.status, " +
                "    rart.years_of_service, " +
                "    rc.certification " +
                "FROM Ranger_assigned_ranger_team rart " +
                "INNER JOIN Ranger r ON rart.ranger_id_number = r.id_number " +
                "INNER JOIN Individual i ON r.id_number = i.id_number " +
                "LEFT JOIN Ranger_certifications rc ON r.id_number = rc.id_number " +
                "WHERE rart.team_id = ? " +
                "ORDER BY i.last_name, i.first_name, rc.certification";
            
            // Fill in the variables
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            pstmt.setString(1, teamId);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Display results
            boolean hasResults = false;
            String currentRangerId = null;
            String currentRangerName = null;
            String currentStatus = null;
            Integer currentYearsOfService = null;
            boolean firstRanger = true;
            boolean firstCertification = true;
    
            System.out.println("Rangers in Team: " + teamId);
            while (rs.next()) {
                String rangerId = rs.getString("id_number");
                
                // If this is a new ranger, display ranger info
                if (currentRangerId == null || !rangerId.equals(currentRangerId)) {
                    if (!firstRanger) {
                        System.out.println(); // New line after certifications
                    }
                    firstRanger = false;
                    firstCertification = true;
                    hasResults = true;
                    
                    currentRangerId = rangerId;
                    currentRangerName = rs.getString("full_name");
                    currentStatus = rs.getString("status");
                    currentYearsOfService = rs.getInt("years_of_service");
                    
                    System.out.println("ID Number: " + currentRangerId);
                    System.out.println("Name: " + currentRangerName);
                    System.out.println("Status (Role): " + currentStatus);
                    System.out.println("Years of Service: " + currentYearsOfService);
                    System.out.print("Certifications: ");
                }
                
                // Display certification 
                // May not have any certifications 
                String certification = rs.getString("certification");
                if (certification != null && !certification.isEmpty()) {
                    if (firstCertification) {
                        // First certification for this ranger
                        System.out.print(certification);
                        firstCertification = false;
                    } else {
                        // Additional certification 
                        System.out.print(", " + certification);
                    }
                } else {
                    // No certifications found 
                    if (firstCertification) {
                        System.out.print("None");
                        firstCertification = false;
                    }
                }
            }
            
            if (hasResults) {
                System.out.println(); // New line after last certification
            } else {
                System.out.println("No rangers found in team: " + teamId);
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

