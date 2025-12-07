package com.medical.jms.ui;

import com.medical.jms.config.JMSConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JMSConsumerUI extends JFrame {

    // Variables d'instance - UNE SEULE DÉCLARATION
    private String brokerUrl = JMSConstants.BROKER_URL;
    private String queueName = JMSConstants.MEDICAL_RESULTS_QUEUE; // Par défaut

    private JTextArea messageArea;
    private JButton startButton, stopButton;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private boolean isConsuming = false;

    // Composants UI
    private JTextField brokerField;
    private JTextField queueField;
    private JComboBox<String> queueSelector;

    public JMSConsumerUI() {
        super("🎧 CONSOMMATEUR JMS - MESSAGES MÉDICAUX");
        initUI();
        setupListeners();
        setVisible(true);
    }

    private void initUI() {
        setSize(700, 500); // Légèrement plus grand
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        messageArea.setBackground(new Color(30, 30, 30));
        messageArea.setForeground(Color.GREEN);

        startButton = createStyledButton("▶ Démarrer la réception", new Color(46, 204, 113));
        stopButton = createStyledButton("⏹ Arrêter", new Color(231, 76, 60));
        stopButton.setEnabled(false);

        JPanel controlPanel = new JPanel();
        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        // Panel configuration amélioré
        JPanel configPanel = createConfigPanel();

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(configPanel, BorderLayout.CENTER);

        // Zone d'information
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("💡 Conseil: Sélectionnez une queue et cliquez sur Démarrer"));
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Configuration JMS"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Broker URL
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Broker URL:"), gbc);
        gbc.gridx = 1;
        brokerField = new JTextField(brokerUrl, 30);
        panel.add(brokerField, gbc);

        // Queue sélection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Queue:"), gbc);
        gbc.gridx = 1;

        // Créer un comboBox avec les queues disponibles
        String[] availableQueues = {
                JMSConstants.MEDICAL_RESULTS_QUEUE,
                JMSConstants.MEDICAL_ALERTS_QUEUE,
                "TestQueue",
                "MÉDICAL. ALERTES" // Pour compatibilité
        };

        queueSelector = new JComboBox<>(availableQueues);
        queueSelector.setSelectedItem(queueName);
        queueSelector.addActionListener(e -> {
            queueName = (String) queueSelector.getSelectedItem();
            messageArea.append("[CONFIG] Queue sélectionnée: " + queueName + "\n");
        });
        panel.add(queueSelector, gbc);

        // Bouton manuel pour custom queue
        gbc.gridx = 2;
        queueField = new JTextField(queueName, 20);
        panel.add(queueField, gbc);

        gbc.gridx = 3;
        JButton customQueueBtn = new JButton("Custom");
        customQueueBtn.addActionListener(e -> {
            queueName = queueField.getText().trim();
            if (!queueName.isEmpty()) {
                messageArea.append("[CONFIG] Queue personnalisée: " + queueName + "\n");
            }
        });
        panel.add(customQueueBtn, gbc);

        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton testBtn = new JButton("🔍 Tester");
        testBtn.addActionListener(e -> testConnection());

        JButton clearBtn = new JButton("🗑️ Effacer");
        clearBtn.addActionListener(e -> messageArea.setText(""));

        JButton refreshBtn = new JButton("🔄 Actualiser");
        refreshBtn.addActionListener(e -> refreshConfig());

        buttonPanel.add(testBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(refreshBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

    private void setupListeners() {
        startButton.addActionListener(e -> {
            if (!isConsuming) {
                startConsumer();
            }
        });

        stopButton.addActionListener(e -> stopConsumer());
    }

    private void startConsumer() {
        // Mettre à jour depuis les champs
        brokerUrl = brokerField.getText().trim();
        if (queueSelector.getSelectedItem() != null) {
            queueName = (String) queueSelector.getSelectedItem();
        }

        if (brokerUrl.isEmpty() || queueName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez spécifier un broker URL et une queue",
                    "Configuration manquante",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination queue = session.createQueue(queueName);
            consumer = session.createConsumer(queue);

            messageArea.append("\n══════════════════════════════════════\n");
            messageArea.append("[✓] Connexion établie: " + brokerUrl + "\n");
            messageArea.append("[✓] Queue: " + queueName + "\n");
            messageArea.append("[⏳] En attente de messages...\n");
            messageArea.append("══════════════════════════════════════\n\n");

            consumer.setMessageListener(this::processMessage);

            isConsuming = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

        } catch (Exception ex) {
            messageArea.append("[❌] Erreur connexion: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la connexion :\n" + ex.getMessage(),
                    "Erreur Connexion",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String txt = ((TextMessage) message).getText();
                SwingUtilities.invokeLater(() -> {
                    messageArea.append("📩 [" + getCurrentTime() + "] Message reçu:\n");
                    messageArea.append("   Contenu: " + txt + "\n");

                    // Afficher les propriétés
                    try {
                        messageArea.append("   Propriétés: ");
                        if (message.propertyExists("patientId")) {
                            messageArea.append("patientId=" + message.getStringProperty("patientId") + " ");
                        }
                        if (message.propertyExists("testType")) {
                            messageArea.append("testType=" + message.getStringProperty("testType") + " ");
                        }
                        if (message.propertyExists("critical")) {
                            messageArea.append("critical=" + message.getBooleanProperty("critical") + " ");
                        }
                        if (message.propertyExists("timestamp")) {
                            long ts = message.getLongProperty("timestamp");
                            messageArea.append("timestamp=" + new Date(ts) + " ");
                        }
                        messageArea.append("\n");
                    } catch (JMSException ex) {
                        messageArea.append("   [Erreur lecture propriétés]\n");
                    }
                    messageArea.append("──\n");
                });
            } else if (message instanceof ObjectMessage) {
                SwingUtilities.invokeLater(() -> {
                    messageArea.append("📦 [" + getCurrentTime() + "] Message objet reçu\n");
                });
            }
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                messageArea.append("[❌] Erreur traitement message: " + ex.getMessage() + "\n");
            });
        }
    }

    private void stopConsumer() {
        try {
            if (consumer != null) {
                consumer.close();
                messageArea.append("[⚠️] Réception arrêtée.\n");
            }
            if (session != null) session.close();
            if (connection != null) connection.close();

        } catch (Exception ex) {
            messageArea.append("[❌] Erreur arrêt: " + ex.getMessage() + "\n");
        } finally {
            isConsuming = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void testConnection() {
        Connection testConn = null;
        try {
            brokerUrl = brokerField.getText().trim();
            queueName = queueField.getText().trim();

            if (queueName.isEmpty() && queueSelector.getSelectedItem() != null) {
                queueName = (String) queueSelector.getSelectedItem();
            }

            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            testConn = factory.createConnection();
            testConn.start();

            Session testSession = testConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = testSession.createQueue(queueName);

            // Essayer de créer un producer/consumer temporaire
            MessageProducer tempProducer = testSession.createProducer(queue);
            MessageConsumer tempConsumer = testSession.createConsumer(queue);

            // Envoyer un message test
            TextMessage testMsg = testSession.createTextMessage("Test de connexion - " + new Date());
            tempProducer.send(testMsg);

            // Recevoir
            Message received = tempConsumer.receive(2000);

            tempProducer.close();
            tempConsumer.close();
            testSession.close();

            if (received != null) {
                messageArea.append("[✅] Test réussi: " + brokerUrl + " | " + queueName + "\n");
                JOptionPane.showMessageDialog(this,
                        "Connexion test réussie!\n" +
                                "Broker: " + brokerUrl + "\n" +
                                "Queue: " + queueName,
                        "Test Réussi",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                messageArea.append("[⚠️] Queue accessible mais timeout réception\n");
            }

        } catch (Exception e) {
            messageArea.append("[❌] Test échoué: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this,
                    "Test de connexion échoué:\n" + e.getMessage(),
                    "Test Échoué",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (testConn != null) {
                try { testConn.close(); } catch (JMSException e) {}
            }
        }
    }

    private void refreshConfig() {
        brokerField.setText(JMSConstants.BROKER_URL);
        queueSelector.setSelectedItem(JMSConstants.MEDICAL_RESULTS_QUEUE);
        queueField.setText(JMSConstants.MEDICAL_RESULTS_QUEUE);
        messageArea.append("[🔄] Configuration rafraîchie\n");
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    @Override
    public void dispose() {
        stopConsumer();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Style personnalisé
                UIManager.put("TextArea.background", new Color(30, 30, 30));
                UIManager.put("TextArea.foreground", Color.GREEN);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new JMSConsumerUI();
        });
    }
}