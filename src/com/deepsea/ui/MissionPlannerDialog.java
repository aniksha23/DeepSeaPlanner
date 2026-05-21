package com.deepsea.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

import com.deepsea.dao.*;
import com.deepsea.models.*;
import com.deepsea.services.FeasibilityChecker;

public class MissionPlannerDialog extends JDialog {

    private JTextField locField, depthField;
    private JSpinner startDateSpinner, endDateSpinner;
    private JComboBox<String> vesselCombo;
    private JList<String> crewList, equipmentList;
    
    // DAOs
    private VesselDAO vesselDAO = new VesselDAO();
    private PersonnelDAO personnelDAO = new PersonnelDAO();
    private EquipmentDAO equipmentDAO = new EquipmentDAO();
    private MissionDAO missionDAO = new MissionDAO();
    private AssignmentDAO assignmentDAO = new AssignmentDAO();

    public MissionPlannerDialog(JFrame parent) {
        super(parent, "Plan New Mission & Run Feasibility Check", true);
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Basic Info
        formPanel.add(new JLabel("Location:"));
        locField = new JTextField();
        formPanel.add(locField);

        formPanel.add(new JLabel("Target Depth (m):"));
        depthField = new JTextField();
        formPanel.add(depthField);

        formPanel.add(new JLabel("Start Date:"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        formPanel.add(startDateSpinner);

        formPanel.add(new JLabel("End Date:"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        formPanel.add(endDateSpinner);

        // 2. Select Vessel
        formPanel.add(new JLabel("Assign Vessel:"));
        vesselCombo = new JComboBox<>();
        for (Vessel v : vesselDAO.getAllVessels()) {
            vesselCombo.addItem(v.getVesselId() + " - " + v.getName());
        }
        formPanel.add(vesselCombo);

        // 3. Select Crew (Multiple Selection)
        formPanel.add(new JLabel("Assign Crew (Hold Ctrl to select multiple):"));
        DefaultListModel<String> crewModel = new DefaultListModel<>();
        for (Personnel p : personnelDAO.getAllPersonnel()) {
            crewModel.addElement(p.getPersonnelId() + " - " + p.getName() + " (" + p.getRole() + ")");
        }
        crewList = new JList<>(crewModel);
        formPanel.add(new JScrollPane(crewList));

        // 4. Select Equipment (Multiple Selection)
        formPanel.add(new JLabel("Assign Equipment:"));
        DefaultListModel<String> eqModel = new DefaultListModel<>();
        for (Equipment e : equipmentDAO.getAllEquipment()) {
            eqModel.addElement(e.getEquipmentId() + " - " + e.getName());
        }
        equipmentList = new JList<>(eqModel);
        formPanel.add(new JScrollPane(equipmentList));

        add(formPanel, BorderLayout.CENTER);

        // 5. The BIG Button
        JButton runBtn = new JButton("Run Feasibility Engine");
        runBtn.setBackground(new Color(46, 204, 113)); // Emerald Green
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("Arial", Font.BOLD, 16));
        runBtn.addActionListener(e -> executePlan());
        add(runBtn, BorderLayout.SOUTH);
    }

    private void executePlan() {
        try {
            // 1. Parse Data
            String loc = locField.getText();
            int depth = Integer.parseInt(depthField.getText());
            
            java.util.Date utilStart = (java.util.Date) startDateSpinner.getValue();
            java.util.Date utilEnd = (java.util.Date) endDateSpinner.getValue();
            Date start = new Date(utilStart.getTime());
            Date end = new Date(utilEnd.getTime());
            
            // Extract Vessel ID (e.g., "1 - Nautilus" -> 1)
            int vesselId = Integer.parseInt(vesselCombo.getSelectedItem().toString().split(" - ")[0]);

            // 2. Save Mission to DB
            Mission newMission = new Mission(vesselId, loc, depth, start, end, MissionStatus.PENDING);
            int missionId = missionDAO.addMission(newMission); 
            
            if (missionId == -1) {
                throw new Exception("Failed to generate Mission ID in database.");
            }

            // 3. Save Crew Assignments
            for (String crewStr : crewList.getSelectedValuesList()) {
                int crewId = Integer.parseInt(crewStr.split(" - ")[0]);
                assignmentDAO.addAssignment(new Assignment(missionId, crewId, null));
            }

            // 4. Save Equipment Assignments
            for (String eqStr : equipmentList.getSelectedValuesList()) {
                int eqId = Integer.parseInt(eqStr.split(" - ")[0]);
                assignmentDAO.addAssignment(new Assignment(missionId, null, eqId));
            }

            // 5. RUN THE ENGINE!
            FeasibilityReport report = new FeasibilityChecker().runFullCheck(missionId);
            
            // NEW CODE: Update the database based on the report!
            if (report.isFeasible()) {
                missionDAO.updateMissionStatus(missionId, MissionStatus.APPROVED);
            } else {
                missionDAO.updateMissionStatus(missionId, MissionStatus.REJECTED);
            }

            // 6. Show the Results
            showReportDialog(report);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in form data: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReportDialog(FeasibilityReport report) {
        JDialog reportDialog = new JDialog(this, "Feasibility Report — Mission #" + report.getMissionId(), true);
        reportDialog.setSize(550, 450);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel(
            report.isFeasible() ? "  ✅  MISSION APPROVED" : "  ❌  MISSION REJECTED",
            SwingConstants.CENTER
        );
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setOpaque(true);
        header.setBackground(report.isFeasible() ? new Color(39, 174, 96) : new Color(192, 57, 43));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        reportDialog.add(header, BorderLayout.NORTH);

        // Results Panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Show each PASS
        for (String pass : report.getPasses()) {
            JLabel lbl = new JLabel("  ✔  " + pass);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setForeground(new Color(39, 174, 96));
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            resultsPanel.add(lbl);
        }

        // Divider if there are both passes and conflicts
        if (!report.getPasses().isEmpty() && !report.getConflicts().isEmpty()) {
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            resultsPanel.add(Box.createVerticalStrut(10));
            resultsPanel.add(sep);
            resultsPanel.add(Box.createVerticalStrut(10));
        }

        // Show each CONFLICT
        for (String[] conflict : report.getConflicts()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

            JLabel typeLabel = new JLabel("  " + conflict[0]);
            typeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setOpaque(true);
            typeLabel.setBackground(new Color(192, 57, 43));
            typeLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            typeLabel.setPreferredSize(new Dimension(160, 30));

            JLabel msgLabel = new JLabel("  " + conflict[1]);
            msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

            row.add(typeLabel, BorderLayout.WEST);
            row.add(msgLabel, BorderLayout.CENTER);
            resultsPanel.add(row);
        }

        reportDialog.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> reportDialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeBtn);
        reportDialog.add(bottom, BorderLayout.SOUTH);

        reportDialog.setVisible(true);
    }
}