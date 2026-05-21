package com.deepsea.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.deepsea.db.DBConnection;
import com.deepsea.models.Vessel;

public class VesselDAO {

    // Method to add a new vessel to the database
    public void addVessel(Vessel vessel) {
        String sql = "INSERT INTO vessels (name, berth_capacity, max_depth, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vessel.getName());
            pstmt.setInt(2, vessel.getBerthCapacity());
            pstmt.setInt(3, vessel.getMaxDepth());
            pstmt.setString(4, vessel.getStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Vessel added successfully: " + vessel.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch all vessels from the database
    public List<Vessel> getAllVessels() {
        List<Vessel> vessels = new ArrayList<>();
        String sql = "SELECT * FROM vessels";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Vessel v = new Vessel(
                    rs.getInt("vessel_id"),
                    rs.getString("name"),
                    rs.getInt("berth_capacity"),
                    rs.getInt("max_depth"),
                    rs.getString("status")
                );
                vessels.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vessels;
    }
 // UPDATE Vessel Status
    public void updateVesselStatus(int vesselId, String status) {
        String sql = "UPDATE vessels SET status = ? WHERE vessel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, vesselId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // DELETE Vessel
    public void deleteVessel(int vesselId) {
        String sql = "DELETE FROM vessels WHERE vessel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vesselId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}