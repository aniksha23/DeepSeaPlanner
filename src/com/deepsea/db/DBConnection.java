package com.deepsea.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Update the port (3306) if your MySQL uses a different one
    private static final String URL = "jdbc:mysql://localhost:3306/deep_sea_planner";
    
    // Replace with your actual MySQL username and password
    private static final String USER = "root";     
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}