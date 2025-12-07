package com.medical.jms.ui;

import com.medical.jms.rmi.MedicalService;
import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;

public class MainInterface extends JFrame {
    private MedicalService rmiService;  // AJOUTÉ : Référence au service RMI
    private final DatabaseManager dbManager;
    private final ActiveMQManager mqManager;

    // Composants UI
    private JTextArea resultsArea;
    private JTextField patientField, testField, valueField;
    private final JTextArea statusArea;

    public MainInterface() {
        super("Système Médical - Dashboard (RMI + JMS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Initialisation
        dbManager = new DatabaseManager();
        mqManager = new ActiveMQManager();

        // CONNEXION RMI (AJOUTÉ)
        connectToRMI();

        // Panneau supérieur - Titre
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panneau gauche - Formulaire
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        // Panneau droit - Affichage résultats
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.CENTER);

        // Log de statut
        statusArea = createStatusArea();
        add(new JScrollPane(statusArea), BorderLayout.SOUTH);

        // Affichage initial
        setVisible(true);
        logStatus("[🚀] Interface démarrée - Prête à l'emploi");

        if (rmiService != null) {
            logStatus("[✅] Connecté au service RMI");
        } else {
            logStatus("[⚠️] Mode simulation (RMI non disponible)");
        }
    }

    private void connectToRMI() {
        try {
            String serviceUrl = "rmi://localhost:1099/MedicalService";
            rmiService = (MedicalService) Naming.lookup(serviceUrl);
            logStatus("[🔗] Connexion RMI établie");
        } catch (Exception e) {
            logStatus("[❌] Erreur RMI: " + e.getMessage());
            logStatus("[ℹ️] Utilisation du mode simulation");
            rmiService = null;
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 102, 204));
        JLabel title = new JLabel("🏥 SYSTÈME MÉDICAL - SUIVI DES RÉSULTATS (RMI + JMS)");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        panel.add(title);
        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nouveau Résultat"));
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Champs du formulaire
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        patientField = new JTextField(15);
        panel.add(patientField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Type de Test:"), gbc);
        gbc.gridx = 1;
        testField = new JTextField(15);
        panel.add(testField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Valeur:"), gbc);
        gbc.gridx = 1;
        valueField = new JTextField(15);
        panel.add(valueField, gbc);

        // Boutons
        JPanel buttonPanel = createButtonPanel();
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton addBtn = createButton("➕ Ajouter via RMI", new Color(76, 175, 80));
        JButton viewBtn = createButton("👁️ Afficher Résultats", new Color(33, 150, 243));
        JButton sendBtn = createButton("📤 Tester JMS", new Color(255, 152, 0));
        JButton checkBtn = createButton("🔍 Vérifier Connexions", new Color(156, 39, 176));
        JButton statsBtn = createButton("📊 Statistiques RMI", new Color(103, 58, 183));

        // Actions
        addBtn.addActionListener(e -> addMedicalResult());
        viewBtn.addActionListener(e -> viewResults());
        sendBtn.addActionListener(e -> testJMS());
        checkBtn.addActionListener(e -> checkConnections());
        statsBtn.addActionListener(e -> showStatistics());

        panel.add(addBtn);
        panel.add(viewBtn);
        panel.add(sendBtn);
        panel.add(checkBtn);
        panel.add(statsBtn);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📊 Résultats Médicaux"));

        resultsArea = new JTextArea();
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTextArea createStatusArea() {
        JTextArea area = new JTextArea(3, 50);
        area.setEditable(false);
        area.setBackground(Color.BLACK);
        area.setForeground(Color.GREEN);
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        return area;
    }

    // ===== ACTIONS =====

    private void addMedicalResult() {
        String patient = patientField.getText();
        String test = testField.getText();
        String value = valueField.getText();

        if (!patient.isEmpty() && !test.isEmpty() && !value.isEmpty()) {
            try {
                double val = Double.parseDouble(value);

                if (rmiService != null) {
                    // Utiliser RMI si disponible
                    String result = rmiService.addMedicalResult(patient, test, val);
                    logStatus("[RMI] " + result.split("\n")[0]);
                } else {
                    // Sinon simulation locale
                    boolean success = dbManager.addMedicalResult(patient, test, value);
                    logStatus(success ? "[+] Résultat ajouté (simulation)" : "[!] Erreur simulation");
                }

                // Effacer les champs
                patientField.setText("");
                testField.setText("");
                valueField.setText("");

            } catch (NumberFormatException e) {
                logStatus("[!] Valeur invalide: " + value);
            } catch (Exception e) {
                logStatus("[RMI ERREUR] " + e.getMessage());
            }
        } else {
            logStatus("[!] Veuillez remplir tous les champs");
        }
    }

    private void viewResults() {
        String results = dbManager.getMedicalResults();
        resultsArea.setText(results);
        logStatus("[✓] Résultats actualisés");
    }

    private void testJMS() {
        String message = "Test JMS: " + patientField.getText() + " - " +
                testField.getText() + " = " + valueField.getText();

        boolean sent = mqManager.sendMessage(message);
        logStatus(sent ? "[📤] Message JMS envoyé" : "[!] Erreur JMS");
    }

    private void checkConnections() {
        boolean dbOK = dbManager.checkConnection();
        boolean mqOK = mqManager.checkConnection();
        boolean rmiOK = (rmiService != null);

        logStatus("[🔍] Vérification connexions:");
        logStatus("    Base H2: " + (dbOK ? "✅ OK" : "❌ ERREUR"));
        logStatus("    ActiveMQ: " + (mqOK ? "✅ OK" : "❌ ERREUR"));
        logStatus("    Service RMI: " + (rmiOK ? "✅ CONNECTÉ" : "❌ DÉCONNECTÉ"));
    }

    private void showStatistics() {
        if (rmiService != null) {
            try {
                String status = rmiService.getSystemStatus();
                resultsArea.setText(status);
                logStatus("[📊] Statistiques RMI récupérées");
            } catch (Exception e) {
                logStatus("[❌] Erreur statistiques: " + e.getMessage());
            }
        } else {
            logStatus("[⚠️] RMI non disponible pour les statistiques");
        }
    }

    private void logStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainInterface();
        });
    }
}