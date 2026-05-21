package com.deepsea.models;

public class Equipment {
    private int equipmentId;
    private Integer vesselId; // Use Integer instead of int so it can be null (for portable equipment)
    private String name;
    private String type;

    // Constructor for creating new equipment
    public Equipment(Integer vesselId, String name, String type) {
        this.vesselId = vesselId;
        this.name = name;
        this.type = type;
    }

    // Constructor for reading from DB
    public Equipment(int equipmentId, Integer vesselId, String name, String type) {
        this.equipmentId = equipmentId;
        this.vesselId = vesselId;
        this.name = name;
        this.type = type;
    }

    public int getEquipmentId() { return equipmentId; }
    public Integer getVesselId() { return vesselId; }
    public String getName() { return name; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return "Equipment [ID=" + equipmentId + ", Name=" + name + ", Type=" + type + ", VesselID=" + vesselId + "]";
    }
}