# NPSS Database System

National Park Service System (NPSS) Database Management Application

## About

This project is a comprehensive database management system for the National Park Service System, built with Java and Azure SQL Database. It provides a menu-driven interface for managing park data, visitors, rangers, researchers, donations, and programs.

## Features

- **15 Database Queries**: Complete CRUD operations for all NPSS entities
- **Import/Export Functionality**: CSV file import for teams and export for mailing lists
- **Stored Procedures**: SQL Server stored procedures for all queries (Task 5a)
- **Error Handling**: Robust error detection and reporting
- **Transaction Management**: Proper database transaction handling

## Project Structure

```
NPSS_Database_App/
├── src/main/java/com/npss/database/
│   ├── Main.java                    # Application entry point
│   ├── NPSS_DBApp.java             # Main application class
│   ├── ConnectDatabase.java         # Database connection management
│   └── queries/                     # Query implementations
│       ├── Query1_InsertVisitor.java
│       ├── Query2_InsertRanger.java
│       ├── ... (all 15 queries)
│       ├── ImportService.java
│       └── ExportService.java
├── data/
│   ├── import/                     # Import data files
│   └── export/                     # Export data files
└── pom.xml                         # Maven configuration
```

## Requirements

- Java 11 or higher
- Maven 3.6+
- Azure SQL Database (or SQL Server)
- JDBC Driver for SQL Server

## Setup

1. Clone the repository:
```bash
git clone https://github.com/wkeav/NPSS_Database_System.git
cd NPSS_Database_System
```

2. Create a `.env` file in the project root with your database credentials:
```
DB_URL=jdbc:sqlserver://your-server.database.windows.net:1433;database=your-database
DB_USERNAME=your-username
DB_PASSWORD=your-password
```

3. Compile the project:
```bash
cd NPSS_Database_App
mvn clean compile
```

4. Run the application:
```bash
mvn exec:java -Dexec.mainClass="com.npss.database.Main"
```

## Files

- `Nguyen_Astra_IP_Task5a.sql`: SQL stored procedures for all queries
- `NPSS_Database_App/`: Main Java application source code

## Author
Astra Nguyen

## License

GPL-2.0
