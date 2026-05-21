package com.deepsea.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.deepsea.db.DBConnection;
import com.deepsea.models.Personnel;

public class PersonnelDAO {

    public void addPersonnel(Personnel person) {
        String sql = "INSERT INTO personnel (name, role) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, person.getName());
            pstmt.setString(2, person.getRole());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Personnel added: " + person.getName() + " (" + person.getRole() + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Personnel> getAllPersonnel() {
        List<Personnel> crew = new ArrayList<>();
        String sql = "SELECT * FROM personnel";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Personnel p = new Personnel(
                    rs.getInt("personnel_id"),
                    rs.getString("name"),
                    rs.getString("role")
                );
                crew.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return crew;
    }
 // DELETE Personnel
    public void deletePersonnel(int personnelId) {
        String sql = "DELETE FROM personnel WHERE personnel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, personnelId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}