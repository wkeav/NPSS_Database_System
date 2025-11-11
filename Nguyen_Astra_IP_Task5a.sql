-- ============================================================================
-- NPSS Database System - Task 5a
-- SQL Stored Procedures for Queries 1-14 with Error Checking
-- Author: Astra Nguyen
-- Date: 2024
-- ============================================================================

USE NPSS_Database;
GO

-- QUERY 1: Insert a new visitor and associate with park programs
CREATE OR ALTER PROCEDURE sp_InsertVisitor
    @id_number VARCHAR(50),
    @first_name VARCHAR(100),
    @last_name VARCHAR(100),
    @gender CHAR(1),
    @street VARCHAR(200),
    @city VARCHAR(100),
    @state VARCHAR(50),
    @postal_code VARCHAR(20),
    @date_of_birth DATE,
    @newsletter_status BIT,
    @visit_date DATE = NULL,
    @accessibility_needs VARCHAR(500) = NULL,
    @program_names NVARCHAR(MAX) = NULL, 
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate gender
        IF @gender NOT IN ('M', 'F', 'O')
        BEGIN
            SET @error_message = 'Invalid gender. Must be M, F, or O.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, 
                               postal_code, date_of_birth, newsletter_status)
        VALUES (@id_number, @first_name, @last_name, @gender, @street, @city, @state, 
                @postal_code, @date_of_birth, @newsletter_status);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        INSERT INTO Visitor(id_number, visit_date, accessibility_needs)
        VALUES (@id_number, @visit_date, @accessibility_needs);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        -- Associate with park programs 
        IF @program_names IS NOT NULL AND LEN(@program_names) > 0
        BEGIN
            DECLARE @program_name VARCHAR(200);
            DECLARE @pos INT;
            DECLARE @program_list NVARCHAR(MAX) = @program_names + ',';
            
            WHILE LEN(@program_list) > 0
            BEGIN
                SET @pos = CHARINDEX(',', @program_list);
                SET @program_name = LTRIM(RTRIM(SUBSTRING(@program_list, 1, @pos - 1)));
                SET @program_list = SUBSTRING(@program_list, @pos + 1, LEN(@program_list));
                
                IF LEN(@program_name) > 0
                BEGIN
                    INSERT INTO Visitor_enrolls_program(visitor_id_number, program_name)
                    VALUES (@id_number, @program_name);
                    SET @rows_affected = @rows_affected + @@ROWCOUNT;
                END
            END
        END
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 2: Insert a new ranger and assign to a team
CREATE OR ALTER PROCEDURE sp_InsertRanger
    @id_number VARCHAR(50),
    @first_name VARCHAR(100),
    @last_name VARCHAR(100),
    @gender CHAR(1),
    @street VARCHAR(200),
    @city VARCHAR(100),
    @state VARCHAR(50),
    @postal_code VARCHAR(20),
    @date_of_birth DATE,
    @newsletter_status BIT,
    @team_id VARCHAR(50),
    @start_date DATE,
    @status VARCHAR(50),
    @certifications NVARCHAR(MAX) = NULL,  -- Comma-separated list
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate gender
        IF @gender NOT IN ('M', 'F', 'O')
        BEGIN
            SET @error_message = 'Invalid gender. Must be M, F, or O.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Validate status
        IF @status NOT IN ('active', 'inactive', 'on_leave', 'terminated')
        BEGIN
            SET @error_message = 'Invalid status. Must be active, inactive, on_leave, or terminated.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check if team exists
        IF NOT EXISTS (SELECT 1 FROM Ranger_team WHERE team_id = @team_id)
        BEGIN
            SET @error_message = 'Team ' + @team_id + ' does not exist.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, 
                               postal_code, date_of_birth, newsletter_status)
        VALUES (@id_number, @first_name, @last_name, @gender, @street, @city, @state, 
                @postal_code, @date_of_birth, @newsletter_status);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        INSERT INTO Ranger(id_number)
        VALUES (@id_number);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        INSERT INTO Ranger_assigned_ranger_team(ranger_id_number, team_id, start_date, status)
        VALUES (@id_number, @team_id, @start_date, @status);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        IF @certifications IS NOT NULL AND LEN(@certifications) > 0
        BEGIN
            DECLARE @certification VARCHAR(200);
            DECLARE @pos INT;
            DECLARE @cert_list NVARCHAR(MAX) = @certifications + ',';
            
            WHILE LEN(@cert_list) > 0
            BEGIN
                SET @pos = CHARINDEX(',', @cert_list);
                SET @certification = LTRIM(RTRIM(SUBSTRING(@cert_list, 1, @pos - 1)));
                SET @cert_list = SUBSTRING(@cert_list, @pos + 1, LEN(@cert_list));
                
                IF LEN(@certification) > 0
                BEGIN
                    INSERT INTO Ranger_certifications(id_number, certification)
                    VALUES (@id_number, @certification);
                    SET @rows_affected = @rows_affected + @@ROWCOUNT;
                END
            END
        END
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 3: Insert a new ranger team
CREATE OR ALTER PROCEDURE sp_InsertRangerTeam
    @team_id VARCHAR(50),
    @formation_date DATE,
    @focus_date DATE = NULL,
    @team_leader VARCHAR(50) = NULL,
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate team leader exists if provided
        IF @team_leader IS NOT NULL
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM Ranger WHERE id_number = @team_leader)
            BEGIN
                SET @error_message = 'Team leader ' + @team_leader + ' does not exist.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
        END
        
        INSERT INTO Ranger_team(team_id, formation_date, focus_date, team_leader)
        VALUES (@team_id, @formation_date, @focus_date, @team_leader);
        
        SET @rows_affected = @@ROWCOUNT;
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 4: Insert a new donation (creates donor/individual if needed)
CREATE OR ALTER PROCEDURE sp_InsertDonation
    @donation_id VARCHAR(50),
    @donor_id_number VARCHAR(50),
    @donation_date DATE,
    @amount DECIMAL(18, 2),
    @campaign_name VARCHAR(200) = NULL,
    @payment_method VARCHAR(10),  -- 'check' or 'card'
    @check_number VARCHAR(50) = NULL,
    @card_type VARCHAR(50) = NULL,
    @last_four_digits VARCHAR(4) = NULL,
    @expiration_date DATE = NULL,
    -- Individual fields (if creating new)
    @first_name VARCHAR(100) = NULL,
    @last_name VARCHAR(100) = NULL,
    @gender CHAR(1) = NULL,
    @street VARCHAR(200) = NULL,
    @city VARCHAR(100) = NULL,
    @state VARCHAR(50) = NULL,
    @postal_code VARCHAR(20) = NULL,
    @date_of_birth DATE = NULL,
    @newsletter_status BIT = 0,
    @preference VARCHAR(200) = NULL,
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate amount
        IF @amount <= 0
        BEGIN
            SET @error_message = 'Donation amount must be greater than 0.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check if individual exists, create if not 
        IF NOT EXISTS (SELECT id_number FROM Individual WHERE id_number = @donor_id_number)
        BEGIN
            IF @first_name IS NULL OR @last_name IS NULL OR @gender IS NULL OR 
               @street IS NULL OR @city IS NULL OR @state IS NULL OR 
               @postal_code IS NULL OR @date_of_birth IS NULL
            BEGIN
                SET @error_message = 'Individual does not exist. All individual fields are required.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            IF @gender NOT IN ('M', 'F', 'O')
            BEGIN
                SET @error_message = 'Invalid gender. Must be M, F, or O.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, 
                                   postal_code, date_of_birth, newsletter_status)
            VALUES (@donor_id_number, @first_name, @last_name, @gender, @street, @city, @state, 
                    @postal_code, @date_of_birth, @newsletter_status);
            
            SET @rows_affected = @rows_affected + @@ROWCOUNT;
        END
        
        -- Check if donor exists, create if not
        IF NOT EXISTS (SELECT id_number FROM Donor WHERE id_number = @donor_id_number)
        BEGIN
            INSERT INTO Donor(id_number, preference)
            VALUES (@donor_id_number, @preference);
            
            SET @rows_affected = @rows_affected + @@ROWCOUNT;
        END
        
        -- Insert donation
        INSERT INTO Donation(donation_id, donor_id_number, date, amount, campaign_name)
        VALUES (@donation_id, @donor_id_number, @donation_date, @amount, @campaign_name);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        -- Insert payment method
        IF @payment_method = 'check'
        BEGIN
            IF @check_number IS NULL
            BEGIN
                SET @error_message = 'Check number is required for check payment.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            INSERT INTO Check_donation(donation_id, check_number)
            VALUES (@donation_id, @check_number);
            
            SET @rows_affected = @rows_affected + @@ROWCOUNT;
        END
        ELSE IF @payment_method = 'card'
        BEGIN
            IF @card_type IS NULL OR @last_four_digits IS NULL OR @expiration_date IS NULL
            BEGIN
                SET @error_message = 'Card type, last four digits, and expiration date are required for card payment.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            INSERT INTO Card_number(donation_id, card_type, last_four_digits, expiration_date)
            VALUES (@donation_id, @card_type, @last_four_digits, @expiration_date);
            
            SET @rows_affected = @rows_affected + @@ROWCOUNT;
        END
        ELSE
        BEGIN
            SET @error_message = 'Invalid payment method. Must be ''check'' or ''card''.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 5: Insert a new researcher and associate with ranger teams
CREATE OR ALTER PROCEDURE sp_InsertResearcher
    @id_number VARCHAR(50),
    @first_name VARCHAR(100),
    @last_name VARCHAR(100),
    @gender CHAR(1),
    @street VARCHAR(200),
    @city VARCHAR(100),
    @state VARCHAR(50),
    @postal_code VARCHAR(20),
    @date_of_birth DATE,
    @newsletter_status BIT,
    @research_field VARCHAR(200),
    @hire_date DATE,
    @salary DECIMAL(18, 2),
    @team_ids NVARCHAR(MAX),  -- Comma-separated list of team IDs
    @report_dates NVARCHAR(MAX) = NULL,  -- Comma-separated list of dates
    @summaries NVARCHAR(MAX) = NULL,  -- Comma-separated list
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate gender
        IF @gender NOT IN ('M', 'F', 'O')
        BEGIN
            SET @error_message = 'Invalid gender. Must be M, F, or O.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Validate salary
        IF @salary <= 0
        BEGIN
            SET @error_message = 'Salary must be greater than 0.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        INSERT INTO Individual(id_number, first_name, last_name, gender, street, city, state, 
                               postal_code, date_of_birth, newsletter_status)
        VALUES (@id_number, @first_name, @last_name, @gender, @street, @city, @state, 
                @postal_code, @date_of_birth, @newsletter_status);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        INSERT INTO Researcher(id_number, research_field, hire_date, salary)
        VALUES (@id_number, @research_field, @hire_date, @salary);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        -- Associate with ranger teams
        IF @team_ids IS NOT NULL AND LEN(@team_ids) > 0
        BEGIN
            DECLARE @team_id VARCHAR(50);
            DECLARE @report_date DATE;
            DECLARE @summary VARCHAR(MAX);
            DECLARE @pos INT;
            DECLARE @team_list NVARCHAR(MAX) = @team_ids + ',';
            DECLARE @date_list NVARCHAR(MAX) = ISNULL(@report_dates, '') + ',';
            DECLARE @summary_list NVARCHAR(MAX) = ISNULL(@summaries, '') + ',';
            
            WHILE LEN(@team_list) > 0
            BEGIN
                SET @pos = CHARINDEX(',', @team_list);
                SET @team_id = LTRIM(RTRIM(SUBSTRING(@team_list, 1, @pos - 1)));
                SET @team_list = SUBSTRING(@team_list, @pos + 1, LEN(@team_list));
                
                IF LEN(@team_id) > 0
                BEGIN
                    -- Check if team exists
                    IF NOT EXISTS (SELECT 1 FROM Ranger_team WHERE team_id = @team_id)
                    BEGIN
                        SET @error_message = 'Team ' + @team_id + ' does not exist.';
                        ROLLBACK TRANSACTION;
                        RETURN;
                    END
                    
                    -- Get corresponding date and summary
                    SET @report_date = GETDATE();  -- Default to today
                    SET @summary = NULL;
                    
                    IF LEN(@date_list) > 0
                    BEGIN
                        SET @pos = CHARINDEX(',', @date_list);
                        DECLARE @date_str VARCHAR(50) = LTRIM(RTRIM(SUBSTRING(@date_list, 1, @pos - 1)));
                        SET @date_list = SUBSTRING(@date_list, @pos + 1, LEN(@date_list));
                        
                        IF LEN(@date_str) > 0
                            SET @report_date = CAST(@date_str AS DATE);
                    END
                    
                    IF LEN(@summary_list) > 0
                    BEGIN
                        SET @pos = CHARINDEX(',', @summary_list);
                        SET @summary = LTRIM(RTRIM(SUBSTRING(@summary_list, 1, @pos - 1)));
                        SET @summary_list = SUBSTRING(@summary_list, @pos + 1, LEN(@summary_list));
                        
                        IF LEN(@summary) = 0
                            SET @summary = NULL;
                    END
                    
                    INSERT INTO Researcher_reports_ranger_team(researcher_id_number, team_id, date, summary)
                    VALUES (@id_number, @team_id, @report_date, @summary);
                    
                    SET @rows_affected = @rows_affected + @@ROWCOUNT;
                END
            END
        END
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 6: Insert/Update a report (UPSERT using MERGE)
CREATE OR ALTER PROCEDURE sp_InsertReport
    @researcher_id_number VARCHAR(50),
    @team_id VARCHAR(50),
    @date DATE,
    @summary VARCHAR(MAX) = NULL,
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Check if researcher exists
        IF NOT EXISTS (SELECT 1 FROM Researcher WHERE id_number = @researcher_id_number)
        BEGIN
            SET @error_message = 'Researcher ' + @researcher_id_number + ' does not exist.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check if team exists
        IF NOT EXISTS (SELECT 1 FROM Ranger_team WHERE team_id = @team_id)
        BEGIN
            SET @error_message = 'Team ' + @team_id + ' does not exist.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        MERGE Researcher_reports_ranger_team AS target
        USING (SELECT @researcher_id_number AS researcher_id_number, @team_id AS team_id) AS source
        ON target.researcher_id_number = source.researcher_id_number
           AND target.team_id = source.team_id
        WHEN MATCHED THEN
            UPDATE SET date = @date, summary = @summary
        WHEN NOT MATCHED THEN
            INSERT (researcher_id_number, team_id, date, summary)
            VALUES (@researcher_id_number, @team_id, @date, @summary);
        
        SET @rows_affected = @@ROWCOUNT;
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 7: Insert a park program (creates park if needed)
CREATE OR ALTER PROCEDURE sp_InsertParkProgram
    @program_name VARCHAR(200),
    @type VARCHAR(100),
    @start_date DATE,
    @duration INT,
    @park_name VARCHAR(200),
    -- Park fields
    @park_street VARCHAR(200) = NULL,
    @park_city VARCHAR(100) = NULL,
    @park_state VARCHAR(50) = NULL,
    @park_postal_code VARCHAR(20) = NULL,
    @establishment_date DATE = NULL,
    @capacity INT = NULL,
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validate duration
        IF @duration <= 0
        BEGIN
            SET @error_message = 'Duration must be greater than 0.';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Create park if it doesn't exist 
        IF NOT EXISTS (SELECT Name FROM National_parks WHERE Name = @park_name)
        BEGIN
            IF @park_street IS NULL OR @park_city IS NULL OR @park_state IS NULL OR 
               @park_postal_code IS NULL OR @establishment_date IS NULL OR @capacity IS NULL
            BEGIN
                SET @error_message = 'Park does not exist. All park fields are required.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            IF @capacity <= 0
            BEGIN
                SET @error_message = 'Park capacity must be greater than 0.';
                ROLLBACK TRANSACTION;
                RETURN;
            END
            
            INSERT INTO National_parks(Name, Street, City, State, Postal_code, Establishment_date, Capacity)
            VALUES (@park_name, @park_street, @park_city, @park_state, @park_postal_code, @establishment_date, @capacity);
            
            SET @rows_affected = @rows_affected + @@ROWCOUNT;
        END
        
        -- Insert program 
        INSERT INTO Program(program_name, type, start_date, duration)
        VALUES (@program_name, @type, @start_date, @duration);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        -- Link program to park 
        INSERT INTO National_parks_offers_program(park_name, program_name)
        VALUES (@park_name, @program_name);
        
        SET @rows_affected = @rows_affected + @@ROWCOUNT;
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- QUERY 8: Retrieve emergency contacts for a person
CREATE OR ALTER PROCEDURE sp_RetrieveEmergencyContacts
    @id_number VARCHAR(50),
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        SELECT 
            ec.name AS emergency_contact_name,
            ec.relationship,
            ec.phone_number,
            CONCAT(i.first_name, ' ', i.last_name) AS person_name
        FROM Emergency_contact ec
        INNER JOIN Individual i ON ec.id_number = i.id_number
        WHERE ec.id_number = @id_number
        ORDER BY ec.relationship, ec.name;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 9: Retrieve visitors enrolled in a program
CREATE OR ALTER PROCEDURE sp_RetrieveVisitorsInProgram
    @program_name VARCHAR(200),
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        SELECT 
            i.id_number,
            i.first_name,
            i.last_name,
            CONCAT(i.first_name, ' ', i.last_name) AS full_name,
            v.accessibility_needs,
            v.visit_date
        FROM Visitor_enrolls_program vep
        INNER JOIN Visitor v ON vep.visitor_id_number = v.id_number
        INNER JOIN Individual i ON v.id_number = i.id_number
        WHERE vep.program_name = @program_name
        ORDER BY i.last_name, i.first_name;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 10: Retrieve park programs for a park after a date
CREATE OR ALTER PROCEDURE sp_RetrieveParkPrograms
    @park_name VARCHAR(200),
    @start_date DATE,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        SELECT 
            p.program_name,
            p.type,
            p.start_date,
            p.duration
        FROM National_parks_offers_program npop
        INNER JOIN Program p ON npop.program_name = p.program_name
        WHERE npop.park_name = @park_name
          AND p.start_date > @start_date
        ORDER BY p.start_date, p.program_name;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 11: Retrieve donation statistics for anonymous donors
CREATE OR ALTER PROCEDURE sp_RetrieveDonationStats
    @year INT,
    @month INT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        -- Validate month
        IF @month < 1 OR @month > 12
        BEGIN
            SET @error_message = 'Month must be between 1 and 12.';
            RETURN;
        END
        
        SELECT 
            dr.id_number AS donor_id,
            SUM(d.amount) AS total_amount,
            AVG(d.amount) AS average_amount,
            COUNT(d.donation_id) AS donation_count
        FROM Donation d
        INNER JOIN Donor dr ON d.donor_id_number = dr.id_number
        WHERE YEAR(d.date) = @year
          AND MONTH(d.date) = @month
          AND (dr.preference IS NULL OR dr.preference = 'Anonymous')
        GROUP BY dr.id_number
        ORDER BY SUM(d.amount) DESC;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 12: Retrieve rangers in a team
CREATE OR ALTER PROCEDURE sp_RetrieveRangersInTeam
    @team_id VARCHAR(50),
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        SELECT DISTINCT
            i.id_number,
            i.first_name,
            i.last_name,
            CONCAT(i.first_name, ' ', i.last_name) AS full_name,
            rart.status,
            rart.years_of_service,
            rc.certification
        FROM Ranger_assigned_ranger_team rart
        INNER JOIN Ranger r ON rart.ranger_id_number = r.id_number
        INNER JOIN Individual i ON r.id_number = i.id_number
        LEFT JOIN Ranger_certifications rc ON r.id_number = rc.id_number
        WHERE rart.team_id = @team_id
        ORDER BY i.last_name, i.first_name, rc.certification;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 13: Retrieve all individuals
CREATE OR ALTER PROCEDURE sp_RetrieveAllIndividuals
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @error_message = NULL;
    
    BEGIN TRY
        SELECT 
            i.id_number,
            i.first_name,
            i.last_name,
            CONCAT(i.first_name, ' ', i.last_name) AS full_name,
            i.newsletter_status,
            ipn.phone_number,
            iea.email_address
        FROM Individual i
        LEFT JOIN Individual_phone_numbers ipn ON i.id_number = ipn.id_number
        LEFT JOIN Individual_email_addresses iea ON i.id_number = iea.id_number
        ORDER BY i.last_name, i.first_name, ipn.phone_number, iea.email_address;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- QUERY 14: Update researcher salary (3% increase for those overseeing >1 team)
CREATE OR ALTER PROCEDURE sp_UpdateResearcherSalary
    @rows_affected INT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @rows_affected = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        UPDATE Researcher
        SET salary = salary * 1.03
        WHERE id_number IN (
            SELECT researcher_id_number
            FROM Researcher_reports_ranger_team
            GROUP BY researcher_id_number
            HAVING COUNT(DISTINCT team_id) > 1
        );
        
        SET @rows_affected = @@ROWCOUNT;
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @error_message = ERROR_MESSAGE();
        SET @rows_affected = 0;
    END CATCH
END;
GO

-- Procedure to check for foreign key constraint violations
CREATE OR ALTER PROCEDURE sp_CheckForeignKeyViolations
    @table_name VARCHAR(200),
    @column_name VARCHAR(200),
    @value VARCHAR(200),
    @exists BIT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @exists = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        DECLARE @sql NVARCHAR(MAX);
        DECLARE @count INT;
        
        SET @sql = N'SELECT @count = COUNT(*) FROM ' + QUOTENAME(@table_name) + 
                   N' WHERE ' + QUOTENAME(@column_name) + N' = @val';
        
        EXEC sp_executesql @sql, N'@val VARCHAR(200), @count INT OUTPUT', @val = @value, @count = @count OUTPUT;
        
        IF @count > 0
            SET @exists = 1;
        ELSE
            SET @error_message = 'Foreign key violation: ' + @value + ' does not exist in ' + @table_name + '.' + @column_name;
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedure to check for primary key constraint violations
CREATE OR ALTER PROCEDURE sp_CheckPrimaryKeyViolations
    @table_name VARCHAR(200),
    @column_name VARCHAR(200),
    @value VARCHAR(200),
    @exists BIT OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @exists = 0;
    SET @error_message = NULL;
    
    BEGIN TRY
        DECLARE @sql NVARCHAR(MAX);
        DECLARE @count INT;
        
        SET @sql = N'SELECT @count = COUNT(*) FROM ' + QUOTENAME(@table_name) + 
                   N' WHERE ' + QUOTENAME(@column_name) + N' = @val';
        
        EXEC sp_executesql @sql, N'@val VARCHAR(200), @count INT OUTPUT', @val = @value, @count = @count OUTPUT;
        
        IF @count > 0
        BEGIN
            SET @exists = 1;
            SET @error_message = 'Primary key violation: ' + @value + ' already exists in ' + @table_name + '.' + @column_name;
        END
        
    END TRY
    BEGIN CATCH
        SET @error_message = ERROR_MESSAGE();
    END CATCH
END;
GO

-- Procedure to validate date format
CREATE OR ALTER PROCEDURE sp_ValidateDateFormat
    @date_string VARCHAR(50),
    @is_valid BIT OUTPUT,
    @parsed_date DATE OUTPUT,
    @error_message NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @is_valid = 0;
    SET @parsed_date = NULL;
    SET @error_message = NULL;
    
    BEGIN TRY
        SET @parsed_date = CAST(@date_string AS DATE);
        SET @is_valid = 1;
    END TRY
    BEGIN CATCH
        SET @error_message = 'Invalid date format: ' + @date_string + '. Expected format: YYYY-MM-DD';
        SET @is_valid = 0;
    END CATCH
END;
GO

