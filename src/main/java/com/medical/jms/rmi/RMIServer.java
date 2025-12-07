package com.medical.jms.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            // 1. Démarrer le registre RMI sur le port 1099
            System.out.println(" Démarrage du registre RMI...");
            LocateRegistry.createRegistry(1099);
            System.out.println("Registre RMI démarré sur le port 1099");

            // 2. Créer l'instance du service
            System.out.println(" Création du service médical...");
            MedicalService medicalService = new MedicalServiceImpl();

            // 3. Enregistrer le service dans le registre
            String serviceUrl = "rmi://localhost:1099/MedicalService";
            Naming.rebind(serviceUrl, medicalService);

            System.out.println(" Service RMI enregistré: " + serviceUrl);
            System.out.println(" Serveur prêt à recevoir des appels clients");
            System.out.println("=".repeat(50));
            System.out.println(" SERVEUR MÉDICAL RMI ACTIF");
            System.out.println("Port: 1099");
            System.out.println("Service: MedicalService");
            System.out.println("=".repeat(50));

            // 4. Garder le serveur actif
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("ERREUR SERVEUR RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}