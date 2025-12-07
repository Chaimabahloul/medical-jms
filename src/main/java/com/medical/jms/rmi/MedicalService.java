package com.medical.jms.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// AJOUTER CET IMPORT :
import com.medical.jms.model.MedicalResult;  //  IMPORT MANQUANT

public interface MedicalService extends Remote {

    // Ajouter un résultat médical
    String addMedicalResult(String patient, String test, double value)
            throws RemoteException;

    // Récupérer les résultats d'un patient
    List<MedicalRecord> getPatientResults(String patient)
            throws RemoteException;

    // Récupérer tous les résultats critiques
    List<MedicalRecord> getCriticalResults() throws RemoteException;

    // Obtenir les statistiques
    MedicalStatistics getStatistics() throws RemoteException;

    // Vérifier si le service est en vie
    boolean isAlive() throws RemoteException;

    String getSystemStatus() throws RemoteException;

    // Maintenant MedicalResult est reconnu
    String sendTestResult(MedicalResult result) throws RemoteException;

    int getTotalResults() throws RemoteException;
    int getCriticalCount() throws RemoteException;
    int getPendingAlerts() throws RemoteException;

    // ✅ Maintenant MedicalResult est reconnu
    List<MedicalResult> getRecentResults(int limit) throws RemoteException;
}