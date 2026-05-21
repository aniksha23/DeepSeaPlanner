package com.deepsea.models;

public class Personnel {
    private int personnelId;
    private String name;
    private String role; // e.g., "Marine Biologist", "ROV Pilot", "Medic"

    // Constructor for creating a new person
    public Personnel(String name, String role) {
        this.name = name;
        this.role = role;
    }

    // Constructor for reading from the database
    public Personnel(int personnelId, String name, String role) {
        this.personnelId = personnelId;
        this.name = name;
        this.role = role;
    }

    public int getPersonnelId() { return personnelId; }
    public String getName() { return name; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return "Personnel [ID=" + personnelId + ", Name=" + name + ", Role=" + role + "]";
    }
}