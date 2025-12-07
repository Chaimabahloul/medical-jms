package com.medical.jms.service;

import com.medical.jms.config.JMSConstants;
import com.medical.jms.model.MedicalResult;
import com.medical.jms.producer.AlertProducer;
import com.medical.jms.producer.MedicalResultProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AlertService {

    // File d'attente locale (pour l'affichage UI et les Tests)
    private final ConcurrentLinkedQueue<MedicalResult> alertQueue;

    // Producteurs JMS (pour l'architecture distribuée)
    private AlertProducer alertProducer;
    private MedicalResultProducer resultProducer;
    private boolean jmsEnabled;

    public AlertService() {
        this.alertQueue = new ConcurrentLinkedQueue<>();
        this.jmsEnabled = false;

        try {
            // Initialisation des producteurs JMS
            this.alertProducer = new AlertProducer(JMSConstants.MEDICAL_ALERTS_QUEUE);
            this.resultProducer = new MedicalResultProducer(JMSConstants.MEDICAL_RESULTS_QUEUE);
            this.jmsEnabled = true;
            System.out.println(" AlertService: Mode Hybride (Local + JMS) activé");
        } catch (Exception e) {
            System.err.println(" AlertService: JMS non disponible, mode Local uniquement (" + e.getMessage() + ")");
            this.jmsEnabled = false;
        }
    }

    /**
     * Méthode principale qui gère la logique métier et l'envoi JMS
     */
    public void addAlert(MedicalResult result) {
        if (result == null) return;

        // 1. Logique JMS (Architecture)
        if (jmsEnabled) {
            try {
                // Toujours envoyer vers le stockage (MedicalResultProducer)
                resultProducer.sendResult(result);
                System.out.println(" [JMS] Résultat envoyé au système d'archivage");

                // Si critique, envoyer vers les alertes (AlertProducer)
                if (result.isCritical()) {
                    alertProducer.sendAlert(result);
                    System.out.println(" [JMS] ALERTE CRITIQUE envoyée au médecin");
                }
            } catch (Exception e) {
                System.err.println(" Erreur d'envoi JMS: " + e.getMessage());
            }
        }

        // 2. Logique Locale (Pour vos tests et l'interface graphique)
        if (result.isCritical()) {
            alertQueue.offer(result);
            System.out.println(" [LOCAL] Alerte ajoutée à la liste interne");
        }
    }

    // --- MÉTHODES REQUISES PAR TestAlertService.java ---

    public int getAlertCount() {
        return alertQueue.size();
    }

    public boolean hasAlerts() {
        return !alertQueue.isEmpty();
    }

    public List<MedicalResult> getPendingAlerts() {
        return new ArrayList<>(alertQueue);
    }

    public void acknowledgeAlert(String patientId) {
        if (patientId == null) return;
        // Supprime l'alerte de la liste locale si l'ID correspond
        boolean removed = alertQueue.removeIf(r -> r.getPatientId() != null && r.getPatientId().equals(patientId));
        if (removed) {
            System.out.println(" Alerte acquittée pour le patient: " + patientId);
        } else {
            System.out.println("Aucune alerte trouvée pour: " + patientId);
        }
    }

    public void clearAlerts() {
        alertQueue.clear();
        System.out.println(" Toutes les alertes locales ont été effacées.");
    }

    public void generateTestAlerts(int count) {
        for (int i = 0; i < count; i++) {
            MedicalResult mock = new MedicalResult(
                    "TEST_PAT_" + i,
                    "Patient Test " + i,
                    "Simulation",
                    100.0 + i,
                    "unit",
                    true // Critique pour qu'elle soit ajoutée
            );
            addAlert(mock);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
    }

    public void shutdown() {
        try {
            if (alertProducer != null) alertProducer.close();
            if (resultProducer != null) resultProducer.close();
            alertQueue.clear();
            System.out.println(" AlertService arrêté.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}