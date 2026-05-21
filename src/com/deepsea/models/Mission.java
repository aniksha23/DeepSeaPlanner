package com.deepsea.models;

import java.sql.Date; // Using java.sql.Date for database compatibility

public class Mission {
    private int missionId;
    private Integer vesselId; 
    private String location;
    private int targetDepth;
    private Date startDate;
    private Date endDate;
    private MissionStatus status;

    // Constructor for creating a new Mission
    public Mission(Integer vesselId, String location, int targetDepth, Date startDate, Date endDate, MissionStatus status) {
        this.vesselId = vesselId;
        this.location = location;
        this.targetDepth = targetDepth;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Constructor for reading from DB
    public Mission(int missionId, Integer vesselId, String location, int targetDepth, Date startDate, Date endDate, MissionStatus status) {
        this.missionId = missionId;
        this.vesselId = vesselId;
        this.location = location;
        this.targetDepth = targetDepth;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters
    public int getMissionId() { return missionId; }
    public Integer getVesselId() { return vesselId; }
    public String getLocation() { return location; }
    public int getTargetDepth() { return targetDepth; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public MissionStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "Mission [ID=" + missionId + ", Location=" + location + ", Dates=" + startDate + " to " + endDate + ", Status=" + status + "]";
    }
}