package com.deepsea.models;

public enum VesselStatus {
    AVAILABLE("Available"),
    MAINTENANCE("Maintenance"),
    DEPLOYED("Deployed");

    private final String displayName;

    VesselStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VesselStatus fromString(String text) {
        for (VesselStatus status : VesselStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return AVAILABLE; // Default
    }
}
