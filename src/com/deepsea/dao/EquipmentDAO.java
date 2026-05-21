package com.deepsea.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.deepsea.db.DBConnection;
import com.deepsea.models.Equipment;

public class EquipmentDAO {

    public void addEquipment(Equipment equipment) {
        String sql = "INSERT INTO equipments (vessel_id, name, type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Handle portable equipment (where vesselId might be null)
            if (equipment.getVesselId() != null) {
                pstmt.setInt(1, equipment.getVesselId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, equipment.getName());
            pstmt.setString(3, equipment.getType());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Equipment added: " + equipment.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Equipment> getAllEquipment() {
        List<Equipment> items = new ArrayList<>();
        String sql = "SELECT * FROM equipments";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // getObject is used here to safely handle potential null vessel_ids
                Integer vesselId = (Integer) rs.getObject("vessel_id");
                
                Equipment eq = new Equipment(
                    rs.getInt("equipment_id"),
                    vesselId,
                    rs.getString("name"),
                    rs.getString("type")
                );
                items.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}