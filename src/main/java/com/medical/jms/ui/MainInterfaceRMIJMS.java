package com.medical.jms.ui;

import com.medical.jms.rmi.MedicalService;
import com.medical.jms.rmi.MedicalRecord;
import com.medical.jms.rmi.MedicalStatistics;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainInterfaceRMIJMS extends JFrame {

    // Services
    private MedicalService rmiService;
    private ActiveMQManager jmsManager;
    private DatabaseManager dbManager;

    // Composants RMI
    private JTextField rmiPatientField, rmiTestField, rmiValueField;
    private JTextArea rmiResultsArea;
    private JButton rmiCallBtn, rmiStatsBtn, rmiConnectBtn;

    // Composants JMS
    private JTextField jmsMessageField;
    private JTextArea jmsLogArea;
    private JButton jmsSendBtn, jmsCheckBtn, jmsClearBtn;

    // Panneau central
    private JTextArea statusArea;

    public MainInterfaceRMIJMS() {
        super("🏥 SYSTÈME DISTRIBUÉ - RMI + JMS");
        initUI();
        initServices();
        setVisible(true);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLayout(new BorderLayout());

        // ===== TITRE =====
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(41, 128, 185));
        JLabel title = new JLabel("🏥 SYSTÈME MÉDICAL DISTRIBUÉ - RMI + JMS");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        titlePanel.add(title);

        // ===== PANEL PRINCIPAL (RMI + JMS) =====
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // PANEL GAUCHE - RMI
        JPanel rmiPanel = createRMIPanel();

        // PANEL DROIT - JMS
        JPanel jmsPanel = createJMSPanel();

        mainPanel.add(rmiPanel);
        mainPanel.add(jmsPanel);

        // ===== PANEL STATUT =====
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("📋 JOURNAL SYSTÈME"));

        statusArea = new JTextArea(8, 100);
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.GREEN);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusPanel.add(statusScroll, BorderLayout.CENTER);

        // ===== ASSEMBLAGE =====
        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Centre la fenêtre
        setLocationRelativeTo(null);

        log("🚀 Interface RMI + JMS initialisée");
        log("📡 Prête à se connecter aux services");
    }

    private JPanel createRMIPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📡 SERVICE RMI"));
        panel.setBackground(new Color(240, 248, 255));

        // Formulaire RMI
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Champs RMI
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Patient RMI:"), gbc);
        gbc.gridx = 1;
        rmiPatientField = new JTextField(15);
        formPanel.add(rmiPatientField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Test:"), gbc);
        gbc.gridx = 1;
        rmiTestField = new JTextField(15);
        formPanel.add(rmiTestField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Valeur:"), gbc);
        gbc.gridx = 1;
        rmiValueField = new JTextField(15);
        formPanel.add(rmiValueField, gbc);

        // Boutons RMI
        JPanel rmiButtonPanel = new JPanel(new FlowLayout());
        rmiConnectBtn = createStyledButton("🔗 Connecter RMI", new Color(52, 152, 219));
        rmiCallBtn = createStyledButton("📞 Appeler Service", new Color(46, 204, 113));
        rmiStatsBtn = createStyledButton("📊 Statistiques", new Color(155, 89, 182));

        rmiButtonPanel.add(rmiConnectBtn);
        rmiButtonPanel.add(rmiCallBtn);
        rmiButtonPanel.add(rmiStatsBtn);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(rmiButtonPanel, gbc);

        // Zone résultats RMI
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("📄 Résultats RMI"));

        rmiResultsArea = new JTextArea(10, 30);
        rmiResultsArea.setEditable(false);
        rmiResultsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane rmiScroll = new JScrollPane(rmiResultsArea);

        resultsPanel.add(rmiScroll, BorderLayout.CENTER);

        // Assemblage panel RMI
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(resultsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createJMSPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📨 SERVICE JMS"));
        panel.setBackground(new Color(255, 245, 245));

        // Formulaire JMS
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(255, 245, 245));

        JLabel jmsLabel = new JLabel("Message JMS:");
        jmsMessageField = new JTextField();
        jmsMessageField.setText("Patient: [Nom] | Test: [Type] | Valeur: [XX] | CRITIQUE");

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.add(jmsLabel, BorderLayout.WEST);
        fieldPanel.add(jmsMessageField, BorderLayout.CENTER);

        // Boutons JMS
        JPanel jmsButtonPanel = new JPanel(new FlowLayout());
        jmsSendBtn = createStyledButton("📤 Envoyer à ActiveMQ", new Color(230, 126, 34));
        jmsCheckBtn = createStyledButton("🔍 Vérifier Connexion", new Color(52, 152, 219));
        jmsClearBtn = createStyledButton("🧹 Effacer Logs", new Color(231, 76, 60));

        jmsButtonPanel.add(jmsSendBtn);
        jmsButtonPanel.add(jmsCheckBtn);
        jmsButtonPanel.add(jmsClearBtn);

        formPanel.add(fieldPanel, BorderLayout.NORTH);
        formPanel.add(jmsButtonPanel, BorderLayout.SOUTH);

        // Zone logs JMS
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("📝 Logs JMS"));

        jmsLogArea = new JTextArea(10, 30);
        jmsLogArea.setEditable(false);
        jmsLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        jmsLogArea.setBackground(new Color(30, 30, 30));
        jmsLogArea.setForeground(Color.YELLOW);
        JScrollPane jmsScroll = new JScrollPane(jmsLogArea);

        logPanel.add(jmsScroll, BorderLayout.CENTER);

        // Assemblage panel JMS
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void initServices() {
        // Initialiser JMS
        jmsManager = new ActiveMQManager();
        dbManager = new DatabaseManager();

        // Actions des boutons RMI
        rmiConnectBtn.addActionListener(e -> connectToRMIService());
        rmiCallBtn.addActionListener(e -> callRMIService());
        rmiStatsBtn.addActionListener(e -> getRMIStatistics());

        // Actions des boutons JMS
        jmsSendBtn.addActionListener(e -> sendJMSMessage());
        jmsCheckBtn.addActionListener(e -> checkJMSConnection());
        jmsClearBtn.addActionListener(e -> jmsLogArea.setText(""));
    }

    private void connectToRMIService() {
        try {
            log("🔗 Tentative de connexion au service RMI...");

            // URL du service RMI
            String rmiUrl = "rmi://localhost:1099/MedicalService";

            // Essayer de se connecter au vrai serveur
            rmiService = (MedicalService) Naming.lookup(rmiUrl);

            if (rmiService.isAlive()) {
                log("✅ Connexion RMI réussie au serveur réel!");
                rmiResultsArea.setText("✅ Service RMI réel connecté!\n\n");
                rmiConnectBtn.setBackground(new Color(39, 174, 96));
                rmiConnectBtn.setText("✅ Connecté RMI");
                return; // IMPORTANT : Sortir si connexion réussie
            }

        } catch (Exception e) {
            log("⚠ Impossible de se connecter au serveur RMI réel: " + e.getMessage());
            log("🔄 Utilisation du serveur RMI simulé...");

            // Utiliser le serveur mock
            rmiService = new MockRMIServer();
            rmiResultsArea.setText("✅ Serveur RMI simulé activé!\n\n" +
                    "Mode simulation: Toutes les opérations seront simulées\n\n" +
                    "Pour utiliser le vrai serveur RMI:\n" +
                    "1. Lancez RMIServer.java\n" +
                    "2. Redémarrez la connexion");
            rmiConnectBtn.setBackground(new Color(255, 165, 0)); // Orange
            rmiConnectBtn.setText("🔄 Mode Simulation");

            log("✅ Serveur RMI simulé activé");
        }
    }

    private void callRMIService() {
        if (rmiService == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord connecter le service RMI!",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String patient = rmiPatientField.getText();
            String test = rmiTestField.getText();
            String valueStr = rmiValueField.getText();

            if (patient.isEmpty() || test.isEmpty() || valueStr.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez remplir tous les champs RMI!",
                        "Champs manquants",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double value = Double.parseDouble(valueStr);

            log("📞 Appel du service RMI: addMedicalResult...");
            String result = rmiService.addMedicalResult(patient, test, value);

            // Afficher le résultat
            rmiResultsArea.setText("📊 RÉSULTAT RMI:\n\n");
            rmiResultsArea.append("Service appelé: addMedicalResult\n");
            rmiResultsArea.append("Patient: " + patient + "\n");
            rmiResultsArea.append("Test: " + test + "\n");
            rmiResultsArea.append("Valeur: " + value + "\n");
            rmiResultsArea.append("\nRéponse du serveur:\n");
            rmiResultsArea.append(result + "\n");

            log("✅ Service RMI appelé avec succès");

            // Récupérer les résultats du patient
            List<MedicalRecord> records = rmiService.getPatientResults(patient);
            if (!records.isEmpty()) {
                rmiResultsArea.append("\n📋 HISTORIQUE DU PATIENT:\n");
                for (MedicalRecord record : records) {
                    rmiResultsArea.append("  • " + record.toString() + "\n");
                }
            }

        } catch (NumberFormatException e) {
            log("❌ Erreur: La valeur doit être un nombre!");
            JOptionPane.showMessageDialog(this,
                    "La valeur doit être un nombre!",
                    "Erreur de format",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            log("❌ Erreur appel RMI: " + e.getMessage());
            rmiResultsArea.setText("❌ ERREUR:\n" + e.getMessage());
        }
    }

    private void getRMIStatistics() {
        if (rmiService == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez d'abord connecter le service RMI!",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            log("📊 Récupération des statistiques RMI...");
            MedicalStatistics stats = rmiService.getStatistics();

            rmiResultsArea.setText("📈 STATISTIQUES MÉDICALES\n\n");
            rmiResultsArea.append(stats.toString() + "\n\n");

            // Récupérer les alertes critiques
            List<MedicalRecord> critical = rmiService.getCriticalResults();
            if (!critical.isEmpty()) {
                rmiResultsArea.append("🚨 ALERTES CRITIQUES (" + critical.size() + "):\n");
                for (MedicalRecord record : critical) {
                    rmiResultsArea.append("  • " + record.toString() + "\n");
                }
            } else {
                rmiResultsArea.append("✅ Aucune alerte critique\n");
            }

            log("✅ Statistiques RMI récupérées");

        } catch (Exception e) {
            log("❌ Erreur statistiques RMI: " + e.getMessage());
            rmiResultsArea.setText("❌ ERREUR STATISTIQUES:\n" + e.getMessage());
        }
    }

    private void sendJMSMessage() {
        String message = jmsMessageField.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez entrer un message!",
                    "Message vide",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        log("📤 Envoi message JMS...");
        boolean sent = jmsManager.sendMessage(message);

        if (sent) {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            jmsLogArea.append("[" + timestamp + "] ✅ ENVOYÉ: " + message + "\n");
            log("✅ Message JMS envoyé à ActiveMQ");

            // Popup de confirmation
            JOptionPane.showMessageDialog(this,
                    "Message envoyé avec succès à ActiveMQ!\n\n" +
                            "Queue: MÉDICAL. ALERTES\n" +
                            "Contenu: " + message,
                    "Succès JMS",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            jmsLogArea.append("[❌] ÉCHEC: " + message + "\n");
            log("❌ Échec envoi JMS");
        }
    }

    private void checkJMSConnection() {
        log("🔍 Vérification connexion JMS...");
        boolean jmsOK = jmsManager.checkConnection();
        boolean dbOK = dbManager.checkConnection();

        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        jmsLogArea.append("[" + timestamp + "] 🔍 TEST CONNEXION:\n");
        jmsLogArea.append("  ActiveMQ: " + (jmsOK ? "✅ OK" : "❌ ERREUR") + "\n");
        jmsLogArea.append("  Base H2: " + (dbOK ? "✅ OK" : "❌ ERREUR") + "\n");

        if (jmsOK && dbOK) {
            log("✅ Toutes les connexions sont OK");
        } else {
            log("⚠ Certaines connexions ont échoué");
        }
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        statusArea.append("[" + timestamp + "] " + message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
        System.out.println(message); // Aussi dans la console
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainInterfaceRMIJMS();
        });
    }
}