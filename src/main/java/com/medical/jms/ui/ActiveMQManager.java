package com.medical.jms.ui;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveMQManager {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "MedicalAlertsQueue";  // CORRIGÉ: nom standard
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    public boolean sendMessage(String text) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            System.out.println(" Tentative d'envoi à ActiveMQ...");
            System.out.println("   Broker: " + BROKER_URL);
            System.out.println("   Queue: " + QUEUE_NAME);
            System.out.println("   Message: " + (text.length() > 50 ? text.substring(0, 50) + "..." : text));

            // Connexion ActiveMQ
            ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = factory.createConnection();
            connection.start();

            // Création session et queue
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Création du message
            TextMessage message = session.createTextMessage(text);

            // Ajouter des propriétés pour le suivi
            message.setStringProperty("source", "MedicalInterface");
            message.setLongProperty("timestamp", System.currentTimeMillis());
            message.setStringProperty("type", "MEDICAL_ALERT");

            // Envoi
            producer.send(message);

            System.out.println(" Message envoyé avec succès!");
            isConnected.set(true);

            return true;

        } catch (Exception e) {
            System.err.println(" Erreur ActiveMQ: " + e.getMessage());
            isConnected.set(false);

            // Popup d'erreur
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "ERREUR ACTIVE MQ\n\n" +
                                "Impossible d'envoyer le message.\n" +
                                "Cause: " + e.getMessage() + "\n\n" +
                                "Vérifiez que:\n" +
                                "1. ActiveMQ est démarré\n" +
                                "2. Le broker écoute sur " + BROKER_URL + "\n" +
                                "3. Le port 61616 est accessible",
                        "Erreur ActiveMQ",
                        JOptionPane.ERROR_MESSAGE);
            });

            return false;

        } finally {
            // TOUJOURS FERMER LES RESSOURCES
            closeResources(producer, session, connection);
        }
    }

    public boolean checkConnection() {
        Connection testConn = null;

        try {
            System.out.println(" Test connexion ActiveMQ...");

            ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            testConn = factory.createConnection();
            testConn.start();

            System.out.println(" Connexion ActiveMQ OK");
            isConnected.set(true);
            return true;

        } catch (Exception e) {
            System.err.println(" Connexion ActiveMQ échouée: " + e.getMessage());
            isConnected.set(false);
            return false;

        } finally {
            if (testConn != null) {
                try {
                    testConn.close();
                } catch (JMSException e) {
                    System.err.println("Erreur fermeture test connexion: " + e.getMessage());
                }
            }
        }
    }

    // Méthode pour recevoir des messages (optionnel)
    public void testReceiveMessage(int timeoutMillis) {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            consumer = session.createConsumer(queue);

            System.out.println(" En attente de message sur " + QUEUE_NAME + " (" + timeoutMillis + "ms)...");

            Message message = consumer.receive(timeoutMillis);

            if (message != null && message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                System.out.println(" Message reçu: " + text);
                System.out.println("   Type: " + message.getStringProperty("type"));
                System.out.println("   Timestamp: " + message.getLongProperty("timestamp"));
            } else {
                System.out.println(" Aucun message reçu dans " + timeoutMillis + "ms");
            }

        } catch (Exception e) {
            System.err.println(" Erreur réception: " + e.getMessage());

        } finally {
            closeResources(consumer, session, connection);
        }
    }

    // MÉTHODE UTILITAIRE POUR FERMER LES RESSOURCES
    private void closeResources(MessageProducer producer, Session session, Connection connection) {
        try {
            if (producer != null) producer.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture producer: " + e.getMessage());
        }

        try {
            if (session != null) session.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture session: " + e.getMessage());
        }

        try {
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture connection: " + e.getMessage());
        }
    }

    private void closeResources(MessageConsumer consumer, Session session, Connection connection) {
        try {
            if (consumer != null) consumer.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture consumer: " + e.getMessage());
        }

        try {
            if (session != null) session.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture session: " + e.getMessage());
        }

        try {
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Erreur fermeture connection: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }
}