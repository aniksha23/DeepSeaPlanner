package com.deepsea.models;

public class Assignment {
    private int assignmentId;
    private int missionId;
    private Integer personnelId; // Using Integer in case a row only assigns equipment
    private Integer equipmentId; // Using Integer in case a row only assigns personnel

    public Assignment(int missionId, Integer personnelId, Integer equipmentId) {
        this.missionId = missionId;
        this.personnelId = personnelId;
        this.equipmentId = equipmentId;
    }

    public int getAssignmentId() { return assignmentId; }
    public int getMissionId() { return missionId; }
    public Integer getPersonnelId() { return personnelId; }
    public Integer getEquipmentId() { return equipmentId; }
}