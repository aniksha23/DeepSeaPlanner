package com.deepsea.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.deepsea.db.DBConnection;
import com.deepsea.models.Assignment;

public class AssignmentDAO {

    public void addAssignment(Assignment assignment) {
        String sql = "INSERT INTO assignment (mission_id, personnel_id, equipment_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, assignment.getMissionId());
            
            if (assignment.getPersonnelId() != null) pstmt.setInt(2, assignment.getPersonnelId());
            else pstmt.setNull(2, java.sql.Types.INTEGER);
            
            if (assignment.getEquipmentId() != null) pstmt.setInt(3, assignment.getEquipmentId());
            else pstmt.setNull(3, java.sql.Types.INTEGER);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Assignment saved for Mission ID: " + assignment.getMissionId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}