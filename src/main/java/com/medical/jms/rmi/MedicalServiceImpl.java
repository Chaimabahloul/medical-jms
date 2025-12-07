package com.medical.jms.rmi;

import com.medical.jms.model.MedicalResult;
import com.medical.jms.service.AlertService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class MedicalServiceImpl extends UnicastRemoteObject implements MedicalService {

    private final List<MedicalRecord> records = new ArrayList<>();
    private AlertService alertService;
    private int pendingAlerts = 0;

    public MedicalServiceImpl() throws RemoteException {
        super();

        // Initialiser AlertService pour JMS
        initializeAlertService();

        // Données de test
        records.add(new MedicalRecord("Ali Ben Mohamed", "Glycémie", 1.30, "g/L", true));
        records.add(new MedicalRecord("Fatima Zohra", "Tension", 120.0, "mmHg", false));

        System.out.println(" Serveur RMI MedicalService prêt");
        System.out.println(" Enregistrements initiaux: " + records.size());
    }

    private void initializeAlertService() {
        try {
            this.alertService = new AlertService();
            System.out.println(" AlertService JMS initialisé");
        } catch (Exception e) {
            System.err.println("  AlertService non disponible: " + e.getMessage());
            this.alertService = null;
        }
    }

    // ===== MÉTHODES PRINCIPALES =====

    @Override
    public String addMedicalResult(String patient, String test, double value)
            throws RemoteException {

        String unit = getUnitForTest(test);
        boolean isCritical = isCriticalValue(test, value);

        // Créer et sauvegarder MedicalRecord
        MedicalRecord record = new MedicalRecord(patient, test, value, unit, isCritical);
        records.add(record);

        // Traiter le résultat (conversion + JMS)
        processMedicalResult(record, isCritical);

        System.out.println("📡 RMI: Résultat ajouté - " + patient + " (" + test + ")");

        return String.format(" Patient: %s\n Test: %s\n📊 Valeur: %.2f %s\n⚠ Statut: %s\n📨 JMS: %s",
                patient, test, value, unit,
                isCritical ? "CRITIQUE" : "Normal",
                alertService != null ? "Message envoyé" : "JMS non disponible");
    }

    @Override
    public String sendTestResult(MedicalResult medicalResult) throws RemoteException {
        if (medicalResult == null) {
            return "ERREUR: MedicalResult est null";
        }

        // Convertir MedicalResult en MedicalRecord
        MedicalRecord record = convertToMedicalRecord(medicalResult);
        records.add(record);

        // Traiter le résultat
        processMedicalResult(record, medicalResult.isCritical());

        return String.format(
                " TEST ENVOYÉ AVEC SUCCÈS\n\n" +
                        "Patient: %s (%s)\n" +
                        "Test: %s\n" +
                        "Valeur: %.2f %s\n" +
                        "Statut: %s\n" +
                        "JMS: %s",
                medicalResult.getPatientName(),
                medicalResult.getPatientId(),
                medicalResult.getTestType(),
                medicalResult.getValue(),
                medicalResult.getUnit(),
                medicalResult.isCritical() ? "CRITIQUE" : "Normal",
                alertService != null ? "Message JMS envoyé" : "JMS non disponible"
        );
    }

    // ===== MÉTHODES DE TRAITEMENT COMMUNES =====

    private void processMedicalResult(MedicalRecord record, boolean isCritical) {
        // Incrémenter alertes en attente si critique
        if (isCritical) {
            pendingAlerts++;
        }

        // Convertir et envoyer via JMS si disponible
        if (alertService != null) {
            try {
                MedicalResult jmsResult = convertToMedicalResult(record);
                alertService.addAlert(jmsResult);
                System.out.println(" Résultat envoyé via JMS: " +
                        record.getPatientName() + " - " + record.getTestType());
            } catch (Exception e) {
                System.err.println(" Erreur envoi JMS: " + e.getMessage());
            }
        }
    }

    // ===== MÉTHODES DE RECHERCHE =====

    @Override
    public List<MedicalRecord> getPatientResults(String patient) throws RemoteException {
        List<MedicalRecord> patientRecords = new ArrayList<>();
        for (MedicalRecord record : records) {
            if (record.getPatientName().equalsIgnoreCase(patient)) {
                patientRecords.add(record);
            }
        }
        return patientRecords;
    }

    @Override
    public List<MedicalRecord> getCriticalResults() throws RemoteException {
        return getRecordsByCriticalStatus(true);
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

        return results;
    }

    // ===== MÉTHODES STATISTIQUES =====

    @Override
    public MedicalStatistics getStatistics() throws RemoteException {
        int[] stats = calculateStatistics();
        int total = stats[0];
        int critical = stats[1];
        double sum = stats[2];

        double average = total > 0 ? sum / total : 0;
        return new MedicalStatistics(total, critical, average);
    }

    @Override
    public int getTotalResults() throws RemoteException {
        return records.size();
    }

    @Override
    public int getCriticalCount() throws RemoteException {
        return calculateStatistics()[1];
    }

    @Override
    public int getPendingAlerts() throws RemoteException {
        return pendingAlerts;
    }

    @Override
    public String getSystemStatus() throws RemoteException {
        int[] stats = calculateStatistics();
        int totalRecords = stats[0];
        int criticalRecords = stats[1];

        String jmsStatus = (alertService != null) ? "✅ CONNECTÉ" : "❌ DÉCONNECTÉ";

        return String.format(
                "🏥 STATUT DU SYSTÈME MÉDICAL\n" +
                        "══════════════════════════════\n" +
                        "Enregistrements: %d\n" +
                        " Résultats critiques: %d\n" +
                        " Alertes en attente: %d\n" +
                        "JMS ActiveMQ: %s\n" +
                        " Service RMI: ACTIF\n" +
                        "══════════════════════════════",
                totalRecords, criticalRecords, pendingAlerts, jmsStatus
        );
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    // ===== MÉTHODES UTILITAIRES PRIVÉES =====

    private int[] calculateStatistics() {
        int total = records.size();
        int critical = 0;
        double sum = 0;

        for (MedicalRecord record : records) {
            if (record.isCritical()) {
                critical++;
            }
            sum += record.getValue();
        }

        return new int[]{total, critical, (int)sum};
    }

    private List<MedicalRecord> getRecordsByCriticalStatus(boolean critical) {
        List<MedicalRecord> result = new ArrayList<>();
        for (MedicalRecord record : records) {
            if (record.isCritical() == critical) {
                result.add(record);
            }
        }
        return result;
    }

    private MedicalRecord convertToMedicalRecord(MedicalResult result) {
        return new MedicalRecord(
                result.getPatientName(),
                result.getTestType(),
                result.getValue(),
                result.getUnit(),
                result.isCritical()
        );
    }

    private MedicalResult convertToMedicalResult(MedicalRecord record) {
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
            return value > 1.26;
        }
        if (testLower.contains("cholestérol") || testLower.contains("cholesterol")) {
            return value > 2.0;
        }
        if (testLower.contains("tension") || testLower.contains("pressure")) {
            return value > 140;
        }
        if (testLower.contains("température") || testLower.contains("temperature")) {
            return value > 38.5;
        }
        if (testLower.contains("créatinine") || testLower.contains("creatinine")) {
            return value > 13.0;
        }

        return false;
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
            System.out.println("🧪 TEST MEDICALSERVICEIMPL");
            System.out.println("===========================\n");

            MedicalServiceImpl service = new MedicalServiceImpl();

            runAllTests(service);

        } catch (Exception e) {
            handleTestError(e);
        }
    }

    private static void runAllTests(MedicalServiceImpl service) throws RemoteException {
        testAddMedicalResult(service);
        testGetStatistics(service);
        testGetSystemStatus(service);
        testTotalResults(service);
        testCriticalCount(service);
        testPendingAlerts(service);
        testRecentResults(service);
        testSendTestResult(service);

        System.out.println("\n TOUS LES TESTS RÉUSSIS !");
    }

    private static void testAddMedicalResult(MedicalServiceImpl service) throws RemoteException {
        System.out.println("1. Test addMedicalResult:");
        String result = service.addMedicalResult("Jean Test", "Glycémie", 1.40);
        System.out.println("   " + result.split("\n")[0]);
    }

    private static void testGetStatistics(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n2. Test getStatistics:");
        MedicalStatistics stats = service.getStatistics();
        System.out.println("   " + stats);
    }

    private static void testGetSystemStatus(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n3. Test getSystemStatus:");
        String status = service.getSystemStatus();
        System.out.println("   " + status.split("\n")[0]);
        System.out.println("   " + status.split("\n")[1]);
    }

    private static void testTotalResults(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n4. Test getTotalResults:");
        int total = service.getTotalResults();
        System.out.println("   Total: " + total);
    }

    private static void testCriticalCount(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n5. Test getCriticalCount:");
        int critical = service.getCriticalCount();
        System.out.println("   Critiques: " + critical);
    }

    private static void testPendingAlerts(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n6. Test getPendingAlerts:");
        int pending = service.getPendingAlerts();
        System.out.println("   En attente: " + pending);
    }

    private static void testRecentResults(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n7. Test getRecentResults:");
        List<MedicalResult> recent = service.getRecentResults(3);
        System.out.println("   Récents: " + recent.size() + " résultats");
        for (MedicalResult res : recent) {
            System.out.println("   • " + res.getPatientName() + " - " + res.getTestType());
        }
    }

    private static void testSendTestResult(MedicalServiceImpl service) throws RemoteException {
        System.out.println("\n8. Test sendTestResult:");
        MedicalResult testResult = new MedicalResult(
                "PAT_TEST",
                "Patient Test",
                "Cholestérol",
                2.5,
                "g/L",
                true
        );
        String sendResult = service.sendTestResult(testResult);
        System.out.println("   " + sendResult.split("\n")[0]);
    }

    private static void handleTestError(Exception e) {
        System.err.println(" ERREUR TEST: " + e.getMessage());
        System.err.println("   Type d'erreur: " + e.getClass().getSimpleName());

        // Logging ciblé seulement pour les classes medical
        boolean foundMedicalError = false;
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().contains("medical")) {
                System.err.println("   -> " + element.getClassName() +
                        "." + element.getMethodName() +
                        " (ligne " + element.getLineNumber() + ")");
                foundMedicalError = true;
            }
        }

        if (!foundMedicalError) {
            System.err.println("   → Erreur externe au package medical");
        }
    }
}