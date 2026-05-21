package com.deepsea.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.deepsea.dao.*;
import com.deepsea.db.DBConnection;
import com.deepsea.models.*;
import java.text.SimpleDateFormat;

public class DashboardFrame extends JFrame {

    private VesselDAO vesselDAO = new VesselDAO();
    private MissionDAO missionDAO = new MissionDAO();
    private PersonnelDAO personnelDAO = new PersonnelDAO();
    private AdvancedQueryDAO advDAO = new AdvancedQueryDAO();

    private JPanel contentArea;
    private String currentNav = "Dashboard";

    // ── ENTRY POINT ──────────────────────────────────────────────────
    public DashboardFrame() {
        setTitle("Deep Sea Command Center");
        setSize(1150, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(245, 246, 248));
        contentArea.add(buildDashboardPanel(), BorderLayout.CENTER);
        body.add(contentArea, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    // ── TOP BAR ──────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        bar.setPreferredSize(new Dimension(0, 52));

        JLabel brand = new JLabel("  ⬡  Deep Sea Command Center");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(new Color(30, 30, 30));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);

        // Live DB status check
        boolean dbOk = false;
        try (Connection c = DBConnection.getConnection()) { dbOk = (c != null && !c.isClosed()); }
        catch (Exception ignored) {}

        String dbText = dbOk ? "● DB Connected" : "● DB Offline";
        Color dbBg   = dbOk ? new Color(225, 245, 238) : new Color(255, 235, 235);
        Color dbFg   = dbOk ? new Color(15, 110, 86)  : new Color(150, 30, 30);

        right.add(pill(dbText, dbBg, dbFg));
        right.add(pill("Mission Coordinator", new Color(240, 240, 240), new Color(60, 60, 60)));

        bar.add(brand, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JLabel pill(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

    // ── SIDEBAR ──────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(250, 250, 250));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        sidebar.setPreferredSize(new Dimension(200, 0));

        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(navItem("Dashboard",      "⊞", sidebar));
        sidebar.add(navItem("Mission Planner","✦", sidebar));
        sidebar.add(navItem("Fleet",          "⛵", sidebar));
        sidebar.add(navItem("Personnel",      "⚇", sidebar));
        sidebar.add(navItem("Equipment",      "⚙", sidebar));
        sidebar.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(navItem("Analytics",   "◈", sidebar));
        sidebar.add(navItem("Conflict Log","⚠", sidebar));
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel navItem(String label, String icon, JPanel sidebar) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 9));
        item.setMaximumSize(new Dimension(200, 40));
        item.setOpaque(true);
        updateNavStyle(item, label);

        JLabel lbl = new JLabel(icon + "  " + label);
        lbl.setFont(new Font("SansSerif", currentNav.equals(label) ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(currentNav.equals(label) ? new Color(29, 158, 117) : new Color(90, 90, 90));
        item.add(lbl);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                currentNav = label;
                // Refresh all nav items' styles
                for (Component c : sidebar.getComponents()) {
                    if (c instanceof JPanel) {
                        JPanel navPanel = (JPanel) c;
                        updateNavStyle(navPanel, "");
                        for (Component child : navPanel.getComponents()) {
                            if (child instanceof JLabel) {
                                JLabel childLbl = (JLabel) child;
                                String childLabel = childLbl.getText().substring(3).trim();
                                boolean active = childLabel.equals(label);
                                childLbl.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
                                childLbl.setForeground(active ? new Color(29, 158, 117) : new Color(90, 90, 90));
                                if (active) {
                                    navPanel.setBackground(Color.WHITE);
                                    navPanel.setBorder(new MatteBorder(0, 3, 0, 0, new Color(29, 158, 117)));
                                } else {
                                    navPanel.setBackground(new Color(250, 250, 250));
                                    navPanel.setBorder(new EmptyBorder(0, 3, 0, 0));
                                }
                            }
                        }
                    }
                }
                handleNav(label);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!currentNav.equals(label)) item.setBackground(new Color(243, 243, 243));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!currentNav.equals(label)) item.setBackground(new Color(250, 250, 250));
            }
        });
        return item;
    }

    private void updateNavStyle(JPanel item, String activeLabel) {
        boolean active = currentNav != null && currentNav.equals(activeLabel);
        item.setBackground(active ? Color.WHITE : new Color(250, 250, 250));
        item.setBorder(active
            ? new MatteBorder(0, 3, 0, 0, new Color(29, 158, 117))
            : new EmptyBorder(0, 3, 0, 0));
    }

    // ── NAVIGATION HANDLER ───────────────────────────────────────────
    private void handleNav(String label) {
        contentArea.removeAll();
        switch (label) {
            case "Mission Planner":
                MissionPlannerDialog d = new MissionPlannerDialog(this);
                d.setVisible(true);
                currentNav = "Dashboard";
                contentArea.add(buildDashboardPanel(), BorderLayout.CENTER);
                break;
            case "Analytics":
                contentArea.add(buildAnalyticsPanel(), BorderLayout.CENTER);
                break;
            case "Fleet":
                contentArea.add(buildFleetPanel(), BorderLayout.CENTER);
                break;
            case "Personnel":
                contentArea.add(buildPersonnelPanel(), BorderLayout.CENTER);
                break;
            case "Equipment":
                contentArea.add(buildEquipmentPanel(), BorderLayout.CENTER);
                break;
            case "Conflict Log":
                contentArea.add(buildConflictLogPanel(), BorderLayout.CENTER);
                break;
            default:
                contentArea.add(buildDashboardPanel(), BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── DASHBOARD HOME ───────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(buildStatCards(), BorderLayout.NORTH);
        panel.add(buildMissionTableCard(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(0, 95));

        int vessels  = vesselDAO.getAllVessels().size();
        int crew     = personnelDAO.getAllPersonnel().size();
        int missions = missionDAO.getAllMissions().size();
        int approved = missionDAO.countByStatus(MissionStatus.APPROVED);

        grid.add(statCard("Active Fleet",  String.valueOf(vessels),  "vessels registered",  new Color(29, 158, 117)));
        grid.add(statCard("Personnel",     String.valueOf(crew),     "crew members",         new Color(55, 138, 221)));
        grid.add(statCard("Total Missions",String.valueOf(missions), "all proposals",        new Color(99, 153, 34)));
        grid.add(statCard("Approved",      String.valueOf(approved), "missions approved",    new Color(226, 75, 74)));
        return grid;
    }

    private JPanel statCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lb.setForeground(new Color(120, 120, 120));

        JLabel accentBar = new JLabel();
        accentBar.setBackground(accent);
        accentBar.setOpaque(true);
        accentBar.setPreferredSize(new Dimension(0, 3));

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 32));
        val.setForeground(accent);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(new Color(150, 150, 150));

        card.add(accentBar, BorderLayout.NORTH);
        card.add(lb, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(val, BorderLayout.WEST);
        bottom.add(subLbl, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildMissionTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel title = new JLabel("Active & Pending Missions");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);

        JButton planBtn = new JButton("+ Plan New Mission");
        planBtn.setBackground(new Color(29, 158, 117));
        planBtn.setForeground(Color.WHITE);
        planBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        planBtn.setBorderPainted(false);
        planBtn.setFocusPainted(false);
        planBtn.addActionListener(e -> {
            MissionPlannerDialog dlg = new MissionPlannerDialog(this);
            dlg.setVisible(true);
            refreshDashboard();
        });
        header.add(planBtn, BorderLayout.EAST);

        String[] cols = {"#", "Location", "Depth", "Start", "End", "Status", "Vessel"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Mission m : missionDAO.getDashboardMissions()) {
            model.addRow(new Object[]{
                m.getMissionId(), m.getLocation(), m.getTargetDepth() + "m",
                m.getStartDate(), m.getEndDate(), m.getStatus(),
                m.getVesselId() == null ? "—" : "V-" + m.getVesselId()
            });
        }

        JTable table = styledTable(model);

        // Color-code status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                try {
                    MissionStatus status = MissionStatus.valueOf(s);
                    switch (status) {
                        case APPROVED: setForeground(new Color(15, 110, 86)); break;
                        case PENDING:  setForeground(new Color(160, 100, 0)); break;
                        default:       setForeground(new Color(90, 90, 90));
                    }
                } catch (IllegalArgumentException e) {
                    setForeground(new Color(90, 90, 90));
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        // Right-click: delete
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("⚠ Delete Mission");
        deleteItem.setForeground(new Color(192, 57, 43));
        deleteItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) table.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this,
                        "Delete Mission #" + id + " and all its assignments?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    missionDAO.deleteMission(id);
                    refreshDashboard();
                }
            }
        });
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── FLEET PANEL ──────────────────────────────────────────────────
    private JPanel buildFleetPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lbl = new JLabel("Fleet Management");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(lbl, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Vessel");
        addBtn.setBackground(new Color(55, 138, 221));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> showAddVesselDialog());
        header.add(addBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Capacity", "Max Depth (m)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Vessel v : vesselDAO.getAllVessels()) {
            model.addRow(new Object[]{
                v.getVesselId(), v.getName(),
                v.getBerthCapacity(), v.getMaxDepth(), v.getStatus()
            });
        }

        JTable table = styledTable(model);

        // Color-code status
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                VesselStatus status = VesselStatus.fromString(s);
                switch (status) {
                    case AVAILABLE:   setForeground(new Color(15, 110, 86)); break;
                    case DEPLOYED:    setForeground(new Color(55, 138, 221)); break;
                    default:          setForeground(new Color(180, 100, 0)); break;
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem statusItem = new JMenuItem("✎ Update Status");
        statusItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a vessel first."); return; }
            int id = (int) table.getValueAt(row, 0);
            String[] statuses = {"Available", "Maintenance", "Deployed"};
            String chosen = (String) JOptionPane.showInputDialog(this,
                "Select new status for: " + table.getValueAt(row, 1),
                "Update Status", JOptionPane.QUESTION_MESSAGE, null,
                statuses, table.getValueAt(row, 4));
            if (chosen != null) { vesselDAO.updateVesselStatus(id, chosen); handleNav("Fleet"); }
        });
        JMenuItem deleteItem = new JMenuItem("⚠ Delete Vessel");
        deleteItem.setForeground(new Color(192, 57, 43));
        deleteItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a vessel first."); return; }
            int id = (int) table.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this,
                    "Delete vessel '" + table.getValueAt(row, 1) + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                vesselDAO.deleteVessel(id);
                handleNav("Fleet");
            }
        });
        popup.add(statusItem);
        popup.addSeparator();
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);

        JLabel hint = new JLabel("  Right-click a row to update status or delete");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 160, 160));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private void showAddVesselDialog() {
        JDialog d = new JDialog(this, "Add New Vessel", true);
        d.setSize(320, 260);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameF  = new JTextField();
        JTextField capF   = new JTextField();
        JTextField depthF = new JTextField();
        JComboBox<String> statC = new JComboBox<>(new String[]{"Available", "Maintenance"});

        form.add(new JLabel("Name:"));      form.add(nameF);
        form.add(new JLabel("Berth Capacity:")); form.add(capF);
        form.add(new JLabel("Max Depth (m):")); form.add(depthF);
        form.add(new JLabel("Status:"));    form.add(statC);

        JButton save = new JButton("Save Vessel");
        save.setBackground(new Color(29, 158, 117));
        save.setForeground(Color.WHITE);
        save.setBorderPainted(false);
        save.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name cannot be empty."); return;
            }
            try {
                int cap   = Integer.parseInt(capF.getText().trim());
                int depth = Integer.parseInt(depthF.getText().trim());
                vesselDAO.addVessel(new Vessel(nameF.getText().trim(), cap, depth, statC.getSelectedItem().toString()));
                d.dispose();
                handleNav("Fleet");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Capacity and Depth must be numbers.");
            }
        });
        form.add(new JLabel(""));
        form.add(save);

        d.add(form);
        d.setVisible(true);
    }

    // ── PERSONNEL PANEL ──────────────────────────────────────────────
    private JPanel buildPersonnelPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lbl = new JLabel("Personnel Roster");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(lbl, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Personnel");
        addBtn.setBackground(new Color(55, 138, 221));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> showAddPersonnelDialog());
        header.add(addBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Tabs: Roster | Certifications
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // -- Roster tab
        String[] cols = {"ID", "Name", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Personnel p : personnelDAO.getAllPersonnel()) {
            model.addRow(new Object[]{p.getPersonnelId(), p.getName(), p.getRole()});
        }
        JTable rosterTable = styledTable(model);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("⚠ Delete Personnel");
        deleteItem.setForeground(new Color(192, 57, 43));
        deleteItem.addActionListener(e -> {
            int row = rosterTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a crew member first."); return; }
            int id = (int) rosterTable.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this,
                    "Delete '" + rosterTable.getValueAt(row, 1) + "'?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                personnelDAO.deletePersonnel(id);
                handleNav("Personnel");
            }
        });
        popup.add(deleteItem);
        rosterTable.setComponentPopupMenu(popup);

        JPanel rosterPanel = new JPanel(new BorderLayout());
        rosterPanel.add(new JScrollPane(rosterTable), BorderLayout.CENTER);
        JLabel hint = new JLabel("  Right-click a row to delete");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 160, 160));
        rosterPanel.add(hint, BorderLayout.SOUTH);

        // -- Certifications tab (all certs, color-coded)
        String[] certCols = {"Name", "Role", "Certification", "Expiry Date", "Status"};
        DefaultTableModel certModel = new DefaultTableModel(certCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : advDAO.getAllCertifications()) {
            certModel.addRow(row);
        }
        JTable certTable = styledTable(certModel);
        certTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                CertStatus status = CertStatus.fromString(s);
                switch (status) {
                    case EXPIRED:       setForeground(new Color(192, 57, 43)); break;
                    case EXPIRING_SOON: setForeground(new Color(180, 100, 0)); break;
                    default:            setForeground(new Color(15, 110, 86)); break;
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        tabs.addTab("Crew Roster", rosterPanel);
        tabs.addTab("Certifications", new JScrollPane(certTable));

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private void showAddPersonnelDialog() {
        JDialog d = new JDialog(this, "Add Personnel", true);
        d.setSize(300, 170);
        d.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameF = new JTextField();
        JTextField roleF = new JTextField();

        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Role:"));      form.add(roleF);

        JButton save = new JButton("Save");
        save.setBackground(new Color(29, 158, 117));
        save.setForeground(Color.WHITE);
        save.setBorderPainted(false);
        save.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty() || roleF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Name and Role cannot be empty."); return;
            }
            personnelDAO.addPersonnel(new Personnel(nameF.getText().trim(), roleF.getText().trim()));
            d.dispose();
            handleNav("Personnel");
        });
        form.add(new JLabel("")); form.add(save);

        d.add(form);
        d.setVisible(true);
    }

    // ── EQUIPMENT PANEL ──────────────────────────────────────────────
    private JPanel buildEquipmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("Equipment Inventory");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(lbl, BorderLayout.NORTH);

        EquipmentDAO eqDAO = new EquipmentDAO();
        String[] cols = {"ID", "Name", "Type", "Assigned Vessel"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Equipment e : eqDAO.getAllEquipment()) {
            String vesselLabel = (e.getVesselId() == null) ? "Portable / Unassigned" : "Vessel ID: " + e.getVesselId();
            model.addRow(new Object[]{e.getEquipmentId(), e.getName(), e.getType(), vesselLabel});
        }

        JTable table = styledTable(model);
        // Color-code portable vs assigned
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                if (s.startsWith("Portable")) setForeground(new Color(100, 100, 200));
                else                          setForeground(new Color(30, 30, 30));
                return this;
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel hint = new JLabel("  Equipment assignments are managed via Mission Planner");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 160, 160));
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // ── CONFLICT LOG PANEL ───────────────────────────────────────────
    private JPanel buildConflictLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Equipment Conflict Log");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(title, BorderLayout.NORTH);

        // Filter controls
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        filterBar.add(new JLabel("From:"));
        JSpinner startD = new JSpinner(new SpinnerDateModel());
        startD.setEditor(new JSpinner.DateEditor(startD, "yyyy-MM-dd"));
        JSpinner endD = new JSpinner(new SpinnerDateModel());
        endD.setEditor(new JSpinner.DateEditor(endD, "yyyy-MM-dd"));
        filterBar.add(startD);
        filterBar.add(new JLabel("To:"));
        filterBar.add(endD);

        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(29, 158, 117));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        filterBar.add(searchBtn);

        JLabel resultCount = new JLabel("0 conflicts found");
        resultCount.setForeground(new Color(130, 130, 130));
        resultCount.setFont(new Font("SansSerif", Font.ITALIC, 12));
        filterBar.add(Box.createHorizontalStrut(20));
        filterBar.add(resultCount);

        String[] cols = {"Equip ID", "Equipment Name", "Conflicting Mission Location", "Mission Start", "Mission End"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);

        searchBtn.addActionListener(e -> {
            model.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String s = sdf.format(startD.getValue());
            String ed = sdf.format(endD.getValue());
            List<String[]> rows = advDAO.getConflictingEquipment(s, ed);
            for (String[] row : rows) model.addRow(row);
            resultCount.setText(rows.size() + (rows.size() == 1 ? " conflict found" : " conflicts found"));
            resultCount.setForeground(rows.isEmpty() ? new Color(15, 110, 86) : new Color(192, 57, 43));
        });

        // Load with defaults on open
        searchBtn.doClick();

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filterBar, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ── ANALYTICS PANEL ──────────────────────────────────────────────
    private JPanel buildAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Analytics & Query Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        panel.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Tab 1: Mission Summary
        tabs.addTab("Mission Summary", buildQueryTab(
            "Full mission overview — crew count, equipment count, vessel (correlated subqueries)",
            new String[]{"Mission ID", "Location", "Start", "End", "Status", "Vessel", "Crew", "Equip"},
            advDAO.getMissionSummary()
        ));

        // Tab 2: Vessel Utilization with mini bar chart
        tabs.addTab("Vessel Utilization", buildVesselUtilizationTab());

        // Tab 3: Expiring Certifications
        tabs.addTab("Expiring Certs (60d)", buildExpiringCertsTab());

        // Tab 4: Available Crew — with date pickers
        tabs.addTab("Available Crew", buildAvailableCrewTab());

        // Tab 5: Overloaded Vessels
        tabs.addTab("Overloaded Vessels", buildQueryTab(
            "Vessels assigned to more than 1 active/pending mission simultaneously (HAVING COUNT > 1)",
            new String[]{"Vessel Name", "Active Mission Count"},
            advDAO.getOverloadedVessels()
        ));

        // Tab 6: Top Crew by Missions
        tabs.addTab("Top Crew", buildTopCrewTab());

        // Tab 7: Mission Status Breakdown with visual bar
        tabs.addTab("Status Breakdown", buildStatusBreakdownTab());

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    // Analytics: generic query tab
    private JPanel buildQueryTab(String description, String[] cols, List<String[]> rows) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>" + description + "</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 10, 0));

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) model.addRow(row);

        JTable table = styledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220)));

        JLabel countLabel = new JLabel(rows.size() + " rows returned");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        countLabel.setForeground(new Color(150, 150, 150));
        countLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        panel.add(desc, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);
        return panel;
    }

    // Analytics: Available Crew with interactive date pickers
    private JPanel buildAvailableCrewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>Personnel with NO conflicting assignments in a given date range (NOT EXISTS subquery)</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setBackground(Color.WHITE);
        filterBar.add(new JLabel("Check availability from:"));
        JSpinner startF = new JSpinner(new SpinnerDateModel());
        startF.setEditor(new JSpinner.DateEditor(startF, "yyyy-MM-dd"));
        JSpinner endF = new JSpinner(new SpinnerDateModel());
        endF.setEditor(new JSpinner.DateEditor(endF, "yyyy-MM-dd"));
        filterBar.add(startF);
        filterBar.add(new JLabel("to:"));
        filterBar.add(endF);

        JButton runBtn = new JButton("Run Query");
        runBtn.setBackground(new Color(55, 138, 221));
        runBtn.setForeground(Color.WHITE);
        runBtn.setBorderPainted(false);
        filterBar.add(runBtn);

        String[] cols = {"ID", "Name", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = styledTable(model);
        JLabel countLbl = new JLabel("Enter dates and click Run Query");
        countLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        countLbl.setForeground(new Color(150, 150, 150));

        runBtn.addActionListener(e -> {
            model.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String s = sdf.format(startF.getValue());
            String ed = sdf.format(endF.getValue());
            List<String[]> rows = advDAO.getAvailablePersonnel(s, ed);
            for (String[] row : rows) model.addRow(row);
            countLbl.setText(rows.size() + " available crew members in this period");
            countLbl.setForeground(rows.isEmpty() ? new Color(192, 57, 43) : new Color(15, 110, 86));
        });

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setBackground(Color.WHITE);
        top.add(desc, BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(countLbl, BorderLayout.SOUTH);
        return panel;
    }

    // Analytics: Vessel Utilization with a simple Swing bar chart
    private JPanel buildVesselUtilizationTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>Mission count and total sea-days per vessel (GROUP BY + DATEDIFF aggregate)</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 10, 0));

        List<String[]> rows = advDAO.getVesselUtilization();

        // Table
        String[] cols = {"Vessel Name", "Missions", "Total Sea-Days", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) model.addRow(row);
        JTable table = styledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 160));

        // Inline bar chart panel
        JPanel chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (rows.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int maxDays = rows.stream().mapToInt(r -> {
                    try { return Integer.parseInt(r[2]); } catch (Exception e) { return 0; }
                }).max().orElse(1);
                if (maxDays == 0) maxDays = 1;

                int barH = 28, gap = 10, labelW = 140, chartW = getWidth() - labelW - 60;
                Color[] palette = {
                    new Color(29, 158, 117), new Color(55, 138, 221),
                    new Color(99, 153, 34),  new Color(226, 150, 50),
                    new Color(155, 89, 182), new Color(52, 152, 219)
                };

                for (int i = 0; i < rows.size(); i++) {
                    int y = 10 + i * (barH + gap);
                    String name = rows.get(i)[0];
                    int days = 0;
                    try { days = Integer.parseInt(rows.get(i)[2]); } catch (Exception ignored) {}
                    int barLen = (int) ((double) days / maxDays * chartW);

                    // Label
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2.setColor(new Color(60, 60, 60));
                    g2.drawString(name.length() > 18 ? name.substring(0, 18) + "…" : name, 4, y + barH - 8);

                    // Bar
                    Color c = palette[i % palette.length];
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(labelW, y, Math.max(barLen, 4), barH, 6, 6));

                    // Value label
                    g2.setColor(new Color(40, 40, 40));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2.drawString(days + " days", labelW + Math.max(barLen, 4) + 6, y + barH - 8);
                }
            }
            public Dimension getPreferredSize() {
                return new Dimension(500, rows.size() * 38 + 20);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setBorder(new LineBorder(new Color(220, 220, 220)));
        chartScroll.setPreferredSize(new Dimension(0, 200));

        JLabel chartTitle = new JLabel("  Sea-Days per Vessel (bar chart)");
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        chartTitle.setForeground(new Color(80, 80, 80));
        chartTitle.setBorder(new EmptyBorder(8, 0, 4, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(desc, BorderLayout.NORTH);
        top.add(scroll, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(chartTitle, BorderLayout.CENTER);

        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(Color.WHITE);
        chartWrapper.add(chartScroll, BorderLayout.CENTER);
        panel.add(chartWrapper, BorderLayout.SOUTH);

        return panel;
    }

    // Analytics: Expiring Certifications with color-coded rows
    private JPanel buildExpiringCertsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>Crew certifications expiring within the next 60 days (DATE_ADD, ORDER BY expiry)</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 10, 0));

        List<String[]> rows = advDAO.getExpiringCertifications();
        String[] cols = {"Name", "Role", "Certification", "Expiry Date", "Days Left"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) model.addRow(row);
        JTable table = styledTable(model);

        // Color-code "Days Left"
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                try {
                    int days = Integer.parseInt(s.replace(" days", "").trim());
                    if (days <= 14)      setForeground(new Color(192, 57, 43));
                    else if (days <= 30) setForeground(new Color(180, 100, 0));
                    else                 setForeground(new Color(55, 138, 221));
                } catch (Exception ignored) {}
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        JLabel note = new JLabel(rows.isEmpty()
            ? "  ✅  No certifications expiring in the next 60 days."
            : "  ⚠  " + rows.size() + " certification(s) expiring soon. Red = < 14 days.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        note.setForeground(rows.isEmpty() ? new Color(15, 110, 86) : new Color(192, 57, 43));
        note.setBorder(new EmptyBorder(6, 0, 0, 0));

        panel.add(desc, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    // Analytics: Top Crew by mission count
    private JPanel buildTopCrewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>Top personnel ranked by number of mission assignments (GROUP BY + ORDER BY COUNT DESC, LIMIT 10)</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 10, 0));

        List<String[]> rows = advDAO.getTopPersonnelByMissions();
        String[] cols = {"Name", "Role", "Missions Assigned"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) model.addRow(row);
        JTable table = styledTable(model);

        // Bold top row
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(getFont().deriveFont(row == 0 ? Font.BOLD : Font.PLAIN));
                setForeground(row == 0 ? new Color(29, 158, 117) : new Color(40, 40, 40));
                return this;
            }
        });

        panel.add(desc, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Analytics: Mission Status Breakdown with visual bars
    private JPanel buildStatusBreakdownTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel desc = new JLabel("<html><i style='color:#888'>Count of missions grouped by status (GROUP BY status)</i></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBorder(new EmptyBorder(0, 0, 10, 0));

        List<String[]> rows = advDAO.getMissionStatusBreakdown();

        // Table
        String[] cols = {"Status", "Count"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) model.addRow(row);
        JTable table = styledTable(model);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = String.valueOf(val);
                switch (s) {
                    case "APPROVED":  setForeground(new Color(15, 110, 86));  break;
                    case "REJECTED":  setForeground(new Color(192, 57, 43));  break;
                    case "PENDING":   setForeground(new Color(160, 100, 0));  break;
                    case "COMPLETED": setForeground(new Color(55, 138, 221)); break;
                    default:          setForeground(new Color(60, 60, 60));
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 130));

        // Inline bar chart
        Map<String, Color> statusColors = new java.util.LinkedHashMap<>();
        statusColors.put("APPROVED",  new Color(29, 158, 117));
        statusColors.put("PENDING",   new Color(226, 150, 50));
        statusColors.put("REJECTED",  new Color(192, 57, 43));
        statusColors.put("COMPLETED", new Color(55, 138, 221));
        statusColors.put("CANCELLED", new Color(160, 160, 160));

        JPanel chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (rows.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int maxCnt = rows.stream().mapToInt(r -> {
                    try { return Integer.parseInt(r[1]); } catch (Exception e) { return 0; }
                }).max().orElse(1);
                if (maxCnt == 0) maxCnt = 1;
                int barH = 32, gap = 10, labelW = 110, chartW = getWidth() - labelW - 70;

                for (int i = 0; i < rows.size(); i++) {
                    String status = rows.get(i)[0];
                    int cnt = 0;
                    try { cnt = Integer.parseInt(rows.get(i)[1]); } catch (Exception ignored) {}
                    int y = 10 + i * (barH + gap);
                    int barLen = (int) ((double) cnt / maxCnt * chartW);

                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.setColor(new Color(60, 60, 60));
                    g2.drawString(status, 4, y + barH - 9);

                    Color c = statusColors.getOrDefault(status, new Color(120, 120, 120));
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(labelW, y, Math.max(barLen, 4), barH, 8, 8));

                    g2.setColor(new Color(40, 40, 40));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString(cnt + " missions", labelW + Math.max(barLen, 4) + 8, y + barH - 9);
                }
            }
            public Dimension getPreferredSize() {
                return new Dimension(500, rows.size() * 42 + 20);
            }
        };
        chartPanel.setBackground(Color.WHITE);

        JLabel chartTitle = new JLabel("  Mission Distribution by Status");
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        chartTitle.setForeground(new Color(80, 80, 80));
        chartTitle.setBorder(new EmptyBorder(10, 0, 6, 0));

        panel.add(desc, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.add(tableScroll, BorderLayout.NORTH);
        center.add(chartTitle, BorderLayout.CENTER);
        center.add(new JScrollPane(chartPanel), BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ── SHARED TABLE STYLING ─────────────────────────────────────────
    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(225, 245, 238));
        table.setSelectionForeground(new Color(15, 110, 86));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 248, 248));
        table.getTableHeader().setForeground(new Color(100, 100, 100));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        table.setFillsViewportHeight(true);
        return table;
    }

    private void refreshDashboard() {
        currentNav = "Dashboard";
        contentArea.removeAll();
        contentArea.add(buildDashboardPanel(), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }
}