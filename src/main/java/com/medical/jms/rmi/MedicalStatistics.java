package com.medical.jms.rmi;

import java.io.Serializable;

public class MedicalStatistics implements Serializable {
    private int totalRecords;
    private int criticalRecords;
    private double averageValue;

    public MedicalStatistics(int totalRecords, int criticalRecords, double averageValue) {
        this.totalRecords = totalRecords;
        this.criticalRecords = criticalRecords;
        this.averageValue = averageValue;
    }

    // Getters
    public int getTotalRecords() { return totalRecords; }
    public int getCriticalRecords() { return criticalRecords; }
    public double getAverageValue() { return averageValue; }

    @Override
    public String toString() {
        return String.format(
                " STATISTIQUES MÉDICALES\n" +
                        "─────────────────────────\n" +
                        " Total enregistrements: %d\n" +
                        " Alertes critiques: %d\n" +
                        " Valeur moyenne: %.2f\n" +
                        "─────────────────────────",
                totalRecords, criticalRecords, averageValue
        );
    }
}