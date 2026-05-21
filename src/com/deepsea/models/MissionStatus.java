package com.deepsea.models;

public enum MissionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED,
    PROPOSED;

    public static MissionStatus fromString(String text) {
        if (text == null) return PENDING;
        for (MissionStatus status : MissionStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return PENDING;
    }
}
