package com.medical.jms;

import com.medical.jms.config.JMSConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class TestJMSConnection {

    public static void main(String[] args) {
        System.out.println("🔍 TEST CONNEXION JMS ET QUEUES");
        System.out.println("===============================\n");

        testBrokerConnection();
        testQueue(JMSConstants.MEDICAL_RESULTS_QUEUE);
        testQueue(JMSConstants.MEDICAL_ALERTS_QUEUE);
    }

    private static void testBrokerConnection() {
        System.out.println("1. Test connexion broker...");
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(JMSConstants.BROKER_URL);
            Connection conn = factory.createConnection();
            conn.start();
            conn.close();
            System.out.println("   ✅ Broker ActiveMQ connecté: " + JMSConstants.BROKER_URL);
        } catch (Exception e) {
            System.err.println("   ❌ Erreur connexion broker: " + e.getMessage());
        }
    }

    private static void testQueue(String queueName) {
        System.out.println("\n2. Test queue: " + queueName);
        Connection connection = null;
        Session session = null;

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(JMSConstants.BROKER_URL);
            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);

            // Essayer de créer un consumer temporaire
            MessageConsumer consumer = session.createConsumer(queue);

            // Envoyer un message test
            MessageProducer producer = session.createProducer(queue);
            TextMessage testMsg = session.createTextMessage("Test message for " + queueName);
            producer.send(testMsg);

            // Recevoir le message
            Message received = consumer.receive(2000);

            if (received != null && received instanceof TextMessage) {
                System.out.println("   ✅ Queue " + queueName + " opérationnelle");
                System.out.println("   📨 Message test envoyé et reçu");
            } else {
                System.out.println("   ⚠️  Queue " + queueName + " accessible mais pas de message reçu");
            }

            consumer.close();
            producer.close();

        } catch (Exception e) {
            System.err.println("   ❌ Erreur queue " + queueName + ": " + e.getMessage());
        } finally {
            closeResources(session, connection);
        }
    }

    private static void closeResources(Session session, Connection connection) {
        try {
            if (session != null) session.close();
        } catch (Exception e) {
            System.err.println("   Erreur fermeture session: " + e.getMessage());
        }

        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            System.err.println("   Erreur fermeture connexion: " + e.getMessage());
        }
    }
}