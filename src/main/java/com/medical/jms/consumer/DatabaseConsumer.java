package com.medical.jms.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import com.medical.jms.config.JMSConstants;
import javax.jms.*;
import java.sql.*;
import java.util.logging.Logger;

public class DatabaseConsumer implements MessageListener {

    private static final Logger LOG = Logger.getLogger(DatabaseConsumer.class.getName());
    private javax.jms.Connection jmsConnection;  // Fully qualified name
    private Session session;
    private MessageConsumer consumer;
    private java.sql.Connection dbConnection;    // Fully qualified name

    public DatabaseConsumer(String queueName) throws Exception {
        // Connexion JMS
        ConnectionFactory factory = new ActiveMQConnectionFactory(JMSConstants.BROKER_URL);
        jmsConnection = factory.createConnection();
        jmsConnection.start();

        session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        consumer = session.createConsumer(queue);
        consumer.setMessageListener(this);

        // Connexion H2 Database (base de données en mémoire)
        Class.forName("org.h2.Driver");
        dbConnection = DriverManager.getConnection("jdbc:h2:mem:medicaldb", "sa", "");
        createDatabaseTable();

        LOG.info("DatabaseConsumer prêt pour: " + queueName);
    }

    private void createDatabaseTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS medical_results (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id VARCHAR(50), " +
                "patient_name VARCHAR(100), " +
                "test_type VARCHAR(50), " +
                "result_value VARCHAR(50), " +
                "unit VARCHAR(20), " +
                "reference_range VARCHAR(50), " +
                "is_critical BOOLEAN, " +
                "timestamp TIMESTAMP, " +
                "received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute(sql);
            LOG.info("Table medical_results créée");
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String content = textMessage.getText();

                // Extraire les informations du message
                String patientId = message.getStringProperty("patientId");
                String patientName = extractValue(content, "patientName");
                String testType = extractValue(content, "testType");
                String resultValue = extractValue(content, "resultValue");
                String unit = extractValue(content, "unit");
                String refRange = extractValue(content, "referenceRange");
                boolean critical = Boolean.parseBoolean(extractValue(content, "isCritical"));

                // Sauvegarder dans la base de données
                saveToDatabase(patientId, patientName, testType, resultValue, unit, refRange, critical);

                LOG.info("Résultat sauvegardé: " + patientName + " - " + testType);
            }
        } catch (Exception e) {
            LOG.severe("Erreur traitement message: " + e.getMessage());
        }
    }

    private void saveToDatabase(String patientId, String patientName, String testType,
                                String resultValue, String unit, String refRange,
                                boolean critical) throws SQLException {
        String sql = "INSERT INTO medical_results " +
                "(patient_id, patient_name, test_type, result_value, unit, " +
                "reference_range, is_critical, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            pstmt.setString(2, patientName);
            pstmt.setString(3, testType);
            pstmt.setString(4, resultValue);
            pstmt.setString(5, unit);
            pstmt.setString(6, refRange);
            pstmt.setBoolean(7, critical);

            pstmt.executeUpdate();
        }
    }

    private String extractValue(String json, String field) {
        try {
            String search = "\"" + field + "\":";
            int start = json.indexOf(search);
            if (start == -1) return "";

            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);

            String value = json.substring(start, end).trim();

            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            return value;
        } catch (Exception e) {
            return "";
        }
    }

    public void displayResults() throws SQLException {
        String sql = "SELECT * FROM medical_results ORDER BY received_at DESC";

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== RÉSULTATS EN BASE DE DONNÉES ===");
            System.out.printf("%-10s %-20s %-15s %-10s %-6s %s%n",
                    "ID Patient", "Nom", "Test", "Valeur", "Unit", "Critique");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10s %-20s %-15s %-10s %-6s %s%n",
                        rs.getString("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("test_type"),
                        rs.getString("result_value"),
                        rs.getString("unit"),
                        rs.getBoolean("is_critical") ? "OUI" : "non"
                );
            }
        }
    }

    public void close() throws JMSException, SQLException {
        if (consumer != null) consumer.close();
        if (session != null) session.close();
        if (jmsConnection != null) jmsConnection.close();
        if (dbConnection != null) dbConnection.close();
        LOG.info("DatabaseConsumer fermé");
    }

    public static void main(String[] args) {
        try {
            // Démarrer le consumer
            DatabaseConsumer consumer = new DatabaseConsumer(JMSConstants.MEDICAL_RESULTS_QUEUE);
            // Démarrer un thread pour envoyer des messages de test après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Attendre que le consumer soit prêt

                    System.out.println("\n=== ENVOI DE MESSAGES DE TEST ===");

                    // Connexion pour envoyer des messages
                    ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
                    javax.jms.Connection conn = factory.createConnection();
                    conn.start();

                    Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Queue queue = session.createQueue("MedicalResultsQueue");
                    MessageProducer producer = session.createProducer(queue);

                    // Message 1
                    String json1 = "{\"patientName\":\"Jean Dupont\",\"testType\":\"Glycémie\",\"resultValue\":\"95.2\",\"unit\":\"mg/dL\",\"referenceRange\":\"70-110\",\"isCritical\":false}";
                    TextMessage msg1 = session.createTextMessage(json1);
                    msg1.setStringProperty("patientId", "PAT1001");
                    producer.send(msg1);
                    System.out.println("1. Message envoyé: Jean Dupont - Glycémie");

                    Thread.sleep(1000);

                    // Message 2
                    String json2 = "{\"patientName\":\"Marie Curie\",\"testType\":\"Cholestérol\",\"resultValue\":\"245\",\"unit\":\"mg/dL\",\"referenceRange\":\"<200\",\"isCritical\":true}";
                    TextMessage msg2 = session.createTextMessage(json2);
                    msg2.setStringProperty("patientId", "PAT1002");
                    producer.send(msg2);
                    System.out.println("2. Message envoyé: Marie Curie - Cholestérol");

                    Thread.sleep(1000);

                    // Message 3
                    String json3 = "{\"patientName\":\"Paul Martin\",\"testType\":\"Tension\",\"resultValue\":\"145/95\",\"unit\":\"mmHg\",\"referenceRange\":\"120/80\",\"isCritical\":true}";
                    TextMessage msg3 = session.createTextMessage(json3);
                    msg3.setStringProperty("patientId", "PAT1003");
                    producer.send(msg3);
                    System.out.println("3. Message envoyé: Paul Martin - Tension");

                    producer.close();
                    session.close();
                    conn.close();

                    System.out.println("=== 3 messages de test envoyés ===\n");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Attente de messages (30 secondes)...");
            Thread.sleep(30000); // Attendre 30 secondes

            consumer.displayResults();
            consumer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}