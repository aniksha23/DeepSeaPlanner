package com.deepsea.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.deepsea.db.DBConnection;
import com.deepsea.models.Mission;
import com.deepsea.models.MissionStatus;

public class MissionDAO {

    // ADD NEW MISSION — returns the generated mission_id (fixes race condition)
    public int addMission(Mission mission) {
        String sql = "INSERT INTO mission "
                + "(vessel_id, location, target_depth, start_date, end_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (mission.getVesselId() != null) {
                pstmt.setInt(1, mission.getVesselId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, mission.getLocation());
            pstmt.setInt(3, mission.getTargetDepth());
            pstmt.setDate(4, mission.getStartDate());
            pstmt.setDate(5, mission.getEndDate());
            pstmt.setString(6, mission.getStatus().name());

            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // GET ALL MISSIONS
    public List<Mission> getAllMissions() {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT * FROM mission";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer vesselId = (Integer) rs.getObject("vessel_id");
                missions.add(new Mission(
                        rs.getInt("mission_id"), vesselId,
                        rs.getString("location"), rs.getInt("target_depth"),
                        rs.getDate("start_date"), rs.getDate("end_date"),
                        MissionStatus.fromString(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missions;
    }

    // GET DASHBOARD MISSIONS — excludes REJECTED and CANCELLED
    public List<Mission> getDashboardMissions() {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT * FROM mission "
                   + "WHERE status NOT IN ('REJECTED', 'CANCELLED') "
                   + "ORDER BY start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer vesselId = (Integer) rs.getObject("vessel_id");
                missions.add(new Mission(
                        rs.getInt("mission_id"), vesselId,
                        rs.getString("location"), rs.getInt("target_depth"),
                        rs.getDate("start_date"), rs.getDate("end_date"),
                        MissionStatus.fromString(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missions;
    }

    // UPDATE MISSION STATUS
    public void updateMissionStatus(int missionId, MissionStatus status) {
        String sql = "UPDATE mission SET status = ? WHERE mission_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, missionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE MISSION — also deletes assignments via FK cascade
    public void deleteMission(int missionId) {
        String deleteAssignments = "DELETE FROM assignment WHERE mission_id = ?";
        String deleteMission = "DELETE FROM mission WHERE mission_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(deleteAssignments);
                 PreparedStatement p2 = conn.prepareStatement(deleteMission)) {
                
                p1.setInt(1, missionId);
                p1.executeUpdate();
                
                p2.setInt(1, missionId);
                p2.executeUpdate();
                
                conn.commit();
                System.out.println("✅ Mission #" + missionId + " and its assignments deleted.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GET MISSION COUNT BY STATUS — for dashboard stat cards
    public int countByStatus(MissionStatus status) {
        String sql = "SELECT COUNT(*) FROM mission WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}