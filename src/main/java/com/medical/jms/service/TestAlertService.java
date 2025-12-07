package com.medical.jms.service;

import com.medical.jms.model.MedicalResult;

public class TestAlertService {
    public static void main(String[] args) {
        System.out.println("TEST ALERT SERVICE");
        System.out.println("=====================\n");

        AlertService alertService = new AlertService();

        // 1. Tester l'ajout d'alertes
        System.out.println("1. Ajout d'alertes...");

        MedicalResult alert1 = new MedicalResult(
                "Ali Ben Mohamed",
                "PAT001",
                "Glycémie",
                1.45,
                "g/L",
                true
        );

        MedicalResult alert2 = new MedicalResult(
                "Fatima Zohra",
                "PAT002",
                "Tension",
                160.0,
                "mmHg",
                true
        );

        MedicalResult alert3 = new MedicalResult(
                "Mohamed Ali",
                "PAT003",
                "Créatinine",
                1.2,
                "mg/dL",
                false  // Pas critique
        );

        alertService.addAlert(alert1);
        alertService.addAlert(alert2);
        alertService.addAlert(alert3);

        // 2. Vérifier le nombre d'alertes
        System.out.println("\n2. Nombre d'alertes: " + alertService.getAlertCount());
        System.out.println("   A des alertes? " + alertService.hasAlerts());

        // 3. Lister les alertes en attente
        System.out.println("\n3. Alertes en attente:");
        for (MedicalResult alert : alertService.getPendingAlerts()) {
            System.out.println("   • " + alert);
        }

        // 4. Tester l'acquittement
        System.out.println("\n4. Acquittement alerte PAT001...");
        alertService.acknowledgeAlert("PAT001");
        System.out.println("   Alertes restantes: " + alertService.getAlertCount());

        // 5. Tester la génération d'alertes de test
        System.out.println("\n5. Génération alertes de test...");
        alertService.generateTestAlerts(3);

        // 6. Nettoyage
        System.out.println("\n6. Nettoyage...");
        alertService.clearAlerts();
        System.out.println("   Alertes restantes: " + alertService.getAlertCount());

        // 7. Fermer le service
        alertService.shutdown();

        System.out.println("\n TEST TERMINÉ");
    }
}