package com.npss.database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import com.npss.database.ConnectDatabase;
import com.npss.database.queries.*;


/**
 * National Park Service System (NPSS) Database Application
 * 
 * This application provides a menu-driven interface to interact with the NPSS database
 * using JDBC and Azure SQL Database. It supports 15 queries, import/export functionality,
 * and proper error handling.
 * 
 * @author Astra Nguyen 
 * @version 1.0
 */

public class NPSS_DBApp {
    private Connection connection; 
    private Scanner scanner;

    /*
     * Initial Constructor 
     */
    public NPSS_DBApp(){
        scanner = new Scanner(System.in);
    }

    /**
     * Check to see if connect to Azure SQL Database 
     * @return true if connection is successful, false otherwise 
     * @throws SQLException If the environment variable is not set
     */
    public boolean connectToDatabase()throws SQLException{
        try{
            this.connection = ConnectDatabase.getConnection();
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("Database connected successfully!");
                return true;
            }
        }catch(SQLException e){
            System.err.println("SQL Exception occurred:");
            System.err.println("  Message: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("SQL State: " + e.getSQLState());
            }
            if (e.getErrorCode() != 0) {
                System.err.println("Error Code: " + e.getErrorCode());
            }
            return false; 
        }
        return false;
    }

    /**
     * Displaying the NPPS Menu 
     */
    public void displayMenu(){
        System.out.println("\n" + "=".repeat(60));
        System.out.println("WELCOME TO THE NATIONAL PARK SERVICE SYSTEM DATABASE(NPSS)!");
        System.out.println("=".repeat(60));
        System.out.println("(1)  Insert a new visitor into the database and associate them with one or more park programs");
        System.out.println("(2)  Insert a new ranger into the database and assign them to a ranger team");
        System.out.println("(3)  Insert a new ranger team into the database and set its leader");
        System.out.println("(4)  Insert a new donation from a donor");
        System.out.println("(5)  Insert a new researcher into the database and associate them with one or more ranger teams");
        System.out.println("(6)  Insert a report submitted by a ranger team to a researcher");
        System.out.println("(7)  Insert a new park program into the database for a specific park");
        System.out.println("(8)  Retrieve the names and contact information of all emergency contacts for a specific person");
        System.out.println("(9)  Retrieve the list of visitors enrolled in a specific park program, including their accessibility needs");
        System.out.println("(10) Retrieve all park programs for a specific park that started after a given date");
        System.out.println("(11) Retrieve the total and average donation amount received in a month from all anonymous donors");
        System.out.println("(12) Retrieve the list of rangers in a team, including their certifications, years of service and their role in the team");
        System.out.println("(13) Retrieve the names, IDs, contact information, and newsletter subscription status of all individuals in the database");
        System.out.println("(14) Update the salary of researchers overseeing more than one ranger team by a 3% increase");
        System.out.println("(15) Delete visitors who have not enrolled in any park programs and whose park passes have expired");
        System.out.println("(16) Import: Enter new teams from a data file until the file is empty");
        System.out.println("(17) Export: Retrieve names and mailing addresses of all people on the mailing list");
        System.out.println("(18) Quit");
        System.out.println("=".repeat(60));
        System.out.print("Please select an option (1-18): ");
    }

    /**
     * Processes the user's menu choice and executes the corresponding action
     * @param choice The menu option selected by the user (1-18)
     */
    public void processMenuChoice(int choice){
        try {
            switch(choice){
                case 1:
                    Query1_InsertVisitor query1 = new Query1_InsertVisitor(connection, scanner);
                    query1.execute();
                    break;
                case 2:
                    Query2_InsertRanger query2 = new Query2_InsertRanger(connection, scanner);
                    query2.execute();
                    break;
                case 3:
                    Query3_InsertRangerTeam query3 = new Query3_InsertRangerTeam(connection, scanner);
                    query3.execute();
                    break;
                case 4:
                    Query4_InsertDonation query4 = new Query4_InsertDonation(connection, scanner);
                    query4.execute();
                    break;
                case 5:
                    Query5_InsertResearcher query5 = new Query5_InsertResearcher(connection, scanner);
                    query5.execute();
                    break;
                case 6:
                    Query6_InsertReport query6 = new Query6_InsertReport(connection, scanner);
                    query6.execute();
                    break;
                case 7:
                    Query7_InsertParkProgram query7 = new Query7_InsertParkProgram(connection, scanner);
                    query7.execute();
                    break;
                case 8:
                    Query8_RetrieveEmergencyContacts query8 = new Query8_RetrieveEmergencyContacts(connection, scanner);
                    query8.execute();
                    break;
                case 9:
                    Query9_RetrieveVisitorsInProgram query9 = new Query9_RetrieveVisitorsInProgram(connection, scanner);
                    query9.execute();
                    break;
                case 10:
                    Query10_RetrieveParkPrograms query10 = new Query10_RetrieveParkPrograms(connection, scanner);
                    query10.execute();
                    break;
                case 11:
                    Query11_RetrieveDonationStats query11 = new Query11_RetrieveDonationStats(connection, scanner);
                    query11.execute();
                    break;
                case 12:
                    Query12_RetrieveRangersInTeam query12 = new Query12_RetrieveRangersInTeam(connection, scanner);
                    query12.execute();
                    break;
                case 13:
                    Query13_RetrieveAllIndividuals query13 = new Query13_RetrieveAllIndividuals(connection, scanner);
                    query13.execute();
                    break;
                case 14:
                    Query14_UpdateResearcherSalary query14 = new Query14_UpdateResearcherSalary(connection, scanner);
                    query14.execute();
                    break;
                case 15:
                    Query15_DeleteExpiredVisitors query15 = new Query15_DeleteExpiredVisitors(connection, scanner);
                    query15.execute();
                    break;
                case 16:
                    ImportService importService = new ImportService(connection, scanner);
                    importService.execute();
                    break;
                case 17:
                    ExportService exportService = new ExportService(connection, scanner);
                    exportService.execute();
                    break;
                default:
                    System.out.println("\nInvalid choice! Please select an option between 1-18.");
                    break;
            }
        } catch (SQLException e) {
            System.err.println("\nDatabase error occurred:");
            System.err.println("  Message: " + e.getMessage());
            if (e.getSQLState() != null) {
                System.err.println("  SQL State: " + e.getSQLState());
            }
            if (e.getErrorCode() != 0) {
                System.err.println("  Error Code: " + e.getErrorCode());
            }
        } catch (Exception e) {
            System.err.println("\nAn unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Pause before showing menu again
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Close the database connection and close input scanner 
     */
    public void closeConnection(){
        if(connection != null){
            ConnectDatabase.closeConnection(connection);
            connection = null;
        }
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Main application loop that display menu and handle user input
     */
    public void run(){
        boolean running = true;
        while(running){
            displayMenu();

            try{
                if(scanner.hasNextInt()){
                    int choice = scanner.nextInt();
                    scanner.nextLine(); 
                    
                    if(choice == 18){
                        running = false;
                        System.out.println("\nThank you for using NPSS Database System. Goodbye!");
                    }else if(choice >= 1 && choice <= 17){
                        processMenuChoice(choice);
                    }else{
                        System.out.println("\nInvalid choice! Please select an option between 1-18.");
                        System.out.println("Press Enter to continue...");
                        scanner.nextLine();
                    }
                }else{
                    System.out.println("\nInvalid input! Please enter a number between 1-18.");
                    scanner.nextLine(); // Clear invalid input
                }
            }catch(Exception e){
                System.err.println("\nAn error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        }
        closeConnection();
    }
}
