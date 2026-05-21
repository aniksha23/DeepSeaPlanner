package com.deepsea.models;

public enum CertStatus {
    VALID("VALID"),
    EXPIRING_SOON("EXPIRING SOON"),
    EXPIRED("EXPIRED");

    private final String displayName;

    CertStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CertStatus fromString(String text) {
        for (CertStatus status : CertStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return VALID;
    }
}
