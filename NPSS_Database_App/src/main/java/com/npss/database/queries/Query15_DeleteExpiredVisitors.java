package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Query 15: Delete visitors who have not enrolled in any park programs and whose park passes have expired
 * 
 * This query leverages the following indexes for optimal performance:
 * - IX_visitor_holds_park_passes_visitor_id_number (for joining with Visitor)
 * - IX_visitor_holds_park_passes_pass_id (for joining with Park_passes)
 * - IX_park_passes_expiration_date (for filtering expired passes)
 * - IX_visitor_enrolls_program_visitor_id_number (for checking program enrollment)
 * - Clustered index on Visitor.id_number (for deletion)
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class Query15_DeleteExpiredVisitors {
    private Connection connection;
    private Scanner scanner;

    public Query15_DeleteExpiredVisitors(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes Query 15: Delete visitors who have not enrolled in any park programs and whose park passes have expired
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Query 15] Delete visitors who have not enrolled in any park programs and whose park passes have expired");
        
        try {
            // SQL query - Delete visitors with expired passes who are not enrolled in programs
            // Uses subquery to find eligible visitors for deletion
            String SQL = 
                "DELETE FROM Visitor " +
                "WHERE id_number IN (" +
                    "SELECT DISTINCT v.id_number " +
                    "FROM Visitor v " +
                    "INNER JOIN Visitor_holds_park_passes vhpp ON v.id_number = vhpp.visitor_id_number " +
                    "INNER JOIN Park_passes pp ON vhpp.pass_id = pp.pass_id " +
                    "WHERE pp.expiration_date < GETDATE() " +
                    "AND v.id_number NOT IN (" +
                        "SELECT visitor_id_number FROM Visitor_enrolls_program" +
                    ")" +
                ")";
            
            // Executing
            connection.setAutoCommit(false);
            
            PreparedStatement pstmt = connection.prepareStatement(SQL);
            
            try {
                int rowsAffected = pstmt.executeUpdate();
                connection.commit();
                
                if (rowsAffected > 0) {
                    System.out.println("Deleted " + rowsAffected + " expired visitor(s) successfully!");
                } else {
                    System.out.println("No expired visitors found to delete.");
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

