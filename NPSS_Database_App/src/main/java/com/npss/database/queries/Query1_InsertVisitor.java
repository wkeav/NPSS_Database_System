package com.npss.database.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
/**
* Query 1: Insert a new visitor into the database and associate them with one or more park programs
*
* @author Astra Nguyen
* @version 1.0
*/

public class Query1_InsertVisitor {
   private Connection connection;
   private Scanner scanner;

   public Query1_InsertVisitor(Connection connection, Scanner scanner) {
       this.connection = connection;
       this.scanner = scanner;
   }

   /**
    * Executes Query 1: Insert a new visitor into the database
    * and associate them with one or more park programs
    *
    * @throws SQLException if a database error occurs
    */
   public void execute() throws SQLException {
       System.out.println("\n[Query 1] Insert a new visitor into the database and associate them with one or more park programs");

       try{
           // Get user input
           System.out.print("Enter visitor ID number: ");
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
      
           System.out.print("Enter visit date (YYYY-MM-DD) or press Enter for NULL: ");
           String visitDate = scanner.nextLine().trim();
           if (visitDate.isEmpty()) visitDate = null;
      
           System.out.print("Enter accessibility needs (or press Enter for NULL): ");
           String accessibilityNeeds = scanner.nextLine().trim();
           if (accessibilityNeeds.isEmpty()) accessibilityNeeds = null;

           // SQL queries
           String insertIndividualSQL =
               "INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, " +
               "postal_code, date_of_birth, newsletter_status) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // ? prevents SQL injection, handles types safely like null

           String insertVisitorSQL =
               "INSERT INTO Visitor(id_number, visit_date, accessibility_needs) " +
               "VALUES (?, ?, ?)";

           // Set parameters
           PreparedStatement parameterIndividual = connection.prepareStatement(insertIndividualSQL);
           parameterIndividual.setString(1, idNumber);
           parameterIndividual.setString(2, firstName);
           parameterIndividual.setString(3, lastName);
           parameterIndividual.setString(4, gender);
           parameterIndividual.setString(5, street);
           parameterIndividual.setString(6, city);
           parameterIndividual.setString(7, state);
           parameterIndividual.setString(8, postalCode);
           parameterIndividual.setDate(9, java.sql.Date.valueOf(dateOfBirth));
           parameterIndividual.setBoolean(10, newsletterStatus);

           PreparedStatement parameterVisitor = connection.prepareStatement(insertVisitorSQL);
           parameterVisitor.setString(1, idNumber);
           if (visitDate != null) {
               parameterVisitor.setDate(2, java.sql.Date.valueOf(visitDate));
           } else {
               parameterVisitor.setNull(2, java.sql.Types.DATE);
           }  
           parameterVisitor.setString(3, accessibilityNeeds);

           // Execute
           connection.setAutoCommit(false); // Start transaction
          
           try {
               // Execute inserts 
               parameterIndividual.executeUpdate();
               parameterVisitor.executeUpdate();
              
               // Associate with park programs
               System.out.print("How many park programs to enroll? (0 or more): ");
               int programCount = Integer.parseInt(scanner.nextLine().trim());
              
               if (programCount > 0) {
                   String enrollProgramSQL =
                       "INSERT INTO Visitor_enrolls_program(visitor_id_number, program_name) " +
                       "VALUES (?, ?)";
                   PreparedStatement pstmtEnroll = connection.prepareStatement(enrollProgramSQL);
                  
                   for (int i = 0; i < programCount; i++) {
                       System.out.print("Enter program name " + (i + 1) + ": ");
                       String programName = scanner.nextLine().trim();
                       pstmtEnroll.setString(1, idNumber);
                       pstmtEnroll.setString(2, programName);
                       pstmtEnroll.executeUpdate();
                   }
                   pstmtEnroll.close();
               }
              
               connection.commit(); // Commit transaction
               System.out.println("Visitor inserted successfully!");
              
           } catch (SQLException e) {
               connection.rollback(); // Rollback on error
               throw e;
           } finally {
               connection.setAutoCommit(true); // Reset auto-commit
               parameterIndividual.close();
               parameterVisitor.close();
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
