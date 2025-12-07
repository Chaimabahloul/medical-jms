package com.medical.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TestActiveMQ {

    public static void main(String[] args) {
        System.out.println("=== TEST ACTIVE MQ ===");

        try {
            // 1. Test de connexion
            System.out.println("1. Test de connexion...");
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.start();
            System.out.println("✓ Connexion ActiveMQ OK");

            // 2. Test session
            System.out.println("2. Test session...");
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("✓ Session créée");

            // 3. Test queue
            System.out.println("3. Test création queue...");
            Queue queue = session.createQueue("TestQueue");
            System.out.println("✓ Queue créée: TestQueue");

            // 4. Test producteur
            System.out.println("4. Test producteur...");
            MessageProducer producer = session.createProducer(queue);
            TextMessage message = session.createTextMessage("Message de test ActiveMQ");
            producer.send(message);
            System.out.println("✓ Message envoyé");

            // 5. Test consommateur
            System.out.println("5. Test consommateur...");
            MessageConsumer consumer = session.createConsumer(queue);
            Message received = consumer.receive(5000);

            if (received instanceof TextMessage) {
                TextMessage text = (TextMessage) received;
                System.out.println("✓ Message reçu: " + text.getText());
            } else {
                System.out.println("✗ Aucun message reçu");
            }

            // 6. Nettoyage
            System.out.println("6. Nettoyage...");
            producer.close();
            consumer.close();
            session.close();
            connection.close();
            System.out.println("✓ Ressources fermées");

            System.out.println("\n=== TEST RÉUSSI ===");

        } catch (Exception e) {
            System.err.println("\n=== TEST ÉCHOUÉ ===");
            System.err.println("Erreur: " + e.getMessage());
            System.err.println("\nVérifiez que:");
            System.err.println("1. ActiveMQ est démarré");
            System.err.println("2. L'URL est correcte: tcp://localhost:61616");
            System.err.println("3. Les ports ne sont pas bloqués");
            e.printStackTrace();
        }
    }
}