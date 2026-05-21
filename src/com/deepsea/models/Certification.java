package com.deepsea.models;

import java.sql.Date;

public class Certification {
    private int certId;
    private int personnelId;
    private String certType;
    private Date expiryDate;

    // Constructor for creating a new Certification
    public Certification(int personnelId, String certType, Date expiryDate) {
        this.personnelId = personnelId;
        this.certType = certType;
        this.expiryDate = expiryDate;
    }

    // Constructor for reading from DB
    public Certification(int certId, int personnelId, String certType, Date expiryDate) {
        this.certId = certId;
        this.personnelId = personnelId;
        this.certType = certType;
        this.expiryDate = expiryDate;
    }

    public int getCertId() { return certId; }
    public int getPersonnelId() { return personnelId; }
    public String getCertType() { return certType; }
    public Date getExpiryDate() { return expiryDate; }

    @Override
    public String toString() {
        return "Certification [Type=" + certType + ", Expiry=" + expiryDate + ", PersonnelID=" + personnelId + "]";
    }
}