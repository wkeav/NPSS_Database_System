# Entity-Relationship Model - Complete Notes

## Overview
**Source**: Database System Concepts by Silberschatz, Korth, and Sudarshan  
**Topic Coverage**: Complete ER Model fundamentals and advanced features

## Table of Contents
1. Design Process
2. Modeling Concepts
3. Constraints
4. E-R Diagrams
5. Design Issues
6. Weak Entity Sets
7. Extended E-R Features
8. ER Design Examples
9. Reduction to Relation Schemas (covered in Topic 3)

## 1. Design Process
- Foundational database design methodology
- Systematic approach to structuring database schemas

## 2. Modeling Concepts

### Entities and Entity Sets
- **Entity**: An object that exists and is distinguishable from other objects
  - Examples: specific person, company, event, plant
- **Entity Set**: A set of entities of the same type sharing the same properties
  - Examples: all persons, companies, trees, holidays

#### 🎯 **Visual Learning Exercise**
```
Entity Set: STUDENT
┌─────────────────┐
│     STUDENT     │
├─────────────────┤
│ student_id (PK) │
│ name            │
│ email           │
│ major           │
└─────────────────┘

Individual Entities:
- Entity 1: (S001, "Alice", "alice@uni.edu", "CS")
- Entity 2: (S002, "Bob", "bob@uni.edu", "Math")
- Entity 3: (S003, "Carol", "carol@uni.edu", "CS")
```

### Attributes
- Descriptive properties possessed by all members of an entity set
- **Domain**: Set of permitted values for each attribute

#### Attribute Types
- **Simple Attribute**: Composed of only one part (not divisible)
  - Examples: student ID, instructor salary
- **Composite Attribute**: Composed of several subparts
  - Example: name (first_name, middle_initial, last_name)
  - Example: address (street_number, street_name, city, state, postal_code)
- **Single-valued Attribute**: Has only one value
  - Examples: first name, last name
- **Multivalued Attribute**: Has multiple values
  - Examples: phone_numbers, student_degree
- **Derived Attribute**: Can be computed from other attributes
  - Examples: age (given date_of_birth)

### Relationship Sets
- **Relationship**: Association among several entities
- **Relationship Set**: Mathematical relation among n ≥ 2 entities
  - Formula: `{(e₁, e₂, ..., eₙ) | e₁ ∈ E₁, e₂ ∈ E₂, ..., eₙ ∈ Eₙ}`
- Relationships can have attributes
  - Example: `advisor` relationship with `date` attribute tracking when association began

#### 🎯 **Visual Learning Exercise - Relationships**
```
Binary Relationship: ADVISES
┌─────────────┐    advises    ┌─────────────┐
│  INSTRUCTOR │◄─────────────►│   STUDENT   │
│             │               │             │
│ instructor_id│               │ student_id  │
│ name        │               │ name        │
│ department  │               │ major       │
└─────────────┘               └─────────────┘
         │                           │
         └───── Relationship Set ────┘
         {(I001, S001), (I002, S003), (I001, S002)}

With Attributes:
┌─────────────┐    advises    ┌─────────────┐
│  INSTRUCTOR │◄─────────────►│   STUDENT   │
│             │      │        │             │
│ instructor_id│      │        │ student_id  │
│ name        │      │        │ name        │
│ department  │      │        │ major       │
└─────────────┘      │        └─────────────┘
                     │
                ┌─────────┐
                │  date   │
                │  hours  │
                └─────────┘
```

## 3. Constraints

### Mapping Cardinality Constraints
Expresses the number of entities to which another entity can be associated via a relationship set.

#### Types for Binary Relationships:
- **One to One** (1:1)
- **One to Many** (1:N)
- **Many to One** (N:1)
- **Many to Many** (M:N)

#### 🎯 **Visual Learning Exercise - Cardinality**
```
1:1 Relationship (One-to-One)
┌─────────────┐      1:1      ┌─────────────┐
│   PERSON    │◄─────────────►│  PASSPORT   │
│             │               │             │
│ person_id   │               │ passport_no │
│ name        │               │ country     │
└─────────────┘               └─────────────┘
Each person has exactly one passport
Each passport belongs to exactly one person

1:N Relationship (One-to-Many)
┌─────────────┐      1:N      ┌─────────────┐
│ DEPARTMENT  │◄─────────────►│  EMPLOYEE   │
│             │               │             │
│ dept_id     │               │ emp_id      │
│ dept_name   │               │ name        │
└─────────────┘               └─────────────┘
Each department has many employees
Each employee belongs to one department

M:N Relationship (Many-to-Many)
┌─────────────┐      M:N      ┌─────────────┐
│   STUDENT   │◄─────────────►│    COURSE   │
│             │               │             │
│ student_id  │               │ course_id   │
│ name        │               │ title       │
└─────────────┘               └─────────────┘
Students can take many courses
Courses can have many students
```

### Degree of Relationship Sets
- **Binary Relationship**: Involves two entity sets
  - Example: instructors teach classes
- **Non-binary Relationship**: Involves more than two entity sets
  - Example: students work on research projects under instructor guidance (ternary)

### Keys
- **Super Key**: Set of one or more attributes whose values uniquely determine each entity
- **Candidate Key**: Minimal super key (no proper subset is a super key)
- **Primary Key**: Selected candidate key for identifying entities
  - Example: ID is candidate key of instructor, course_id is candidate key of course

### Participation Constraints
- **Total Participation**: Every entity participates in at least one relationship (double line)
- **Partial Participation**: Some entities may not participate in any relationship

### Complex Constraints Notation
- Minimum and maximum cardinality in form `l..h`
  - Minimum value 1 indicates total participation
  - Maximum value 1 indicates at most one relationship
  - Maximum value * indicates no limit

## 4. E-R Diagrams

### Basic Symbols
- **Rectangles**: Entity sets
- **Diamonds**: Relationship sets
- **Attributes**: Listed inside entity rectangles
- **Underline**: Primary key attributes
- **Double Rectangle**: Weak entity set
- **Double Diamond**: Identifying relationship
- **Double Line**: Total participation
- **ISA Triangle**: Specialization/Generalization

### Special Notations
- **Composite Attributes**: Tree structure showing components
- **Multivalued Attributes**: Oval with double border
- **Derived Attributes**: Oval with dashed border

### Role Indicators
- Used in recursive relationships where entity sets need not be distinct
- Example: course prerequisites with roles "course_id" and "prereq_id"

## 5. Design Issues

### Common Design Problems
- **Use of Entity Sets vs. Attributes**: When to model as entity vs. attribute
  - Example: Phone as entity allows extra information about phone numbers
- **Use of Entity Sets vs. Relationship Sets**: Designate relationship sets for actions between entities
- **Redundant Attributes**: Remove attributes that replicate relationship information
  - Example: dept_name in instructor when inst_dept relationship exists

### Binary vs. Non-binary Relationships
- Non-binary relationships show clearer participation of multiple entities
- Some non-binary relationships may be better as binary relationships
  - Example: Parents relationship (ternary) vs. father and mother relationships (binary)

### Placement of Relationship Attributes
- Decision on where to place attributes like "date" - on relationship or entity

## 6. Weak Entity Sets

### Characteristics
- No primary key of its own
- Existence depends on identifying entity set
- Must relate via total, one-to-many relationship set
- Has discriminator (partial key) to distinguish among weak entities

#### 🎯 **Visual Learning Exercise - Weak Entities**
```
Strong Entity Set: COURSE
┌─────────────────┐
│     COURSE      │
├─────────────────┤
│ course_id (PK)  │
│ title           │
│ credits         │
└─────────────────┘
         │
         │ 1:N (Total Participation)
         │
         ▼
┌─────────────────┐    ┌─────────────────┐
│    SECTION      │◄───┤   OFFERS        │
│ (Weak Entity)   │    │ (Identifying    │
├─────────────────┤    │  Relationship)  │
│ sec_id (PK)     │    └─────────────────┘
│ semester        │
│ year            │
│ room            │
└─────────────────┘

Primary Key of SECTION = (course_id, sec_id, semester, year)
- course_id comes from strong entity COURSE
- sec_id is the discriminator (partial key)
- Without COURSE, SECTION cannot exist
```

### Primary Key Formation
- Primary key = Primary key of strong entity set + discriminator of weak entity set
- Example: section entity primary key = (course_id, sec_id, semester, year)

### Notation
- Double rectangle for weak entity set
- Dashed underline for discriminator
- Double diamond for identifying relationship

## 7. Extended E-R Features

### Specialization
- Top-down design process
- Subgroupings within entity sets become lower-level entity sets
- **ISA relationship**: Inheritance of attributes and relationships
- **Attribute Inheritance**: Lower-level entities inherit higher-level attributes

### Generalization
- Bottom-up design process
- Combine entity sets with shared features into higher-level entity set
- Represented same as specialization in E-R diagrams

### Design Constraints on Specialization/Generalization

#### Membership Constraints:
- **Disjoint**: Entity belongs to only one lower-level entity set
- **Overlapping**: Entity can belong to multiple lower-level entity sets

#### Multiple Specializations:
- Example: permanent_employee vs. temporary_employee AND instructor vs. secretary

### Aggregation
- Treats relationship as abstract entity
- Allows relationships between relationships
- Eliminates redundancy in complex relationships
- Example: proj_guide relationship with evaluation

## 8. E-R Design Decisions

### Key Considerations
- Attribute vs. entity set representation
- Ternary vs. binary relationships
- Strong vs. weak entity sets
- Specialization/Generalization usage
- Aggregation application

## 9. Alternative ER Notations

### Common Variations
- **Chen Notation**
- **IDE1FX (Crows Feet Notation)**
- Different symbols for relationships and participation

## 10. Database Examples

### Problem 1: Company Database
- Departments with managers
- Projects controlled by departments
- Employees with assignments and dependents
- Hours tracking per project

### Problem 2: Hospital Database
- Persons as doctors, patients, or staff
- Specialized attributes for each role
- Treatment records with descriptions
- Inpatient/outpatient distinctions
- Ward and room management
- Emergency case tracking

## 11. Symbols Summary

### Core E-R Notation
- Entity sets, relationship sets, attributes
- Primary keys, weak entity indicators
- Participation constraints
- Specialization/Generalization
- Cardinality notations

## 🎯 **Interactive Practice Exercises**

### Exercise 1: Design a Library Management System
**Requirements:**
- Books have ISBN, title, author, publication year
- Members have ID, name, email, phone numbers (multiple)
- Books can be borrowed by members
- Track borrowing date and return date
- Some books are reference books (cannot be borrowed)

**Your Task:** Design the ER model using the visual notation above.

### Exercise 2: Hospital Database Design
**Requirements:**
- Patients have ID, name, date of birth, insurance info
- Doctors have ID, name, specialization, department
- Patients can have multiple doctors
- Each visit has date, diagnosis, treatment
- Some patients are inpatients (room assignment) or outpatients

**Your Task:** Identify entities, relationships, and constraints.

### Exercise 3: E-commerce System
**Requirements:**
- Customers, Products, Orders, Categories
- Products belong to categories
- Orders contain multiple products with quantities
- Track order status and shipping information

**Your Task:** Design the complete ER model with all constraints.

## 🧠 **Senior Engineer Insights**

### Common Design Mistakes to Avoid:
1. **Over-normalization**: Don't break everything into tiny entities
2. **Under-normalization**: Don't put everything in one table
3. **Missing constraints**: Always define cardinality and participation
4. **Poor naming**: Use clear, consistent naming conventions
5. **Ignoring weak entities**: Recognize when entities depend on others

### Real-World Application Tips:
- Start with business requirements, not technical implementation
- Validate your design with stakeholders
- Consider future scalability and maintenance
- Document your design decisions
- Use CASE tools for complex diagrams

## Key Takeaways

1. **ER Model** is a high-level design tool for describing data and relationships
2. **ER Diagrams** provide graphical representation of database structure
3. **Proper design** requires careful consideration of entities, relationships, and constraints
4. **Extended features** like specialization and aggregation enhance modeling capability
5. **Multiple notation systems** exist with similar expressive power
6. **Visual learning** accelerates understanding of complex database concepts
7. **Practice exercises** build real-world design skills

This comprehensive coverage ensures understanding of all ER model concepts from basic entities and relationships to advanced features like weak entities, specialization, and aggregation.
