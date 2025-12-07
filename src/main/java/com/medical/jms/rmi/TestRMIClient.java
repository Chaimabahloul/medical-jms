package com.medical.jms.rmi;

import com.medical.jms.model.MedicalResult;
import java.rmi.Naming;
import java.util.List;

public class TestRMIClient {
    public static void main(String[] args) {
        try {
            System.out.println(" TEST CLIENT RMI");
            System.out.println("==================");

            // 1. Se connecter au serveur RMI
            String serviceUrl = "rmi://localhost:1099/MedicalService";
            System.out.println(" Connexion à: " + serviceUrl);

            MedicalService service = (MedicalService) Naming.lookup(serviceUrl);
            System.out.println(" Connecté au serveur RMI");

            // 2. Tester le status (CORRIGÉ : assigner à une variable)
            System.out.println("\n Status système:");
            String status = service.getSystemStatus();  // ✅ CORRIGÉ
            System.out.println(status);

            // 3. Envoyer un résultat de test (CORRIGÉ : utiliser addMedicalResult)
            System.out.println("\n Envoi résultat test via addMedicalResult...");
            // Créer un résultat simple
            String result = service.addMedicalResult("Ali Test", "Glycémie", 1.30);
            System.out.println(" Résultat: " + result.split("\n")[0]);

            // 4. Envoyer un MedicalResult via sendTestResult
            System.out.println("\n Envoi MedicalResult via sendTestResult...");
            MedicalResult testResult = new MedicalResult(
                    "PAT001", "Ali Test", "Glycémie", 1.30, "g/L", true
            );
            String sendResult = service.sendTestResult(testResult);
            System.out.println(" Résultat: " + sendResult.split("\n")[0]);

            // 5. Récupérer les statistiques (CORRIGÉ)
            System.out.println("\nStatistiques:");
            MedicalStatistics stats = service.getStatistics();
            System.out.println(stats);

            // 6. Récupérer les comptes (CORRIGÉ)
            System.out.println("\n Comptes:");
            System.out.println("Total résultats: " + service.getTotalResults());
            System.out.println("Alertes critiques: " + service.getCriticalCount());
            System.out.println("Alertes en attente: " + service.getPendingAlerts());

            // 7. Récupérer les résultats récents (CORRIGÉ)
            System.out.println("\n Derniers résultats (max 3):");
            List<MedicalResult> recent = service.getRecentResults(3);
            for (MedicalResult res : recent) {
                System.out.println("  • " + res.getPatientName() +
                        " - " + res.getTestType() +
                        ": " + res.getValue() + " " + res.getUnit());
            }

            // 8. Tester getPatientResults
            System.out.println("\n Recherche patient 'Ali':");
            List<MedicalRecord> patientResults = service.getPatientResults("Ali");
            System.out.println("  Trouvé: " + patientResults.size() + " résultats");

            // 9. Tester getCriticalResults
            System.out.println("\n Résultats critiques:");
            List<MedicalRecord> criticals = service.getCriticalResults();
            System.out.println("  Total critiques: " + criticals.size());

            // 10. Tester isAlive
            System.out.println("\n Vérification status service:");
            boolean alive = service.isAlive();
            System.out.println("  Service actif: " + (alive ? "✅ OUI" : "❌ NON"));

            System.out.println("\n TEST RÉUSSI !");

        } catch (Exception e) {
            System.err.println(" ERREUR CLIENT: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nVérifiez que:");
            System.err.println("1. RMIServer est démarré (port 1099)");
            System.err.println("2. ActiveMQ est démarré (port 61616)");
            System.err.println("3. L'URL RMI est correcte: rmi://localhost:1099/MedicalService");
        }
    }
}