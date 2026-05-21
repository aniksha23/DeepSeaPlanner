package com.deepsea.main;

import javax.swing.SwingUtilities;
import com.deepsea.ui.DashboardFrame;

public class Main {
    public static void main(String[] args) {
        // Launch the graphical user interface
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            }
        });
    }
}