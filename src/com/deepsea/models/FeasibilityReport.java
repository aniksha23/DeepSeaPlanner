package com.deepsea.models;

import java.util.*;

public class FeasibilityReport {
    private int missionId;
    private boolean isFeasible;
    private List<String[]> conflicts;   // Each entry: [type, message]
    private List<String> passes;

    public FeasibilityReport(int missionId) {
        this.missionId = missionId;
        this.isFeasible = true;
        this.conflicts = new ArrayList<>();
        this.passes = new ArrayList<>();
    }

    public void addConflict(String type, String message) {
        this.isFeasible = false;
        this.conflicts.add(new String[]{type, message});
    }

    public void addPass(String message) {
        this.passes.add(message);
    }

    public boolean isFeasible() { return isFeasible; }
    public int getMissionId() { return missionId; }
    public List<String[]> getConflicts() { return conflicts; }
    public List<String> getPasses() { return passes; }

    public void printReport() {
        System.out.println("=== FEASIBILITY REPORT: Mission " + missionId + " ===");
        System.out.println("STATUS: " + (isFeasible ? "APPROVED" : "REJECTED"));
        for (String s : passes) System.out.println("  PASS: " + s);
        for (String[] c : conflicts) System.out.println("  FAIL [" + c[0] + "]: " + c[1]);
    }
}