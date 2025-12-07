package com.medical.jms.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class TestMessageSender {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("MedicalResultsQueue");
            MessageProducer producer = session.createProducer(queue);

            // Message de test
            String jsonMessage = "{\"patientName\":\"Jean Dupont\",\"testType\":\"Glycémie\",\"resultValue\":\"95.2\",\"unit\":\"mg/dL\",\"referenceRange\":\"70-110\",\"isCritical\":false}";

            TextMessage message = session.createTextMessage(jsonMessage);
            message.setStringProperty("patientId", "PAT1001");

            producer.send(message);
            System.out.println("✓ Message envoyé: " + jsonMessage);

            // Envoyer un autre message
            jsonMessage = "{\"patientName\":\"Marie Curie\",\"testType\":\"Cholestérol\",\"resultValue\":\"245\",\"unit\":\"mg/dL\",\"referenceRange\":\"<200\",\"isCritical\":true}";
            message = session.createTextMessage(jsonMessage);
            message.setStringProperty("patientId", "PAT1002");
            producer.send(message);
            System.out.println("✓ Message envoyé: " + jsonMessage);

            producer.close();
            session.close();
            connection.close();

            System.out.println("\n=== 2 messages envoyés avec succès ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}