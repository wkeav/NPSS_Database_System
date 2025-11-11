package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 6: Insert a new report submitted by a ranger team to a researcher
 * If a report already exists for this researcher-team pair, it will be updated
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query6_InsertReport {
    private Connection connection;
    private Scanner scanner;

    public Query6_InsertReport(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 6: Insert a report submitted by a ranger team to a researcher
     * Uses UPDATE if report exists, INSERT if it doesn't (UPSERT pattern)
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 6] Insert a report submitted by a ranger team to a researcher");
        
        try {
            // User's input
            System.out.print("Enter researcher ID number: ");
            String researcherId = scanner.nextLine().trim();
            
            System.out.print("Enter ranger team ID: ");
            String teamId = scanner.nextLine().trim();
            
            System.out.print("Enter report date (YYYY-MM-DD): ");
            String reportDate = scanner.nextLine().trim();
            
            System.out.print("Enter summary (or press Enter for NULL): ");
            String summary = scanner.nextLine().trim();
            if (summary.isEmpty()) summary = null;
            
            // SQL queries 
            // SQL Server MERGE statement
            String mergeReportSQL = 
                "MERGE Researcher_reports_ranger_team AS target " +
                "USING (SELECT ? AS researcher_id_number, ? AS team_id) AS source " +
                "ON target.researcher_id_number = source.researcher_id_number " +
                "   AND target.team_id = source.team_id " +
                "WHEN MATCHED THEN " +
                "    UPDATE SET date = ?, summary = ? " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (researcher_id_number, team_id, date, summary) " +
                "    VALUES (?, ?, ?, ?);";
            
            // Executing
            connection.setAutoCommit(false);
            
            try (PreparedStatement pstmtReport = connection.prepareStatement(mergeReportSQL)) {
                // Parameters for MERGE: source (2), update (2), insert (4)
                pstmtReport.setString(1, researcherId);  // source researcher_id
                pstmtReport.setString(2, teamId);        // source team_id
                pstmtReport.setDate(3, java.sql.Date.valueOf(reportDate));  // update date
                if (summary != null) {
                    pstmtReport.setString(4, summary);    // update summary
                } else {
                    pstmtReport.setNull(4, java.sql.Types.VARCHAR);
                }
                pstmtReport.setString(5, researcherId);  // insert researcher_id
                pstmtReport.setString(6, teamId);        // insert team_id
                pstmtReport.setDate(7, java.sql.Date.valueOf(reportDate));  // insert date
                if (summary != null) {
                    pstmtReport.setString(8, summary);    // insert summary
                } else {
                    pstmtReport.setNull(8, java.sql.Types.VARCHAR);
                }
                
                int rowsAffected = pstmtReport.executeUpdate();
                connection.commit();
                
                if (rowsAffected > 0) {
                    System.out.println("Report inserted/updated successfully! (Rows affected: " + rowsAffected + ")");
                    
                    // Verify the report
                    String verifySQL = "SELECT researcher_id_number, team_id, date, summary " +
                                      "FROM Researcher_reports_ranger_team " +
                                      "WHERE researcher_id_number = ? AND team_id = ?";
                    try (PreparedStatement verifyStmt = connection.prepareStatement(verifySQL)) {
                        verifyStmt.setString(1, researcherId);
                        verifyStmt.setString(2, teamId);
                        try (ResultSet rs = verifyStmt.executeQuery()) {
                            if (rs.next()) {
                                System.out.println("VERIFIED: Report for researcher " + researcherId + 
                                                 " and team " + teamId + " is in the database");
                                System.out.println("  Date: " + rs.getDate("date"));
                                System.out.println("  Summary: " + (rs.getString("summary") != null ? rs.getString("summary") : "NULL"));
                            }
                        }
                    }
                } else {
                    System.out.println("No rows affected. Report may not have been inserted/updated.");
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

