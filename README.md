# Deep Sea Planner (Deep Sea Command Center)

A desktop-based enterprise mission planning, fleet management, and resource allocation system built in **Java Swing** with a **MySQL JDBC** backend database. The system is designed to coordinate deep-sea operations by checking scheduling, equipment, vessel, and personnel constraints via a specialized business validation engine.

---

## 🚀 Key Features

* **Live Command Center Dashboard:** Real-time metrics showing total missions, approved rates, pending approvals, and active vessels.
* **Intelligent Mission Planner:** Schedule missions, assign vessels, attach crew members, and provision diving gear. Uses robust `JSpinner` controls for strict date range validation.
* **Automated Feasibility Engine:** Evaluates depth limits, berth capacity limits, active crew certifications, and resource conflicts to automatically transition missions to `APPROVED` or `REJECTED` states.
* **Vessel & Fleet Operations:** Full CRUD capability to track active vessels, maximum diving depths, berth capacities, and maintenance cycles.
* **Personnel & Certifications Tracker:** Live tracking of personnel safety certifications and automated detection of expiring or invalid credentials.
* **Conflict Log Panel:** Automated warnings showing temporal resource scheduling conflicts and overloading limits.

---

## 🛠️ Technology Stack

* **Frontend GUI:** Java Swing & AWT (utilizes the Event Dispatch Thread for thread-safe UI rendering).
* **Business Logic Layer:** Pure Java SE OOP services (`FeasibilityChecker.java`).
* **Data Access Layer:** JDBC (Java Database Connectivity) with the DAO (Data Access Object) design pattern for complete separation of DB and UI logic.
* **Database Management System:** MySQL (relational constraints, cascading deletes, correlated subqueries, and ACID-compliant transaction rollbacks).

---

## 📂 Project Architecture

```
DeepSeaPlanner/
├── src/                          # Java Source Files
│   └── com/deepsea/
│       ├── main/                 # Entry point (Main.java)
│       ├── models/               # Data structures & Enums (Mission, Vessel, etc.)
│       ├── dao/                  # JDBC Data Access Objects (MissionDAO, VesselDAO, etc.)
│       ├── db/                   # Database Connection Manager (DBConnection.java)
│       ├── services/             # Validation logic (FeasibilityChecker.java)
│       └── ui/                   # Swing GUI layout panels (DashboardFrame.java, etc.)
├── bin/                          # Compiled Class Files (Ignored in Git)
├── .gitignore                    # Excludes compiled binary (.class) files from Git
└── README.md                     # Documentation
```

---

## 🗄️ Database Schema Details

The database `deep_sea_planner` consists of **6 relational tables**:

1. **`vessels`**: Fleet details (ID, name, berth capacity, max diving depth, status).
2. **`mission`**: Core operations planning (ID, location, target depth, start date, end date, status).
3. **`personnel`**: Crew registry (ID, name, role).
4. **`equipments`**: Dive gear inventory (ID, name, type).
5. **`assignment`**: Junction table mapping crew and equipment to specific missions.
6. **`certifications`**: Crew safety credentials and expiry dates.

*Note: Cascade deletes (`ON DELETE CASCADE`) are set on the foreign keys of the `assignment` and `certifications` tables to preserve referential integrity.*

---

## 💻 Setup & Execution Instructions

### 1. Database Setup
1. Open your MySQL client and create the database:
   ```sql
   CREATE DATABASE deep_sea_planner;
   ```
2. Import the schema script (or run your create table DDL scripts) into this database.
3. Open `src/com/deepsea/db/DBConnection.java` and set your local MySQL connection URL, username, and password:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/deep_sea_planner";
   private static final String USER = "your_mysql_username";
   private static final String PASSWORD = "your_mysql_password";
   ```

### 2. Compilation
Compile all Java source files from the project root directory and direct the output files into the `bin/` directory:
```powershell
javac -cp "bin;C:\path\to\mysql-connector-j.jar" -d bin src/com/deepsea/models/*.java src/com/deepsea/dao/*.java src/com/deepsea/db/*.java src/com/deepsea/services/*.java src/com/deepsea/ui/*.java src/com/deepsea/main/*.java
```

### 3. Execution
Launch the application by running the compiled Main class from the `bin/` directory:
```powershell
java -cp "bin;C:\path\to\mysql-connector-j.jar" com.deepsea.main.Main
```
*(Replace `C:\path\to\mysql-connector-j.jar` with the actual path to your downloaded MySQL Connector-J JAR file)*.

---

## 🧹 Git Best Practices
Compiled binaries (`.class` files in `bin/`) are ignored via `.gitignore` to keep the repository clean. If compiled files were previously tracked:
1. Untrack them:
   ```bash
   git rm -r --cached bin/
   ```
2. Commit and push the clean state:
   ```bash
   git commit -m "Untrack compiled build files"
   git push
   ```
This ensures your GitHub repository stays clean and only contains your Java source code.
