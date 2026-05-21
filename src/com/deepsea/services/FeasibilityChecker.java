package com.deepsea.services;

import java.sql.*;
import java.util.*;
import com.deepsea.db.DBConnection;
import com.deepsea.models.FeasibilityReport;

public class FeasibilityChecker {

    public FeasibilityReport runFullCheck(int missionId) {
        FeasibilityReport report = new FeasibilityReport(missionId);

        checkVesselAvailability(missionId, report);
        checkVesselDepthRating(missionId, report);
        checkCrewAvailability(missionId, report);
        checkCertificationExpiry(missionId, report);
        checkEquipmentConflict(missionId, report);
        checkVesselCapacity(missionId, report);

        return report;
    }

    // RULE 1: Is the vessel already on another active mission during these dates?
    private void checkVesselAvailability(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT v.name FROM mission m " +
            "JOIN vessels v ON m.vessel_id = v.vessel_id " +
            "WHERE m.vessel_id = (SELECT vessel_id FROM mission WHERE mission_id = ?) " +
            "AND m.mission_id != ? " +
            "AND m.status NOT IN ('COMPLETED','CANCELLED') " +
            "AND m.start_date <= (SELECT end_date FROM mission WHERE mission_id = ?) " +
            "AND m.end_date >= (SELECT start_date FROM mission WHERE mission_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId); p.setInt(2, missionId);
            p.setInt(3, missionId); p.setInt(4, missionId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                report.addConflict("VESSEL CONFLICT",
                    "Vessel '" + rs.getString("name") + "' is already assigned to another mission during these dates.");
            } else {
                report.addPass("Vessel is available for the requested dates.");
            }
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify vessel availability.");
        }
    }

    // RULE 2: Can this vessel even reach the target depth?
    private void checkVesselDepthRating(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT v.name, v.max_depth, m.target_depth " +
            "FROM mission m JOIN vessels v ON m.vessel_id = v.vessel_id " +
            "WHERE m.mission_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                int maxDepth = rs.getInt("max_depth");
                int targetDepth = rs.getInt("target_depth");
                if (targetDepth > maxDepth) {
                    report.addConflict("DEPTH EXCEEDED",
                        "Mission requires " + targetDepth + "m depth. Vessel '" +
                        rs.getString("name") + "' is only rated to " + maxDepth + "m.");
                } else {
                    report.addPass("Vessel depth rating OK (" + maxDepth + "m rated, " + targetDepth + "m required).");
                }
            }
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify vessel depth rating.");
        }
    }

    // RULE 3: Is any assigned crew member already on another mission during these dates?
    private void checkCrewAvailability(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT DISTINCT p.name FROM assignment a " +
            "JOIN personnel p ON a.personnel_id = p.personnel_id " +
            "JOIN mission m2 ON a.mission_id = m2.mission_id " +
            "WHERE a.personnel_id IN ( " +
            "   SELECT personnel_id FROM assignment WHERE mission_id = ? AND personnel_id IS NOT NULL" +
            ") " +
            "AND a.mission_id != ? " +
            "AND m2.status NOT IN ('COMPLETED','CANCELLED') " +
            "AND m2.start_date <= (SELECT end_date FROM mission WHERE mission_id = ?) " +
            "AND m2.end_date >= (SELECT start_date FROM mission WHERE mission_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId); p.setInt(2, missionId);
            p.setInt(3, missionId); p.setInt(4, missionId);
            ResultSet rs = p.executeQuery();
            boolean conflict = false;
            while (rs.next()) {
                conflict = true;
                report.addConflict("CREW CONFLICT",
                    rs.getString("name") + " is already assigned to another active mission during these dates.");
            }
            if (!conflict) report.addPass("All crew members are available.");
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify crew availability.");
        }
    }

    // RULE 4: Do any assigned crew members have expired certifications?
    private void checkCertificationExpiry(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT p.name, c.cert_type, c.expiry_date " +
            "FROM assignment a " +
            "JOIN personnel p ON a.personnel_id = p.personnel_id " +
            "JOIN certifications c ON p.personnel_id = c.personnel_id " +
            "JOIN mission m ON a.mission_id = m.mission_id " +
            "WHERE m.mission_id = ? AND c.expiry_date < m.start_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId);
            ResultSet rs = p.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                report.addConflict("EXPIRED CERT",
                    rs.getString("name") + "'s " + rs.getString("cert_type") +
                    " certification expired on " + rs.getString("expiry_date") + ".");
            }
            if (!found) report.addPass("All crew certifications are valid.");
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify certifications.");
        }
    }

    // RULE 5: Is any assigned equipment already on another mission during these dates?
    private void checkEquipmentConflict(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT DISTINCT e.name FROM assignment a " +
            "JOIN equipments e ON a.equipment_id = e.equipment_id " +
            "JOIN mission m2 ON a.mission_id = m2.mission_id " +
            "WHERE a.equipment_id IN ( " +
            "   SELECT equipment_id FROM assignment WHERE mission_id = ? AND equipment_id IS NOT NULL" +
            ") " +
            "AND a.mission_id != ? " +
            "AND m2.status NOT IN ('COMPLETED','CANCELLED') " +
            "AND m2.start_date <= (SELECT end_date FROM mission WHERE mission_id = ?) " +
            "AND m2.end_date >= (SELECT start_date FROM mission WHERE mission_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId); p.setInt(2, missionId);
            p.setInt(3, missionId); p.setInt(4, missionId);
            ResultSet rs = p.executeQuery();
            boolean conflict = false;
            while (rs.next()) {
                conflict = true;
                report.addConflict("EQUIPMENT CONFLICT",
                    "Equipment '" + rs.getString("name") + "' is already assigned to another mission during these dates.");
            }
            if (!conflict) report.addPass("All equipment is available.");
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify equipment availability.");
        }
    }

    // RULE 6: Does the crew count exceed vessel berth capacity?
    private void checkVesselCapacity(int missionId, FeasibilityReport report) {
        String sql =
            "SELECT v.name, v.berth_capacity, COUNT(a.personnel_id) AS crew_count " +
            "FROM mission m " +
            "JOIN vessels v ON m.vessel_id = v.vessel_id " +
            "LEFT JOIN assignment a ON m.mission_id = a.mission_id AND a.personnel_id IS NOT NULL " +
            "WHERE m.mission_id = ? " +
            "GROUP BY v.name, v.berth_capacity";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, missionId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                int capacity = rs.getInt("berth_capacity");
                int crew = rs.getInt("crew_count");
                if (crew > capacity) {
                    report.addConflict("CAPACITY OVERLOAD",
                        rs.getString("name") + " has " + capacity + " berths but " + crew + " crew assigned.");
                } else {
                    report.addPass("Vessel capacity OK (" + crew + "/" + capacity + " berths used).");
                }
            }
        } catch (SQLException e) {
            report.addConflict("DB ERROR", "Could not verify vessel capacity.");
        }
    }
}