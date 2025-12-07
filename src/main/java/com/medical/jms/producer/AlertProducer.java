package com.medical.jms.producer;
import com.medical.jms.model.MedicalResult;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.text.SimpleDateFormat;

public class AlertProducer {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private String queueName;

    public AlertProducer(String queueName) throws JMSException {
        this.queueName = queueName; // Utiliser le paramètre
        initialize();
    }

    private void initialize() throws JMSException {
        String brokerUrl = "tcp://localhost:61616";

        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        System.out.println("Producteur JMS initialisé pour: " + queueName);
    }

    public void sendAlert(MedicalResult alert) throws JMSException {
        String messageContent = formatAlertMessage(alert);

        TextMessage message = session.createTextMessage(messageContent);

        // Ajouter des propriétés
        message.setStringProperty("patientId", alert.getPatientId());
        message.setStringProperty("testType", alert.getTestType());
        message.setDoubleProperty("value", alert.getValue());
        message.setBooleanProperty("critical", alert.isCritical());

        producer.send(message);

        System.out.println(" Alerte JMS envoyée: " + alert.getPatientName());
    }

    private String formatAlertMessage(MedicalResult alert) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(alert.getTimestamp());

        return String.format(
                " ALERTE MÉDICALE CRITIQUE\n" +
                        "────────────────────────────\n" +
                        "Patient: %s (%s)\n" +
                        "Test: %s\n" +
                        "Valeur: %.2f %s\n" +
                        "Statut: %s\n" +
                        "Date: %s\n" +
                        "────────────────────────────",
                alert.getPatientName(),
                alert.getPatientId(),
                alert.getTestType(),
                alert.getValue(),
                alert.getUnit(),
                alert.isCritical() ? "CRITIQUE" : "Normal",
                timestamp
        );
    }

    public void sendTextMessage(String text) throws JMSException {
        TextMessage message = session.createTextMessage(text);
        producer.send(message);
        System.out.println(" Message texte envoyé");
    }

    public void close() throws JMSException {
        if (producer != null) producer.close();
        if (session != null) session.close();
        if (connection != null) connection.close();
        System.out.println("Producteur JMS fermé");
    }

    // Méthode de test
    public static void main(String[] args) {
        try {
            AlertProducer producer = new AlertProducer("MedicalAlertsQueue");
            // Créer une alerte de test
            MedicalResult testAlert = new MedicalResult(
                    "PAT001",                    // patientId EN PREMIER (CORRIGÉ)
                    "Ali Ben Mohamed",          // patientName
                    "Glycémie",                  // testType
                    1.45,                        // value
                    "g/L",                       // unit
                    true                         // isCritical
            );
            producer.sendAlert(testAlert);
            producer.close();

            System.out.println(" Test réussi!");

        } catch (Exception e) {
            System.err.println(" Erreur test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
