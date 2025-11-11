package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 3: Insert a new ranger team into the database and set its leader
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query3_InsertRangerTeam {
    private Connection connection;
    private Scanner scanner;

    public Query3_InsertRangerTeam(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 3: Insert a new ranger team into the database and set its leader
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 3] Insert a new ranger team into the database and set its leader");
        
        try {
            // User's input
            System.out.print("Enter team ID: ");
            String teamId = scanner.nextLine().trim();
            
            System.out.print("Enter formation date (YYYY-MM-DD): ");
            String formationDate = scanner.nextLine().trim();
            
            System.out.print("Enter focus date (YYYY-MM-DD) or press Enter for NULL: ");
            String focusDate = scanner.nextLine().trim();
            if (focusDate.isEmpty()) focusDate = null;
            
            System.out.print("Enter team leader ID (ranger ID) or press Enter for NULL: ");
            String teamLeader = scanner.nextLine().trim();
            if (teamLeader.isEmpty()) teamLeader = null;
            
            // SQL queries
            String insertTeamSQL = 
                "INSERT INTO Ranger_team(team_id, formation_date, focus_date, team_leader) " +
                "VALUES (?, ?, ?, ?)";
            
            // Set parameters
            PreparedStatement pstmtTeam = connection.prepareStatement(insertTeamSQL);
            pstmtTeam.setString(1, teamId);
            pstmtTeam.setDate(2, java.sql.Date.valueOf(formationDate));
            if (focusDate != null) {
                pstmtTeam.setDate(3, java.sql.Date.valueOf(focusDate));
            } else {
                pstmtTeam.setNull(3, java.sql.Types.DATE);
            }
            if (teamLeader != null) {
                pstmtTeam.setString(4, teamLeader);
            } else {
                pstmtTeam.setNull(4, java.sql.Types.VARCHAR);
            }
            
            // Executing
            connection.setAutoCommit(false);
            
            try {
                int rowsAffected = pstmtTeam.executeUpdate();
                connection.commit();
                System.out.println("Ranger team inserted successfully! (Rows affected: " + rowsAffected + ")");
                
                // Verify the insert
                String verifySQL = "SELECT team_id FROM Ranger_team WHERE team_id = ?";
                try (PreparedStatement verifyStmt = connection.prepareStatement(verifySQL)) {
                    verifyStmt.setString(1, teamId);
                    try (ResultSet rs = verifyStmt.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("✓ VERIFIED: Team " + teamId + " is now in the database");
                        } else {
                            System.out.println("⚠ WARNING: Team " + teamId + " not found after insert!");
                        }
                    }
                }
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
                pstmtTeam.close();
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

