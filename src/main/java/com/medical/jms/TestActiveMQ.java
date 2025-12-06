package com.medical.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TestActiveMQ {
    public static void main(String[] args) {
        System.out.println("=== TEST ACTIVE-MQ ===");
        System.out.println("Vérification des dépendances...");

        try {
            // Test 1: La classe existe-t-elle ?
            Class<?> clazz = Class.forName("org.apache.activemq.ActiveMQConnectionFactory");
            System.out.println("✅ Classe ActiveMQConnectionFactory trouvée: " + clazz);

            // Test 2: Création d'instance
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
            System.out.println("✅ Factory créée: " + factory.getClass().getName());

            // Test 3: Version ActiveMQ
            Package pkg = ActiveMQConnectionFactory.class.getPackage();
            System.out.println("✅ Version ActiveMQ: " + pkg.getImplementationVersion());

            System.out.println("\n=== PRÊT POUR LA CONNEXION ===");
            System.out.println("Toutes les dépendances sont chargées correctement.");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERREUR: ActiveMQ n'est pas dans le classpath");
            System.err.println("Solutions:");
            System.err.println("1. Vérifiez que activemq-all-5.16.6.jar est présent");
            System.err.println("2. Reconfigurez le classpath dans Project Structure");
            System.err.println("3. Vérifiez le fichier pom.xml");

        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}