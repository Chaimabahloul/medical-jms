package com.medical.jms.producer;

import com.medical.jms.model.MedicalResult;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MedicalResultProducer {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Random random = new Random();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MedicalResultProducer(String queueName) throws JMSException {
        // CORRIGER :
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connection = factory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName); // Utiliser le paramètre
        producer = session.createProducer(queue);

        System.out.println(" Producteur initialisé pour: " + queueName);
    }
    public void sendResult(MedicalResult result) throws JMSException {
        // Format JSON simple
        String json = String.format(
                "{\"patientId\":\"%s\",\"patientName\":\"%s\",\"testType\":\"%s\"," +
                        "\"resultValue\":%.2f,\"unit\":\"%s\",\"referenceRange\":\"%s\"," +
                        "\"isCritical\":%s,\"timestamp\":\"%s\"}",
                result.getPatientId(),
                result.getPatientName(),
                result.getTestType(),
                result.getValue(),  // CORRIGÉ: getValue() au lieu de getResultValue()
                result.getUnit(),
                result.getReferenceRange(),
                result.isCritical(),
                dateFormat.format(result.getTimestamp())
        );

        TextMessage message = session.createTextMessage(json);
        message.setStringProperty("type", "MEDICAL_RESULT");
        message.setStringProperty("patientId", result.getPatientId());
        message.setBooleanProperty("critical", result.isCritical());

        producer.send(message);
        System.out.println(" Résultat envoyé: " + result.getPatientName() +
                " - " + result.getTestType() + ": " + result.getValue() + " " + result.getUnit());
    }

    public void sendTextMessage(String text) throws JMSException {
        TextMessage message = session.createTextMessage(text);
        producer.send(message);
        System.out.println("Message texte envoyé: " + text.substring(0, Math.min(50, text.length())) + "...");
    }

    public void generateTestResults(int count) throws JMSException {
        String[] patients = {"Jean Dupont", "Marie Martin", "Pierre Dubois", "Sophie Leroy"};
        String[] patientIds = {"PAT001", "PAT002", "PAT003", "PAT004"};
        String[] tests = {"Glycémie", "Cholestérol", "Créatinine", "Tension", "Température"};
        String[] units = {"g/L", "g/L", "mg/dL", "mmHg", "°C"};

        System.out.println(" Génération de " + count + " résultats de test...");

        for (int i = 0; i < count; i++) {
            int index = i % patients.length;
            String patient = patients[index];
            String patientId = patientIds[index];
            String test = tests[random.nextInt(tests.length)];

            double value;
            boolean critical;

            // Valeurs selon le type de test
            switch(test) {
                case "Glycémie":
                    value = 0.8 + random.nextDouble() * 2.0; // 0.8-2.8 g/L
                    critical = value > 1.26 || value < 0.7;
                    break;
                case "Cholestérol":
                    value = 1.0 + random.nextDouble() * 2.0; // 1.0-3.0 g/L
                    critical = value > 2.0;
                    break;
                case "Tension":
                    value = 90 + random.nextDouble() * 70; // 90-160 mmHg
                    critical = value > 140 || value < 100;
                    break;
                case "Température":
                    value = 36.0 + random.nextDouble() * 4.0; // 36-40 °C
                    critical = value > 38.5 || value < 36.0;
                    break;
                default: // Créatinine
                    value = 6 + random.nextDouble() * 14; // 6-20 mg/dL
                    critical = value > 13;
            }

            MedicalResult result = new MedicalResult(
                    patientId,
                    patient,
                    test,
                    String.valueOf(value),
                    getUnitForTest(test),
                    getReferenceRange(test),
                    critical
            );

            sendResult(result);

            try {
                Thread.sleep(1500); // 1.5 seconde entre les envois
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(" Génération de tests terminée!");
    }

    private String getUnitForTest(String test) {
        switch(test) {
            case "Glycémie": return "g/L";
            case "Cholestérol": return "g/L";
            case "Tension": return "mmHg";
            case "Température": return "°C";
            case "Créatinine": return "mg/dL";
            default: return "unit";
        }
    }

    private String getReferenceRange(String test) {
        switch(test) {
            case "Glycémie": return "0.70-1.10 g/L";
            case "Cholestérol": return "< 2.0 g/L";
            case "Tension": return "120/80 mmHg";
            case "Température": return "36.5-37.5 °C";
            case "Créatinine": return "6-13 mg/dL";
            default: return "N/A";
        }
    }

    public void close() throws JMSException {
        if (producer != null) {
            producer.close();
            System.out.println(" Producteur fermé");
        }
        if (session != null) session.close();
        if (connection != null) connection.close();
    }

    public static void main(String[] args) {
        MedicalResultProducer producer = null;

        try {
            System.out.println("=== TEST PRODUCTEUR RÉSULTATS MÉDICAUX ===");
            System.out.println("Queue: MÉDICAL. ALERTES");
            System.out.println("===========================================\n");

            producer = new MedicalResultProducer("MedicalResultsQueue");
            // Test 1: Envoyer un message simple
            System.out.println("Test 1: Message simple...");
            producer.sendTextMessage("Test initial - Producteur médical opérationnel");

            // Test 2: Générer des résultats de test
            System.out.println("\n Test 2: Génération résultats tests...");
            producer.generateTestResults(3);

            System.out.println("\n TESTS RÉUSSIS!");

        } catch (Exception e) {
            System.err.println(" ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (producer != null) {
                    producer.close();
                }
            } catch (JMSException e) {
                System.err.println("Erreur fermeture: " + e.getMessage());
            }
        }
    }
}