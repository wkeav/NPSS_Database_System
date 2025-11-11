package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 14: Update the salary of researchers overseeing more than one ranger team by a 3% increase
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_researcher_reports_ranger_team_researcher_id_number (for grouping by researcher)
 * - IX_researcher_reports_ranger_team_team_id (for counting teams)
 * - Clustered index on Researcher.id_number (for updating salary)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query14_UpdateResearcherSalary {
    private Connection connection;
    private Scanner scanner;

    public Query14_UpdateResearcherSalary(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 14: Update the salary of researchers overseeing more than one ranger team by a 3% increase
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 14] Update the salary of researchers overseeing more than one ranger team by a 3% increase");
        
        try {
            // SQL query - Update salary by 3% for researchers overseeing more than one team
            // Uses subquery to find researchers with COUNT(team_id) > 1
            String SQL = 
                "UPDATE Researcher " +
                "SET salary = salary * 1.03 " +
                "WHERE id_number IN (" +
                    "SELECT researcher_id_number " +
                    "FROM Researcher_reports_ranger_team " +
                    "GROUP BY researcher_id_number " +
                    "HAVING COUNT(DISTINCT team_id) > 1" +
                ")";
            
            // Executing
            connection.setAutoCommit(false);
            
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            
            try {
                int rowsAffected = pstmt.executeUpdate();
                connection.commit();
                
                if (rowsAffected > 0) {
                    System.out.println("Updated salary for " + rowsAffected + " researcher(s) successfully!");
                    System.out.println("Salary increased by 3% for researchers overseeing more than one ranger team.");
                } else {
                    System.out.println("No researchers found overseeing more than one ranger team.");
                }
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
                pstmt.close();
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

