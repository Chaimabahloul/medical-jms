package com.medical.jms.ui;

import com.medical.jms.rmi.MedicalService;
import com.medical.jms.rmi.MedicalRecord;
import com.medical.jms.rmi.MedicalStatistics;
import com.medical.jms.model.MedicalResult;  // IMPORT AJOUTÉ

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MockRMIServer implements MedicalService {

    private List<MedicalRecord> records = new ArrayList<>();
    private int recordCount = 0;
    private int criticalCount = 0;
    private int pendingAlerts = 0;

    public MockRMIServer() {
        // Ajouter des données de test initiales
        addTestRecord("Ali Ben Mohamed", "Glycémie", 1.30, "g/L", true);
        addTestRecord("Fatima Zohra", "Tension", 120.0, "mmHg", false);
        addTestRecord("Mohamed Ali", "Cholestérol", 1.80, "g/L", false);
        addTestRecord("Sophie Martin", "Température", 39.2, "°C", true);

        System.out.println(" Mock RMIServer initialisé avec " + records.size() + " enregistrements");
    }

    private void addTestRecord(String patient, String test, double value, String unit, boolean critical) {
        MedicalRecord record = new MedicalRecord(patient, test, value, unit, critical);
        records.add(record);
        recordCount++;
        if (critical) {
            criticalCount++;
            pendingAlerts++;
        }
    }

    // ===== MÉTHODES EXISTANTES (déjà implémentées) =====

    @Override
    public String addMedicalResult(String patient, String test, double value) throws RemoteException {
        // Déterminer l'unité selon le test
        String unit = getUnitForTest(test);
        boolean isCritical = isCriticalValue(test, value);

        // Créer le nouvel enregistrement
        MedicalRecord newRecord = new MedicalRecord(patient, test, value, unit, isCritical);
        records.add(newRecord);
        recordCount++;
        if (isCritical) {
            criticalCount++;
            pendingAlerts++;
        }

        // Journalisation
        System.out.println("📡 [MOCK RMI] Résultat ajouté: " + patient);
        System.out.println("   Test: " + test + ", Valeur: " + value + " " + unit);
        System.out.println("   Statut: " + (isCritical ? "CRITIQUE" : "Normal"));

        // Si critique, afficher une alerte
        if (isCritical) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        " ALERTE CRITIQUE (MOCK RMI)!\n\n" +
                                "Patient: " + patient + "\n" +
                                "Test: " + test + "\n" +
                                "Valeur: " + value + " " + unit + "\n\n" +
                                "Cette alerte est simulée via MockRMIServer",
                        "ALERTE MÉDICALE SIMULÉE",
                        JOptionPane.WARNING_MESSAGE);
            });
        }

        // Retourner la confirmation
        return String.format(
                " Résultat ajouté avec succès (Mode Simulation)!\n\n" +
                        "Patient: %s\n" +
                        "Test: %s\n" +
                        " Valeur: %.2f %s\n" +
                        " Statut: %s\n\n" +
                        " Statistiques actuelles:\n" +
                        "   • Total enregistrements: %d\n" +
                        "   • Alertes critiques: %d",
                patient, test, value, unit,
                isCritical ? "CRITIQUE" : "Normal",
                recordCount, criticalCount
        );
    }

    @Override
    public List<MedicalRecord> getPatientResults(String patient) throws RemoteException {
        List<MedicalRecord> patientRecords = new ArrayList<>();

        for (MedicalRecord record : records) {
            if (record.getPatientName().equalsIgnoreCase(patient)) {
                patientRecords.add(record);
            }
        }

        System.out.println("[MOCK RMI] Recherche patient: " + patient);
        System.out.println("   Résultats trouvés: " + patientRecords.size());

        return patientRecords;
    }

    @Override
    public List<MedicalRecord> getCriticalResults() throws RemoteException {
        List<MedicalRecord> criticals = new ArrayList<>();

        for (MedicalRecord record : records) {
            if (record.isCritical()) {
                criticals.add(record);
            }
        }

        System.out.println(" [MOCK RMI] Alertes critiques: " + criticals.size());

        return criticals;
    }

    @Override
    public MedicalStatistics getStatistics() throws RemoteException {
        double totalValue = 0;

        for (MedicalRecord record : records) {
            totalValue += record.getValue();
        }

        double average = recordCount > 0 ? totalValue / recordCount : 0;

        System.out.println("[MOCK RMI] Génération statistiques:");
        System.out.println("   Total: " + recordCount);
        System.out.println("   Critiques: " + criticalCount);
        System.out.println("   Moyenne: " + average);

        return new MedicalStatistics(recordCount, criticalCount, average);
    }

    @Override
    public boolean isAlive() throws RemoteException {
        System.out.println("  [MOCK RMI] Vérification status: SERVEUR ACTIF");
        return true;
    }

    // ===== NOUVELLES MÉTHODES (AJOUTÉES POUR LA COMPATIBILITÉ) =====

    @Override
    public String getSystemStatus() throws RemoteException {
        return String.format(
                " STATUT MOCK RMI SERVER\n" +
                        "══════════════════════════════\n" +
                        "Enregistrements: %d\n" +
                        "Résultats critiques: %d\n" +
                        "Alertes en attente: %d\n" +
                        "JMS ActiveMQ:  DÉSACTIVÉ (MOCK)\n" +
                        "Service RMI:  ACTIF (SIMULATION)\n" +
                        "══════════════════════════════",
                recordCount, criticalCount, pendingAlerts
        );
    }

    @Override
    public String sendTestResult(MedicalResult result) throws RemoteException {
        if (result == null) {
            return " ERREUR: MedicalResult est null";
        }

        // Convertir MedicalResult en MedicalRecord
        MedicalRecord record = new MedicalRecord(
                result.getPatientName(),
                result.getTestType(),
                result.getValue(),
                result.getUnit(),
                result.isCritical()
        );

        records.add(record);
        recordCount++;

        if (result.isCritical()) {
            criticalCount++;
            pendingAlerts++;
        }

        System.out.println(" [MOCK RMI] Test reçu: " + result.getPatientName());

        return String.format(
                " TEST REÇU (MODE SIMULATION)\n\n" +
                        "Patient: %s (%s)\n" +
                        "Test: %s\n" +
                        "Valeur: %.2f %s\n" +
                        "Statut: %s\n" +
                        "Mode: SIMULATION MOCK RMI",
                result.getPatientName(),
                result.getPatientId(),
                result.getTestType(),
                result.getValue(),
                result.getUnit(),
                result.isCritical() ? "CRITIQUE" : "Normal"
        );
    }

    @Override
    public int getTotalResults() throws RemoteException {
        return recordCount;
    }

    @Override
    public int getCriticalCount() throws RemoteException {
        return criticalCount;
    }

    @Override
    public int getPendingAlerts() throws RemoteException {
        return pendingAlerts;
    }

    @Override
    public List<MedicalResult> getRecentResults(int limit) throws RemoteException {
        // Vérifier la limite
        int actualLimit = Math.min(limit, records.size());
        if (actualLimit <= 0) {
            return new ArrayList<>();
        }

        // Prendre les 'limit' derniers résultats
        int start = records.size() - actualLimit;
        List<MedicalRecord> recentRecords = records.subList(start, records.size());

        // Convertir en MedicalResult
        List<MedicalResult> results = new ArrayList<>();
        for (MedicalRecord record : recentRecords) {
            results.add(convertToMedicalResult(record));
        }

        System.out.println("📄 [MOCK RMI] Derniers résultats: " + results.size());

        return results;
    }

    // ===== MÉTHODES UTILITAIRES PRIVÉES =====

    private String getUnitForTest(String test) {
        if (test == null) return "unit";

        String testLower = test.toLowerCase();

        if (testLower.contains("glycémie") || testLower.contains("glucose")) return "g/L";
        if (testLower.contains("cholestérol") || testLower.contains("cholesterol")) return "g/L";
        if (testLower.contains("tension") || testLower.contains("pressure")) return "mmHg";
        if (testLower.contains("température") || testLower.contains("temperature")) return "°C";
        if (testLower.contains("créatinine") || testLower.contains("creatinine")) return "mg/dL";

        return "unit";
    }

    private boolean isCriticalValue(String test, double value) {
        if (test == null) return false;

        String testLower = test.toLowerCase();

        if (testLower.contains("glycémie") || testLower.contains("glucose")) {
            return value > 1.26; // Diabète
        }
        if (testLower.contains("cholestérol") || testLower.contains("cholesterol")) {
            return value > 2.0; // Hypercholestérolémie
        }
        if (testLower.contains("tension") || testLower.contains("pressure")) {
            return value > 140; // Hypertension
        }
        if (testLower.contains("température") || testLower.contains("temperature")) {
            return value > 38.5; // Fièvre élevée
        }
        if (testLower.contains("créatinine") || testLower.contains("creatinine")) {
            return value > 13.0; // Insuffisance rénale
        }

        return false;
    }

    private MedicalResult convertToMedicalResult(MedicalRecord record) {
        // Générer un ID patient basé sur le nom
        String patientId = generatePatientId(record.getPatientName());

        return new MedicalResult(
                patientId,
                record.getPatientName(),
                record.getTestType(),
                record.getValue(),
                record.getUnit(),
                record.isCritical()
        );
    }

    private String generatePatientId(String patientName) {
        if (patientName == null || patientName.isEmpty()) {
            return "PAT" + System.currentTimeMillis() % 10000;
        }

        // Extraire les initiales
        String[] parts = patientName.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }

        String idBase = initials.toString().toUpperCase();
        if (idBase.isEmpty()) {
            idBase = "PAT";
        }

        return idBase + "_" + (System.currentTimeMillis() % 10000);
    }

    // ===== MÉTHODE MAIN POUR TESTER =====

    public static void main(String[] args) {
        try {
            System.out.println("🧪 TEST MOCK RMISERVER");
            System.out.println("======================");

            MockRMIServer server = new MockRMIServer();

            // Test 1: Vérifier status
            System.out.println("\n1. Test isAlive():");
            boolean alive = server.isAlive();
            System.out.println("   Résultat: " + (alive ? "✅ ACTIF" : "❌ INACTIF"));

            // Test 2: Ajouter un résultat
            System.out.println("\n2. Test addMedicalResult():");
            String result = server.addMedicalResult("Jean Test", "Glycémie", 1.40);
            System.out.println("   Réponse: " + result.split("\n")[0]);

            // Test 3: Obtenir statistiques
            System.out.println("\n3. Test getStatistics():");
            MedicalStatistics stats = server.getStatistics();
            System.out.println("   Statistiques: " + stats);

            // Test 4: Obtenir alertes critiques
            System.out.println("\n4. Test getCriticalResults():");
            List<MedicalRecord> criticals = server.getCriticalResults();
            System.out.println("   Alertes critiques: " + criticals.size());
            for (MedicalRecord record : criticals) {
                System.out.println("   • " + record);
            }

            // Test 5: Nouvelle méthode getSystemStatus
            System.out.println("\n5. Test getSystemStatus():");
            String status = server.getSystemStatus();
            System.out.println(status);

            // Test 6: Nouvelle méthode getTotalResults
            System.out.println("\n6. Test getTotalResults():");
            int total = server.getTotalResults();
            System.out.println("   Total: " + total);

            // Test 7: Nouvelle méthode getCriticalCount
            System.out.println("\n7. Test getCriticalCount():");
            int critical = server.getCriticalCount();
            System.out.println("   Critiques: " + critical);

            // Test 8: Nouvelle méthode getPendingAlerts
            System.out.println("\n8. Test getPendingAlerts():");
            int pending = server.getPendingAlerts();
            System.out.println("   En attente: " + pending);

            // Test 9: Nouvelle méthode getRecentResults
            System.out.println("\n9. Test getRecentResults():");
            List<MedicalResult> recent = server.getRecentResults(2);
            System.out.println("   Récents: " + recent.size() + " résultats");

            // Test 10: Nouvelle méthode sendTestResult
            System.out.println("\n10. Test sendTestResult():");
            MedicalResult testResult = new MedicalResult(
                    "PAT_TEST",
                    "Patient Test",
                    "Cholestérol",
                    2.5,
                    "g/L",
                    true
            );
            String sendResult = server.sendTestResult(testResult);
            System.out.println("   " + sendResult.split("\n")[0]);

            System.out.println("\ntest MOCK RMISERVER RÉUSSI!");

        } catch (RemoteException e) {
            System.err.println(" Erreur test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}