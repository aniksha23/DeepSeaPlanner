package com.deepsea.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.deepsea.db.DBConnection;
import com.deepsea.models.Certification;

public class CertificationDAO {

    public void addCertification(Certification cert) {
        String sql = "INSERT INTO certifications (personnel_id, cert_type, expiry_date) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cert.getPersonnelId());
            pstmt.setString(2, cert.getCertType());
            pstmt.setDate(3, cert.getExpiryDate());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Certification added: " + cert.getCertType());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}