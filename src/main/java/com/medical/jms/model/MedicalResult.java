package com.medical.jms.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MedicalResult implements Serializable {
    private String id;
    private String patientName;
    private String patientId;
    private String testType;
    private double value;
    private String unit;
    private String referenceRange;
    private boolean isCritical;
    private Date timestamp;
    private String doctorNotes;

    // Constructeur PRINCIPAL - avec patientId en premier
    public MedicalResult(String patientId, String patientName, String testType,
                         double value, String unit, boolean isCritical) {
        this.id = "MED" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.patientId = patientId;
        this.patientName = patientName;
        this.testType = testType;
        this.value = value;
        this.unit = unit;
        this.isCritical = isCritical;
        this.timestamp = new Date();
        this.referenceRange = getDefaultReferenceRange(testType);
        this.doctorNotes = "";
        System.out.println(" MedicalResult créé: " + patientName + " - " + testType);
    }

    // Constructeur ALTERNATIF - avec patientName en premier
    public MedicalResult(String patientName, String testType, double value,
                         String unit, boolean isCritical) {
        this(generatePatientId(patientName), patientName, testType, value, unit, isCritical);
    }

    // Constructeur ALTERNATIF 2 - pour compatibilité
    public MedicalResult(String patientId, String patientName, String testType,
                         String valueStr, String unit, String referenceRange, boolean isCritical) {
        this.id = "MED" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.patientId = patientId;
        this.patientName = patientName;
        this.testType = testType;

        try {
            this.value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            this.value = 0.0;
            System.err.println(" Erreur conversion valeur: " + valueStr + " -> 0.0");
        }

        this.unit = unit;
        this.referenceRange = (referenceRange != null && !referenceRange.isEmpty())
                ? referenceRange
                : getDefaultReferenceRange(testType);
        this.isCritical = isCritical;
        this.timestamp = new Date();
        this.doctorNotes = "";
    }

    // Méthode utilitaire pour générer un ID patient
    private static String generatePatientId(String patientName) {
        if (patientName == null || patientName.isEmpty()) {
            return "PAT" + System.currentTimeMillis();
        }
        // Générer ID à partir du nom (ex: "Ali Ben Mohamed" -> "PAT_ALI")
        String[] parts = patientName.split(" ");
        String prefix = parts.length > 0 ? parts[0].toUpperCase() : "PAT";
        return prefix + "_" + (System.currentTimeMillis() % 10000);
    }

    private String getDefaultReferenceRange(String testType) {
        if (testType == null) return "N/A";

        String testLower = testType.toLowerCase();

        if (testLower.contains("glycémie") || testLower.contains("glucose")) {
            return "0.70-1.10 g/L";
        }
        if (testLower.contains("cholestérol") || testLower.contains("cholesterol")) {
            return "< 2.0 g/L";
        }
        if (testLower.contains("tension") || testLower.contains("pressure")) {
            return "120/80 mmHg";
        }
        if (testLower.contains("température") || testLower.contains("temperature")) {
            return "36.5-37.5 °C";
        }
        if (testLower.contains("créatinine") || testLower.contains("creatinine")) {
            return "6-13 mg/dL";
        }

        return "N/A";
    }

    // GETTERS
    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getPatientId() { return patientId; }
    public String getTestType() { return testType; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getReferenceRange() {
        return (referenceRange != null && !referenceRange.isEmpty())
                ? referenceRange
                : getDefaultReferenceRange(testType);
    }
    public boolean isCritical() { return isCritical; }
    public Date getTimestamp() { return timestamp; }
    public String getDoctorNotes() {
        return (doctorNotes != null) ? doctorNotes : "";
    }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setTestType(String testType) {
        this.testType = testType;
        // Mettre à jour la plage de référence si elle était par défaut
        if (referenceRange == null || referenceRange.equals("N/A")) {
            this.referenceRange = getDefaultReferenceRange(testType);
        }
    }
    public void setValue(double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public void setCritical(boolean critical) { isCritical = critical; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    // Méthode utilitaire pour obtenir la valeur en String formatée
    public String getFormattedValue() {
        return String.format("%.2f", value);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(timestamp);

        return String.format("[%s] %s (%s) - %s: %.2f %s %s",
                time, patientName, patientId, testType, value, unit,
                isCritical ? " CRITIQUE" : "Normal");
    }

    // Méthode d'affichage détaillé
    public String toDetailedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return String.format(
                " DÉTAILS RÉSULTAT MÉDICAL\n" +
                        "════════════════════════════════\n" +
                        " ID: %s\n" +
                        " Patient: %s (%s)\n" +
                        "Test: %s\n" +
                        "Résultat: %.2f %s\n" +
                        "Plage référence: %s\n" +
                        " Statut: %s\n" +
                        "Date: %s\n" +
                        "Notes: %s\n" +
                        "════════════════════════════════",
                id, patientName, patientId, testType, value, unit,
                getReferenceRange(),
                isCritical ? " CRITIQUE" : " Normal",
                sdf.format(timestamp),
                getDoctorNotes()
        );
    }

    // Méthode de test
    public static void main(String[] args) {
        System.out.println(" TEST MEDICALRESULT");
        System.out.println("=====================\n");

        // Test 1: Constructeur principal
        System.out.println("1. Test constructeur principal:");
        MedicalResult result1 = new MedicalResult("PAT001", "Ali Ben Mohamed",
                "Glycémie", 1.45, "g/L", true);
        System.out.println("   " + result1);
        System.out.println("   ID: " + result1.getId());
        System.out.println("   Plage référence: " + result1.getReferenceRange());
        System.out.println("   Notes médecin: '" + result1.getDoctorNotes() + "'\n");

        // Test 2: Constructeur alternatif (patientName en premier)
        System.out.println("2. Test constructeur alternatif:");
        MedicalResult result2 = new MedicalResult("Fatima Zohra",
                "Tension", 125.0, "mmHg", false);
        System.out.println("   " + result2);
        System.out.println("   ID patient généré: " + result2.getPatientId() + "\n");

        // Test 3: Constructeur avec String value
        System.out.println("3. Test constructeur avec String value:");
        MedicalResult result3 = new MedicalResult("PAT003", "Mohamed Ali",
                "Cholestérol", "1.85", "g/L", "< 2.0 g/L", false);
        System.out.println("   " + result3);

        // CORRECTION ICI : Double.valueOf() pour obtenir la classe
        System.out.println("   Valeur: " + result3.getValue() + " (type: " +
                Double.valueOf(result3.getValue()).getClass().getSimpleName() + ")");

        // Test 4: Méthode d'affichage détaillé
        System.out.println("\n4. Test affichage détaillé:");
        System.out.println(result1.toDetailedString());

        // Test 5: Test de sérialisation
        System.out.println("\n5. Test sérialisation:");
        System.out.println("   Implémente Serializable: " +
                (result1 instanceof Serializable ? "✅ OUI" : "❌ NON"));

        System.out.println("\nTESTS RÉUSSIS!");
    }
}