package com.npss.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConnectDatabase {
    private static final String DB_URL = "DB_URL";
    private static final String DB_USERNAME = "DB_USERNAME";
    private static final String DB_PASSWORD  = "DB_PASSWORD";
    private static Map<String, String> envCache = null;

    /**
     * Loads environment variables from .env file if it exists
     * @return Map of environment variables from .env file
     */
    private static Map<String, String> loadEnvFile() {
        if (envCache != null) {
            return envCache;
        }
        
        Map<String, String> envMap = new HashMap<>();
        try {
            // Try multiple locations to find .env file
            Path envPath = null;
            
            // 1. Try current working directory
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            envPath = currentDir.resolve(".env");
            
            // 2. If not found, try relative to the class location (for IDE runs)
            if (!envPath.toFile().exists()) {
                try {
                    java.net.URL classUrl = ConnectDatabase.class.getResource("/");
                    if (classUrl != null) {
                        String classPath = classUrl.getPath();
                        // Handle URL-encoded paths
                        if (classPath.startsWith("file:")) {
                            classPath = classPath.substring(5);
                        }
                        // Decode URL encoding
                        classPath = URLDecoder.decode(classPath, "UTF-8");
                        Path classDir = Paths.get(classPath);
                        // Navigate up from target/classes to project root
                        if (classDir.toString().contains("target")) {
                            envPath = classDir.getParent().getParent().resolve(".env");
                        } else {
                            envPath = classDir.resolve(".env");
                        }
                    }
                } catch (Exception e) {
                    // If we can't determine class location, continue with other paths
                }
            }
            
            // 3. Try relative paths from current directory
            if (envPath == null || !envPath.toFile().exists()) {
                envPath = Paths.get(".env");
            }
            if (!envPath.toFile().exists()) {
                envPath = Paths.get("..", ".env");
            }
            if (!envPath.toFile().exists()) {
                envPath = Paths.get("..", "..", ".env");
            }
            
            // 4. Try in NPSS_Database_App directory
            if (!envPath.toFile().exists()) {
                Path projectRoot = currentDir;
                if (currentDir.getFileName().toString().equals("NPSS_Database_App")) {
                    envPath = projectRoot.resolve(".env");
                } else {
                    envPath = projectRoot.resolve("NPSS_Database_App").resolve(".env");
                }
            }
            
            if (envPath != null && envPath.toFile().exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        // Skip empty lines and comments
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            String key = line.substring(0, equalsIndex).trim();
                            String value = line.substring(equalsIndex + 1).trim();
                            if (!key.isEmpty() && !value.isEmpty()) {
                                envMap.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // If .env file doesn't exist or can't be read, that's okay
            // We'll fall back to system environment variables
        }
        
        envCache = envMap;
        return envMap;
    }

    /**
     * Gets configuration value from environment variables (.env file or system env)
     * Checks .env file first, then falls back to system environment variables
     * @param envKey The env variable key
     * @param description Description of the error messages 
     * @return The env variable value 
     * @throws SQLException If the environment variable is not set
     */
    private static String getEnvValue(String envKey, String description)throws SQLException{
        // First, try to load from .env file
        Map<String, String> envFile = loadEnvFile();
        String value = envFile.get(envKey);
        
        // If not found in .env file, try system environment variables
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }

        if(value == null || value.trim().isEmpty() ){
            throw new SQLException(
                String.format("Missing required environment variable: %s (%s). Please set this in your .env file or system environment variables.", envKey, description)
            );
        }
        return value.trim();
    }

    /**
     * Creates and returns a secure database connection using environment variables 
     * @return Connection object to the database 
     * @throws SQLException If the environment variable is not set
     */
    public static Connection getConnection() throws SQLException{
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String url = getEnvValue(DB_URL, "Database connection URL");
            String username = getEnvValue(DB_USERNAME, "Database username");
            String password = getEnvValue(DB_PASSWORD, "Database password");

            // Create Connection
            Connection connection = DriverManager.getConnection(url,username,password);

            if(connection != null && !connection.isClosed()){
                System.out.println("Database connect successfully!");
                return connection;
            }else{
                throw new SQLException("Failed to connect the database. Try again.");
            }

        }catch(ClassNotFoundException e){
            throw new SQLException("JDBC Driver not found. Make sure mssql-jdbc is in your classpath.", e);
        }catch(SQLException e){
            System.err.println("SQL Exception occurred:");
            System.err.println("  Message: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getErrorCode() != 0) {
                System.err.println("Error Code: " + e.getErrorCode());
            }
            throw e;
        }
    }

    /**
     * Closes a database connection 
     * @param connection The connection to close 
     */
    public static void closeConnection(Connection connection){
        if(connection != null){
            try {
                if(!connection.isClosed()){
                    connection.close();
                    System.out.println("Database connection closed!");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

}


