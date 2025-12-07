package com.medical.jms.ui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Version SIMULÉE pour démo
    private List<String> medicalData = new ArrayList<>();

    public DatabaseManager() {
        System.out.println(" DatabaseManager initialisé (mode simulation)");

        // Données de test initiales
        addMockData("Ali Ben Mohamed", "Glycémie", "1.30", "g/L", true);
        addMockData("Fatima Zohra", "Tension", "120", "mmHg", false);
        addMockData("Mohamed Ali", "Cholestérol", "1.80", "g/L", false);
    }

    private void addMockData(String patient, String test, String value, String unit, boolean critical) {
        String data = patient + " | " + test + " | " + value + " " + unit + " | "
                + (critical ? "CRITIQUE" : "Normal");
        medicalData.add(data);
    }

    public boolean addMedicalResult(String patient, String test, String value) {
        // Déterminer l'unité selon le test
        String unit = getUnitForTest(test);
        boolean isCritical = isCriticalValue(test, value);

        String result = patient + " | " + test + " | " + value + " " + unit + " | "
                + (isCritical ? "CRITIQUE" : "Normal");

        medicalData.add(0, result); // Ajouter au début
        System.out.println(" Résultat ajouté: " + result);

        // Si critique, montrer popup
        if (isCritical) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        " ALERTE CRITIQUE!\n\n" +
                                "Patient: " + patient + "\n" +
                                "Test: " + test + "\n" +
                                "Valeur: " + value + " " + unit + "\n\n" +
                                "Veuillez intervenir immédiatement!",
                        "ALERTE MÉDICALE",
                        JOptionPane.WARNING_MESSAGE);
            });
        }

        return true;
    }

    public String getMedicalResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DERNIERS RÉSULTATS MÉDICAUX ===\n\n");

        int count = 0;
        for (String data : medicalData) {
            if (count >= 10) break;

            String[] parts = data.split(" \\| ");
            sb.append(" Patient: ").append(parts[0]).append("\n");
            sb.append("    Test: ").append(parts[1]).append("\n");
            sb.append("   Résultat: ").append(parts[2]).append("\n");
            sb.append("    Statut: ").append(parts[3]).append("\n\n");
            count++;
        }

        sb.append(" Total enregistrements: ").append(medicalData.size()).append("\n");
        return sb.toString();
    }

    public boolean checkConnection() {
        return true; // Toujours OK en simulation
    }

    private String getUnitForTest(String test) {
        if (test.toLowerCase().contains("glycémie")) return "g/L";
        if (test.toLowerCase().contains("cholestérol")) return "g/L";
        if (test.toLowerCase().contains("tension")) return "mmHg";
        if (test.toLowerCase().contains("température")) return "°C";
        return "unit";
    }

    private boolean isCriticalValue(String test, String value) {
        try {
            double val = Double.parseDouble(value);

            if (test.toLowerCase().contains("glycémie") && val > 1.26) return true;
            if (test.toLowerCase().contains("cholestérol") && val > 2.0) return true;
            if (test.toLowerCase().contains("tension") && val > 140) return true;
            if (test.toLowerCase().contains("température") && val > 38.5) return true;

            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}