package com.deepsea.dao;

import java.sql.*;
import java.util.*;
import com.deepsea.db.DBConnection;

public class AdvancedQueryDAO {

    // QUERY 1: Crew availability — NOT EXISTS subquery
    public List<String[]> getAvailablePersonnel(String startDate, String endDate) {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT p.personnel_id, p.name, p.role " +
            "FROM personnel p " +
            "WHERE NOT EXISTS ( " +
            "   SELECT 1 FROM assignment a " +
            "   JOIN mission m ON a.mission_id = m.mission_id " +
            "   WHERE a.personnel_id = p.personnel_id " +
            "   AND m.start_date <= ? AND m.end_date >= ? " +
            "   AND m.status NOT IN ('CANCELLED', 'REJECTED') " +
            ")";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, endDate);
            pstmt.setString(2, startDate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("personnel_id"),
                    rs.getString("name"),
                    rs.getString("role")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 2: Equipment conflicts — temporal overlap JOIN
    public List<String[]> getConflictingEquipment(String startDate, String endDate) {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT e.equipment_id, e.name, m.location, m.start_date, m.end_date " +
            "FROM equipments e " +
            "JOIN assignment a ON e.equipment_id = a.equipment_id " +
            "JOIN mission m ON a.mission_id = m.mission_id " +
            "WHERE m.start_date <= ? AND m.end_date >= ? " +
            "AND m.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, endDate);
            pstmt.setString(2, startDate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("equipment_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("start_date"),
                    rs.getString("end_date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 3: Vessel utilization — GROUP BY + DATEDIFF
    public List<String[]> getVesselUtilization() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT v.name, " +
            "COUNT(m.mission_id) AS total_missions, " +
            "COALESCE(SUM(DATEDIFF(m.end_date, m.start_date)), 0) AS total_days, " +
            "v.status " +
            "FROM vessels v " +
            "LEFT JOIN mission m ON v.vessel_id = m.vessel_id " +
            "AND m.status != 'CANCELLED' " +
            "GROUP BY v.vessel_id, v.name, v.status " +
            "ORDER BY total_days DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("name"),
                    rs.getString("total_missions"),
                    rs.getString("total_days"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 4: Expiring certifications — DATE functions
    public List<String[]> getExpiringCertifications() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT p.name, p.role, c.cert_type, c.expiry_date, " +
            "DATEDIFF(c.expiry_date, CURDATE()) AS days_remaining " +
            "FROM personnel p " +
            "JOIN certifications c ON p.personnel_id = c.personnel_id " +
            "WHERE c.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 60 DAY) " +
            "ORDER BY c.expiry_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("cert_type"),
                    rs.getString("expiry_date"),
                    rs.getString("days_remaining") + " days"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 5: Overloaded vessels — HAVING COUNT > 1
    public List<String[]> getOverloadedVessels() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT v.name, COUNT(m.mission_id) AS mission_count " +
            "FROM vessels v " +
            "JOIN mission m ON v.vessel_id = m.vessel_id " +
            "WHERE m.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED') " +
            "GROUP BY v.vessel_id, v.name " +
            "HAVING COUNT(m.mission_id) > 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("name"),
                    rs.getString("mission_count")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 6: Full mission summary — correlated subqueries
    public List<String[]> getMissionSummary() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT m.mission_id, m.location, m.start_date, m.end_date, m.status, " +
            "v.name AS vessel_name, " +
            "(SELECT COUNT(DISTINCT a1.personnel_id) FROM assignment a1 " +
            " WHERE a1.mission_id = m.mission_id AND a1.personnel_id IS NOT NULL) AS crew_count, " +
            "(SELECT COUNT(DISTINCT a2.equipment_id) FROM assignment a2 " +
            " WHERE a2.mission_id = m.mission_id AND a2.equipment_id IS NOT NULL) AS equipment_count " +
            "FROM mission m " +
            "LEFT JOIN vessels v ON m.vessel_id = v.vessel_id " +
            "ORDER BY m.start_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("mission_id"),
                    rs.getString("location"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getString("status"),
                    rs.getString("vessel_name"),
                    rs.getString("crew_count"),
                    rs.getString("equipment_count")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 7 (NEW): Mission status breakdown — for the analytics bar chart
    public List<String[]> getMissionStatusBreakdown() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT status, COUNT(*) AS cnt " +
            "FROM mission " +
            "GROUP BY status " +
            "ORDER BY cnt DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("status"),
                    rs.getString("cnt")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 8 (NEW): Top personnel by mission count — subquery + GROUP BY
    public List<String[]> getTopPersonnelByMissions() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT p.name, p.role, COUNT(a.mission_id) AS mission_count " +
            "FROM personnel p " +
            "LEFT JOIN assignment a ON p.personnel_id = a.personnel_id " +
            "AND a.personnel_id IS NOT NULL " +
            "GROUP BY p.personnel_id, p.name, p.role " +
            "ORDER BY mission_count DESC " +
            "LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("mission_count")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // QUERY 9 (NEW): All certifications for a personnel — for Certifications sub-tab
    public List<String[]> getAllCertifications() {
        List<String[]> result = new ArrayList<>();
        String sql =
            "SELECT p.name, p.role, c.cert_type, c.expiry_date, " +
            "CASE " +
            "  WHEN c.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN c.expiry_date < DATE_ADD(CURDATE(), INTERVAL 60 DAY) THEN 'EXPIRING SOON' " +
            "  ELSE 'VALID' " +
            "END AS cert_status " +
            "FROM personnel p " +
            "JOIN certifications c ON p.personnel_id = c.personnel_id " +
            "ORDER BY c.expiry_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("cert_type"),
                    rs.getString("expiry_date"),
                    rs.getString("cert_status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }
}