package com.medical.jms.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicalSystemDashboard extends JFrame {

    private JTabbedPane mainTabs;
    private JTextArea systemLogArea;
    private JLabel systemStatusLabel;

    // Pour JMS
    private JButton jmsConnectBtn, jmsDisconnectBtn, jmsSendBtn;
    private JTable jmsMessagesTable;
    private DefaultTableModel jmsTableModel;

    // Pour RMI
    private JButton rmiConnectBtn, rmiDisconnectBtn, rmiSearchBtn;
    private JTable rmiPatientsTable;
    private DefaultTableModel rmiTableModel;

    // Données de démo
    private String[][] demoPatients = {
            {"PAT1001", "Jean Dupont", "45", "M", "Type 2", "120/80", "95.2 mg/dL"},
            {"PAT1002", "Marie Curie", "67", "F", "Hypertension", "145/95", "245 mg/dL"},
            {"PAT1003", "Paul Martin", "52", "M", "Cardiaque", "130/85", "6.8 %"},
            {"PAT1004", "Sophie Bernard", "38", "F", "Normal", "118/76", "12.5 mg/L"},
            {"PAT1005", "Luc Tremblay", "29", "M", "Sportif", "110/70", "8500 /mm3"}
    };

    public MedicalSystemDashboard() {
        setTitle("🏥 Tableau de Bord Médical - JMS & RMI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // Panel principal
        setLayout(new BorderLayout());

        // ===== EN-TÊTE =====
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // ===== CENTRE - ONGLETS =====
        mainTabs = new JTabbedPane();
        mainTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        mainTabs.addTab("🏠 Tableau de Bord", createDashboardPanel());
        mainTabs.addTab("📨 Messages JMS", createJMSPanel());
        mainTabs.addTab("👥 Patients RMI", createRMIPanel());
        mainTabs.addTab("📊 Statistiques", createStatisticsPanel());
        mainTabs.addTab("⚙️ Configuration", createConfigPanel());

        add(mainTabs, BorderLayout.CENTER);

        // ===== PIED DE PAGE - LOGS =====
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        // Démarrer avec une notification
        showWelcomeMessage();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Logo et titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("🏥");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 40));

        JLabel titleLabel = new JLabel("SYSTÈME MÉDICAL INTÉGRÉ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("JMS + RMI - Gestion des Patients et Résultats");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(200, 230, 255));

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        titlePanel.add(textPanel);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);

        systemStatusLabel = new JLabel("🔴 Système inactif");
        systemStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        systemStatusLabel.setForeground(Color.YELLOW);
        systemStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JButton refreshBtn = new JButton("🔄 Actualiser");
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAll());

        statusPanel.add(systemStatusLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statusPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout(10, 10));
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== CARTES DE STATISTIQUES =====
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));

        cardsPanel.add(createInfoCard("📨 JMS", "Messages envoyés", "12", new Color(41, 128, 185)));
        cardsPanel.add(createInfoCard("👥 Patients", "Patients enregistrés", "5", new Color(46, 204, 113)));
        cardsPanel.add(createInfoCard("📊 Tests", "Tests effectués", "24", new Color(155, 89, 182)));
        cardsPanel.add(createInfoCard("⚠️ Alertes", "Résultats critiques", "3", new Color(230, 126, 34)));
        cardsPanel.add(createInfoCard("⏱️ Temps", "Réponse moyenne", "2.3s", new Color(241, 196, 15)));
        cardsPanel.add(createInfoCard("📈 Performance", "Disponibilité", "99.8%", new Color(52, 73, 94)));

        // ===== BOUTONS RAPIDES =====
        JPanel quickActionsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Actions Rapides"));

        JButton quickJMSBtn = createQuickButton("📨 Envoyer Message", new Color(41, 128, 185));
        JButton quickRMIBtn = createQuickButton("🔍 Rechercher Patient", new Color(46, 204, 113));
        JButton quickAlertBtn = createQuickButton("⚠️ Voir Alertes", new Color(230, 126, 34));
        JButton quickReportBtn = createQuickButton("📊 Générer Rapport", new Color(155, 89, 182));

        quickJMSBtn.addActionListener(e -> mainTabs.setSelectedIndex(1));
        quickRMIBtn.addActionListener(e -> mainTabs.setSelectedIndex(2));
        quickAlertBtn.addActionListener(e -> showAlerts());
        quickReportBtn.addActionListener(e -> generateReport());

        quickActionsPanel.add(quickJMSBtn);
        quickActionsPanel.add(quickRMIBtn);
        quickActionsPanel.add(quickAlertBtn);
        quickActionsPanel.add(quickReportBtn);

        // ===== ACTIVITÉ RÉCENTE =====
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBorder(BorderFactory.createTitledBorder("Activité Récente"));

        String[] activityColumns = {"Heure", "Action", "Détails", "Statut"};
        DefaultTableModel activityModel = new DefaultTableModel(activityColumns, 0);
        JTable activityTable = new JTable(activityModel);

        // Ajouter des activités de démo
        String[][] activities = {
                {"10:30", "Message JMS envoyé", "Patient: Jean Dupont - Glycémie", "✅"},
                {"10:25", "Recherche RMI", "Patient ID: PAT1002", "✅"},
                {"10:15", "Nouveau patient", "Marie Curie ajoutée", "✅"},
                {"10:00", "Connexion JMS", "ActiveMQ localhost:61616", "✅"},
                {"09:45", "Sauvegarde BD", "Base de données médicale", "✅"}
        };

        for (String[] activity : activities) {
            activityModel.addRow(activity);
        }

        JScrollPane activityScroll = new JScrollPane(activityTable);
        activityPanel.add(activityScroll, BorderLayout.CENTER);

        // Assemblage
        dashboard.add(cardsPanel, BorderLayout.NORTH);
        dashboard.add(quickActionsPanel, BorderLayout.CENTER);
        dashboard.add(activityPanel, BorderLayout.SOUTH);

        return dashboard;
    }

    private JPanel createJMSPanel() {
        JPanel jmsPanel = new JPanel(new BorderLayout(10, 10));
        jmsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== CONTRÔLES JMS =====
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Contrôles JMS"));

        jmsConnectBtn = createStyledButton("🔗 Connecter JMS", new Color(46, 204, 113), 16);
        jmsDisconnectBtn = createStyledButton("🔌 Déconnecter", new Color(231, 76, 60), 16);
        jmsSendBtn = createStyledButton("📤 Envoyer Message", new Color(52, 152, 219), 16);
        JButton jmsClearBtn = createStyledButton("🗑️ Effacer", new Color(149, 165, 166), 16);
        JButton jmsMonitorBtn = createStyledButton("📡 Monitorer", new Color(241, 196, 15), 16);

        jmsDisconnectBtn.setEnabled(false);
        jmsSendBtn.setEnabled(false);

        controlsPanel.add(jmsConnectBtn);
        controlsPanel.add(jmsDisconnectBtn);
        controlsPanel.add(jmsSendBtn);
        controlsPanel.add(jmsClearBtn);
        controlsPanel.add(jmsMonitorBtn);

        // ===== MESSAGES =====
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.setBorder(BorderFactory.createTitledBorder("Messages JMS"));

        String[] columns = {"ID", "Date/Heure", "Patient", "Type", "Contenu", "Statut"};
        jmsTableModel = new DefaultTableModel(columns, 0);
        jmsMessagesTable = new JTable(jmsTableModel);
        jmsMessagesTable.setRowHeight(30);
        jmsMessagesTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        JScrollPane tableScroll = new JScrollPane(jmsMessagesTable);

        // Ajouter des messages de démo
        addDemoJMSMessages();

        // ===== CRÉATION DE MESSAGE =====
        JPanel createPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Partie gauche : Formulaire
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Créer un Message"));

        formPanel.add(new JLabel("Patient ID:"));
        JTextField patientIdField = new JTextField();
        formPanel.add(patientIdField);

        formPanel.add(new JLabel("Nom:"));
        JTextField patientNameField = new JTextField();
        formPanel.add(patientNameField);

        formPanel.add(new JLabel("Test:"));
        JComboBox<String> testCombo = new JComboBox<>(new String[]{"Glycémie", "Cholestérol", "Tension", "Hémoglobine", "CRP"});
        formPanel.add(testCombo);

        formPanel.add(new JLabel("Valeur:"));
        JTextField valueField = new JTextField();
        formPanel.add(valueField);

        formPanel.add(new JLabel("Critique:"));
        JCheckBox criticalCheck = new JCheckBox();
        formPanel.add(criticalCheck);

        // Partie droite : Boutons et aperçu
        JPanel previewPanel = new JPanel(new BorderLayout());

        JTextArea previewArea = new JTextArea(8, 30);
        previewArea.setEditable(false);
        previewArea.setBorder(BorderFactory.createTitledBorder("Aperçu JSON"));

        JButton previewBtn = new JButton("👁️ Aperçu");
        JButton sendCustomBtn = new JButton("🚀 Envoyer");

        previewBtn.addActionListener(e -> {
            String json = String.format(
                    "{\"patientName\":\"%s\",\"testType\":\"%s\",\"resultValue\":\"%s\",\"unit\":\"mg/dL\",\"isCritical\":%s}",
                    patientNameField.getText(),
                    testCombo.getSelectedItem(),
                    valueField.getText(),
                    criticalCheck.isSelected()
            );
            previewArea.setText(json);
        });

        sendCustomBtn.addActionListener(e -> {
            String patientId = patientIdField.getText().isEmpty() ? "PAT" + System.currentTimeMillis() : patientIdField.getText();
            addJMSMessage(patientId, patientNameField.getText(), testCombo.getSelectedItem().toString());
            logSystem("Message JMS envoyé pour: " + patientNameField.getText());
            JOptionPane.showMessageDialog(this, "Message envoyé avec succès!");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(previewBtn);
        buttonPanel.add(sendCustomBtn);

        previewPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);
        previewPanel.add(buttonPanel, BorderLayout.SOUTH);

        createPanel.add(formPanel);
        createPanel.add(previewPanel);

        // Actions des boutons
        jmsConnectBtn.addActionListener(e -> connectJMS());
        jmsDisconnectBtn.addActionListener(e -> disconnectJMS());
        jmsSendBtn.addActionListener(e -> sendJMSMessage());
        jmsClearBtn.addActionListener(e -> jmsTableModel.setRowCount(0));
        jmsMonitorBtn.addActionListener(e -> monitorJMS());

        // Assemblage
        jmsPanel.add(controlsPanel, BorderLayout.NORTH);
        jmsPanel.add(tableScroll, BorderLayout.CENTER);
        jmsPanel.add(createPanel, BorderLayout.SOUTH);

        return jmsPanel;
    }

    private JPanel createRMIPanel() {
        JPanel rmiPanel = new JPanel(new BorderLayout(10, 10));
        rmiPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== CONTRÔLES RMI =====
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Contrôles RMI"));

        rmiConnectBtn = createStyledButton("🔗 Connecter RMI", new Color(46, 204, 113), 16);
        rmiDisconnectBtn = createStyledButton("🔌 Déconnecter", new Color(231, 76, 60), 16);
        rmiSearchBtn = createStyledButton("🔍 Rechercher", new Color(52, 152, 219), 16);
        JButton rmiAddBtn = createStyledButton("➕ Ajouter", new Color(155, 89, 182), 16);
        JButton rmiUpdateBtn = createStyledButton("✏️ Modifier", new Color(241, 196, 15), 16);

        rmiDisconnectBtn.setEnabled(false);

        controlsPanel.add(rmiConnectBtn);
        controlsPanel.add(rmiDisconnectBtn);
        controlsPanel.add(rmiSearchBtn);
        controlsPanel.add(rmiAddBtn);
        controlsPanel.add(rmiUpdateBtn);

        // ===== TABLE PATIENTS =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Patients"));

        String[] columns = {"ID Patient", "Nom", "Âge", "Sexe", "Condition", "Tension", "Dernier Test"};
        rmiTableModel = new DefaultTableModel(columns, 0);
        rmiPatientsTable = new JTable(rmiTableModel);
        rmiPatientsTable.setRowHeight(30);

        // Ajouter patients de démo
        for (String[] patient : demoPatients) {
            rmiTableModel.addRow(patient);
        }

        JScrollPane tableScroll = new JScrollPane(rmiPatientsTable);

        // ===== RECHERCHE =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Recherche de Patient"));

        JLabel searchLabel = new JLabel("Rechercher:");
        JTextField searchField = new JTextField(20);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Par ID", "Par Nom", "Par Condition"});
        JButton searchNowBtn = new JButton("🔍 Lancer recherche");

        searchNowBtn.addActionListener(e -> {
            String query = searchField.getText();
            String type = searchType.getSelectedItem().toString();
            searchPatient(query, type);
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchType);
        searchPanel.add(searchNowBtn);

        // ===== DÉTAILS PATIENT =====
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Détails du Patient"));

        JTextArea detailsArea = new JTextArea(10, 40);
        detailsArea.setEditable(false);
        detailsArea.setText("Sélectionnez un patient pour voir les détails...");

        rmiPatientsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = rmiPatientsTable.getSelectedRow();
            if (row >= 0) {
                String details = String.format(
                        "📋 DÉTAILS DU PATIENT\n" +
                                "────────────────────────\n" +
                                "ID: %s\n" +
                                "Nom: %s\n" +
                                "Âge: %s ans\n" +
                                "Sexe: %s\n" +
                                "Condition: %s\n" +
                                "Tension: %s\n" +
                                "Dernier test: %s\n" +
                                "\n📅 Historique disponible\n" +
                                "⚠️ Suivi médical actif",
                        rmiTableModel.getValueAt(row, 0),
                        rmiTableModel.getValueAt(row, 1),
                        rmiTableModel.getValueAt(row, 2),
                        rmiTableModel.getValueAt(row, 3),
                        rmiTableModel.getValueAt(row, 4),
                        rmiTableModel.getValueAt(row, 5),
                        rmiTableModel.getValueAt(row, 6)
                );
                detailsArea.setText(details);
            }
        });

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsPanel.add(detailsScroll, BorderLayout.CENTER);

        // Actions des boutons
        rmiConnectBtn.addActionListener(e -> connectRMI());
        rmiDisconnectBtn.addActionListener(e -> disconnectRMI());
        rmiSearchBtn.addActionListener(e -> searchPatient("", "Par Nom"));
        rmiAddBtn.addActionListener(e -> addPatient());
        rmiUpdateBtn.addActionListener(e -> updatePatient());

        // Assemblage
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlsPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(tableScroll);
        centerPanel.add(detailsPanel);

        rmiPanel.add(topPanel, BorderLayout.NORTH);
        rmiPanel.add(centerPanel, BorderLayout.CENTER);

        return rmiPanel;
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Graphiques de démo
        statsPanel.add(createChartPanel("📊 Messages JMS par jour", new Color(41, 128, 185)));
        statsPanel.add(createChartPanel("👥 Répartition Patients", new Color(46, 204, 113)));
        statsPanel.add(createChartPanel("⚠️ Résultats Critiques", new Color(230, 126, 34)));
        statsPanel.add(createChartPanel("⏱️ Temps de Réponse", new Color(155, 89, 182)));

        return statsPanel;
    }

    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        configPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        configPanel.add(createConfigSection("JMS Configuration", "localhost:61616", "ActiveMQ"));
        configPanel.add(createConfigSection("RMI Configuration", "localhost:1099", "Registry"));
        configPanel.add(createConfigSection("Base de Données", "jdbc:h2:mem:medicaldb", "H2"));
        configPanel.add(createConfigSection("Sécurité", "TLS 1.2", "Cryptage"));
        configPanel.add(createConfigSection("Logging", "/logs/medical.log", "Journalisation"));
        configPanel.add(createConfigSection("Backup", "Quotidien 02:00", "Sauvegarde"));

        return configPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(52, 73, 94));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Logs système
        systemLogArea = new JTextArea(4, 80);
        systemLogArea.setEditable(false);
        systemLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        systemLogArea.setBackground(new Color(30, 30, 30));
        systemLogArea.setForeground(Color.GREEN);

        JScrollPane logScroll = new JScrollPane(systemLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("📝 Logs Système"));

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);

        // Mettre à jour l'heure en temps réel
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            timeLabel.setText("🕐 " + time);
        });
        timer.start();

        JLabel userLabel = new JLabel("👤 Utilisateur: Admin | 🏥 Hôpital: Central");
        userLabel.setForeground(Color.WHITE);

        statusBar.add(timeLabel, BorderLayout.WEST);
        statusBar.add(userLabel, BorderLayout.EAST);

        footer.add(logScroll, BorderLayout.CENTER);
        footer.add(statusBar, BorderLayout.SOUTH);

        return footer;
    }

    // ===== MÉTHODES UTILITAIRES =====

    private JPanel createInfoCard(String title, String subtitle, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(new Color(230, 230, 230));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createQuickButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createStyledButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createChartPanel(String title, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        // Simuler un graphique avec un JLabel
        JLabel chartLabel = new JLabel("📈 Graphique: " + title, SwingConstants.CENTER);
        chartLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        chartLabel.setOpaque(true);
        chartLabel.setBackground(color.brighter());
        chartLabel.setForeground(Color.WHITE);
        chartLabel.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10));

        panel.add(chartLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConfigSection(String title, String value, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        JLabel typeLabel = new JLabel("Type: " + type, SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        typeLabel.setForeground(Color.GRAY);

        JButton editBtn = new JButton("✏️ Modifier");
        editBtn.setBackground(new Color(52, 152, 219));
        editBtn.setForeground(Color.WHITE);

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(typeLabel, BorderLayout.SOUTH);
        panel.add(editBtn, BorderLayout.NORTH);

        return panel;
    }

    // ===== MÉTHODES DE FONCTIONNALITÉS =====

    private void showWelcomeMessage() {
        SwingUtilities.invokeLater(() -> {
            systemStatusLabel.setText("🟢 Système actif");
            systemStatusLabel.setForeground(Color.GREEN);
            logSystem("✅ Tableau de Bord Médical initialisé");
            logSystem("📅 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            logSystem("👤 Connecté en tant que: Administrateur");
        });
    }

    private void connectJMS() {
        logSystem("🔗 Connexion JMS en cours...");
        jmsConnectBtn.setEnabled(false);
        jmsDisconnectBtn.setEnabled(true);
        jmsSendBtn.setEnabled(true);

        new Timer(1000, e -> {
            logSystem("✅ Connecté à ActiveMQ (localhost:61616)");
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void disconnectJMS() {
        logSystem("🔌 Déconnexion JMS...");
        jmsConnectBtn.setEnabled(true);
        jmsDisconnectBtn.setEnabled(false);
        jmsSendBtn.setEnabled(false);
        logSystem("✅ Déconnecté d'ActiveMQ");
    }

    private void sendJMSMessage() {
        String[] patients = {"Jean Dupont", "Marie Curie", "Paul Martin"};
        String[] tests = {"Glycémie", "Cholestérol", "Tension"};

        String patient = patients[(int)(Math.random() * patients.length)];
        String test = tests[(int)(Math.random() * tests.length)];

        addJMSMessage("PAT" + (1000 + jmsTableModel.getRowCount()), patient, test);
        logSystem("📨 Message JMS envoyé: " + patient + " - " + test);
    }

    private void addJMSMessage(String patientId, String patientName, String testType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        String[] statuses = {"✅ Envoyé", "📨 En cours", "⚠️ En attente"};
        String status = statuses[(int)(Math.random() * statuses.length)];

        Object[] row = {
                "MSG-" + (jmsTableModel.getRowCount() + 1),
                timestamp,
                patientName + " (" + patientId + ")",
                testType,
                "{\"patient\":\"" + patientName + "\",\"test\":\"" + testType + "\"}",
                status
        };

        jmsTableModel.addRow(row);
    }

    private void addDemoJMSMessages() {
        String[][] demoMessages = {
                {"MSG-001", "30/11 10:30", "Jean Dupont (PAT1001)", "Glycémie", "{\"value\":\"95.2\",\"unit\":\"mg/dL\"}", "✅ Traité"},
                {"MSG-002", "30/11 10:25", "Marie Curie (PAT1002)", "Cholestérol", "{\"value\":\"245\",\"unit\":\"mg/dL\"}", "⚠️ Critique"},
                {"MSG-003", "30/11 10:15", "Paul Martin (PAT1003)", "Tension", "{\"value\":\"145/95\",\"unit\":\"mmHg\"}", "✅ Traité"}
        };

        for (String[] msg : demoMessages) {
            jmsTableModel.addRow(msg);
        }
    }

    private void monitorJMS() {
        logSystem("📡 Monitoring JMS activé...");
        new Timer(2000, e -> {
            if (jmsTableModel.getRowCount() < 10) {
                addJMSMessage("PAT" + (2000 + jmsTableModel.getRowCount()),
                        "Patient Auto", "Test Monitor");
            }
        }).start();
    }

    private void connectRMI() {
        logSystem("🔗 Connexion RMI en cours...");
        rmiConnectBtn.setEnabled(false);
        rmiDisconnectBtn.setEnabled(true);
        rmiSearchBtn.setEnabled(true);

        new Timer(1000, e -> {
            logSystem("✅ Connecté au Registry RMI (localhost:1099)");
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void disconnectRMI() {
        logSystem("🔌 Déconnexion RMI...");
        rmiConnectBtn.setEnabled(true);
        rmiDisconnectBtn.setEnabled(false);
        rmiSearchBtn.setEnabled(false);
        logSystem("✅ Déconnecté du Registry RMI");
    }

    private void searchPatient(String query, String type) {
        if (query.isEmpty()) {
            rmiTableModel.setRowCount(0);
            for (String[] patient : demoPatients) {
                rmiTableModel.addRow(patient);
            }
            logSystem("🔍 Affichage de tous les patients");
        } else {
            logSystem("🔍 Recherche RMI: '" + query + "' par " + type);
            JOptionPane.showMessageDialog(this,
                    "Recherche effectuée:\n" +
                            "Type: " + type + "\n" +
                            "Query: " + query + "\n\n" +
                            "2 patients trouvés",
                    "Résultats Recherche",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addPatient() {
        String name = JOptionPane.showInputDialog(this, "Nom du patient:");
        if (name != null && !name.trim().isEmpty()) {
            String newId = "PAT" + (2000 + rmiTableModel.getRowCount());
            String[] newPatient = {newId, name, "30", "M", "Nouveau", "120/80", "En attente"};
            rmiTableModel.addRow(newPatient);
            logSystem("➕ Nouveau patient ajouté: " + name + " (" + newId + ")");
        }
    }

    private void updatePatient() {
        int row = rmiPatientsTable.getSelectedRow();
        if (row >= 0) {
            String name = (String) rmiTableModel.getValueAt(row, 1);
            String newValue = JOptionPane.showInputDialog(this, "Nouvelle valeur pour " + name + ":");
            if (newValue != null) {
                rmiTableModel.setValueAt(newValue, row, 6);
                logSystem("✏️ Patient mis à jour: " + name);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un patient à modifier");
        }
    }

    private void showAlerts() {
        JOptionPane.showMessageDialog(this,
                "⚠️ ALERTES CRITIQUES\n" +
                        "────────────────────\n" +
                        "1. Marie Curie - Cholestérol: 245 mg/dL (Élevé)\n" +
                        "2. Paul Martin - Tension: 145/95 mmHg (Hypertension)\n" +
                        "3. Patient X - Glycémie: 180 mg/dL (Très élevée)\n\n" +
                        "📞 Contact urgent requis!",
                "Alertes Médicales",
                JOptionPane.WARNING_MESSAGE);
    }

    private void generateReport() {
        logSystem("📊 Génération du rapport mensuel...");
        JOptionPane.showMessageDialog(this,
                "📈 RAPPORT MÉDICAL GÉNÉRÉ\n" +
                        "──────────────────────────\n" +
                        "Période: Novembre 2025\n" +
                        "Patients traités: " + rmiTableModel.getRowCount() + "\n" +
                        "Messages JMS: " + jmsTableModel.getRowCount() + "\n" +
                        "Tests critiques: 3\n" +
                        "Taux de réussite: 98.5%\n\n" +
                        "✅ Rapport sauvegardé: /rapports/nov2025.pdf",
                "Rapport Généré",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAll() {
        logSystem("🔄 Actualisation du système...");
        systemStatusLabel.setText("🔄 Actualisation...");

        new Timer(500, e -> {
            systemStatusLabel.setText("🟢 Système actif");
            logSystem("✅ Système actualisé avec succès");
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void logSystem(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            systemLogArea.append("[" + timestamp + "] " + message + "\n");
            systemLogArea.setCaretPosition(systemLogArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Style moderne
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Personnalisation
                UIManager.put("TabbedPane.selected", new Color(41, 128, 185));
                UIManager.put("TabbedPane.selectHighlight", new Color(41, 128, 185));

            } catch (Exception e) {
                e.printStackTrace();
            }

            new MedicalSystemDashboard();
        });
    }
}