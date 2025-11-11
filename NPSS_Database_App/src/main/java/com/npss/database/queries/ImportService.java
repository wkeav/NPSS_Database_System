package com.npss.database.queries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Import Service: Enter new teams from a data file until the file is empty
 * 
 * Expected file format (CSV, one team per line):
 * team_id,formation_date,focus_date,team_leader
 * 
 * Note: Empty values for focus_date or team_leader will be treated as NULL
 * 
 * @author Astra Nguyen
 * @version 1.0
 */
public class ImportService {
    private Connection connection;
    private Scanner scanner;

    public ImportService(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Executes the import functionality: Enter new teams from a data file until the file is empty
     * 
     * @throws SQLException if a database error occurs
     */
    public void execute() throws SQLException {
        System.out.println("\n[Import] Enter new teams from a data file until the file is empty");
        System.out.print("Please enter the input file name: ");
        String inputFileName = scanner.nextLine().trim();
        
        if (inputFileName.isEmpty()) {
            System.out.println("Error: File name cannot be empty.");
            return;
        }

        // Try to find the file in current directory or project root 
        Path filePath = findFile(inputFileName);
        
        if (filePath == null || !Files.exists(filePath)) {
            System.err.println("Error: File '" + inputFileName + "' not found.");
            System.err.println("Please ensure the file exists in the current directory or project root.");
            return;
        }
        
        System.out.println("Reading from file: " + filePath.toAbsolutePath());
        
        // SQL query for inserting teams
        String insertTeamSQL = 
            "INSERT INTO Ranger_team(team_id, formation_date, focus_date, team_leader) " +
            "VALUES (?, ?, ?, ?)";
        
        int totalLines = 0;
        int successCount = 0;
        int errorCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                totalLines++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                // Skip header line if present
                if (isFirstLine && line.toLowerCase().startsWith("team_id")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                // Parse CSV line
                String[] parts = parseCSVLine(line);
                
                if (parts.length < 2) {
                    System.err.println("Line " + totalLines + ": Invalid format (expected at least 2 fields). Skipping: " + line);
                    errorCount++;
                    continue;
                }
                
                String teamId = parts[0].trim();
                String formationDateStr = parts[1].trim();
                String focusDateStr = (parts.length > 2 && !parts[2].trim().isEmpty()) ? parts[2].trim() : null;
                String teamLeader = (parts.length > 3 && !parts[3].trim().isEmpty()) ? parts[3].trim() : null;
                
                // Validate required fields
                if (teamId.isEmpty() || formationDateStr.isEmpty()) {
                    System.err.println("Line " + totalLines + ": Missing required fields (team_id or formation_date). Skipping: " + line);
                    errorCount++;
                    continue;
                }
                
                // Insert team
                try {
                    connection.setAutoCommit(false);
                    
                    try (PreparedStatement pstmt = connection.prepareStatement(insertTeamSQL)) {
                        pstmt.setString(1, teamId);
                        pstmt.setDate(2, java.sql.Date.valueOf(formationDateStr));
                        
                        if (focusDateStr != null && !focusDateStr.isEmpty()) {
                            pstmt.setDate(3, java.sql.Date.valueOf(focusDateStr));
                        } else {
                            pstmt.setNull(3, java.sql.Types.DATE);
                        }
                        
                        if (teamLeader != null && !teamLeader.isEmpty()) {
                            pstmt.setString(4, teamLeader);
                        } else {
                            pstmt.setNull(4, java.sql.Types.VARCHAR);
                        }
                        
                        pstmt.executeUpdate();
                        connection.commit();
                        
                        System.out.println("Imported team: " + teamId + " (formation: " + formationDateStr + ")");
                        successCount++;
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    System.err.println("Line " + totalLines + ": Failed to import team '" + teamId + "': " + e.getMessage());
                    errorCount++;
                } catch (IllegalArgumentException e) {
                    connection.rollback();
                    System.err.println("Line " + totalLines + ": Invalid date format for team '" + teamId + "': " + e.getMessage());
                    System.err.println("   Expected format: YYYY-MM-DD");
                    errorCount++;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
            
            // Display summary result 
            System.out.println("Total lines processed: " + totalLines);
            System.out.println("Successfully imported: " + successCount);
            System.out.println("Errors: " + errorCount);
            
            if (successCount > 0) {
                System.out.println("\nImport completed successfully!");
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            throw new SQLException("File I/O error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Finds the file in current directory or project root
     */
    private Path findFile(String fileName) {
        // Try current directory first
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentDir.resolve(fileName);

        if (Files.exists(filePath)) {
            return filePath;
        }

        // Try project root first
        if (currentDir.getFileName().toString().equals("NPSS_Database_App")) {
            return filePath;
        } else {
            // Try going up one level to find NPSS_Database_App
            Path projectRoot = currentDir.resolve("NPSS_Database_App");
            if (Files.exists(projectRoot)) {
                Path projectFile = projectRoot.resolve(fileName);
                if (Files.exists(projectFile)) {
                    return projectFile;
                }
            }
        }
        return filePath;
    }
    
    /**
     * Parses a CSV line, handling quoted fields
     */
    private String[] parseCSVLine(String line) {
        // Simple CSV parser - splits by comma, trims whitespace
        // For more complex CSV (with quoted fields containing commas), use a CSV library
        return line.split(",");
    }
}


