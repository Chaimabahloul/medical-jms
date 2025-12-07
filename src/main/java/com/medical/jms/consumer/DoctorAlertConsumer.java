// Déclaration du package pour les classes consommatrices d'alertes
package com.medical.jms.consumer;

// Importation des classes nécessaires
import com.medical.jms.config.JMSConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

// Classe consommatrice d'alertes médicales pour les médecins
// Implémente l'interface MessageListener pour le traitement asynchrone des messages
public class DoctorAlertConsumer implements MessageListener {

    // Variables d'instance pour les connexions JMS
    private Connection connection;      // Connexion au broker ActiveMQ
    private Session session;           // Session JMS pour gérer les messages
    private MessageConsumer consumer;  // Consommateur qui reçoit les messages
    private String queueName;          // Nom de la file d'attente à écouter

    // Constructeur principal qui initialise le consommateur
    public DoctorAlertConsumer(String queueName) throws JMSException {
        this.queueName = queueName;    // Stocke le nom de la file d'attente
        initialize();                  // Initialise toutes les connexions
    }

    // Méthode d'initialisation des connexions JMS
    private void initialize() throws JMSException {
        // Affichage d'informations de démarrage
        System.out.println("Initialisation DoctorAlertConsumer...");
        System.out.println("   Broker: " + JMSConstants.BROKER_URL);
        System.out.println("   Queue: " + queueName);

        // Étape 1 : Créer la fabrique de connexions ActiveMQ
        ConnectionFactory factory = new ActiveMQConnectionFactory(JMSConstants.BROKER_URL);

        // Étape 2 : Créer et démarrer la connexion au broker
        connection = factory.createConnection();
        connection.start(); // Démarre la réception des messages

        // Étape 3 : Créer une session JMS (non transactionnelle avec accusé automatique)
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Étape 4 : Créer la file d'attente (queue) à écouter
        Queue queue = session.createQueue(queueName);

        // Étape 5 : Créer le consommateur et lier cet écouteur
        consumer = session.createConsumer(queue);
        consumer.setMessageListener(this); // Cette classe gère les messages reçus

        // Confirmation d'initialisation réussie
        System.out.println("Alerte Docteur prête pour: " + queueName);
        System.out.println("En attente d'alertes critiques...");
        System.out.println();
    }

    // Méthode de traitement des messages JMS (implémentation de l'interface)
    @Override
    public void onMessage(Message message) {
        try {
            // Vérifier si le message est un message texte
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String content = textMessage.getText(); // Contenu du message

                // Vérifier si le message est critique (via propriété JMS)
                boolean isCritical = false;
                try {
                    if (message.propertyExists("critical")) {
                        isCritical = message.getBooleanProperty("critical");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lecture propriété 'critical'");
                }

                // Traitement spécial pour les alertes critiques
                if (isCritical) {
                    // Affichage d'une alerte critique formatée
                    System.out.println();
                    for (int i = 0; i < 20; i++) {
                        System.out.print("*");
                    }
                    System.out.println();
                    System.out.println("ALERTE MÉDICALE CRITIQUE");
                    System.out.println("Date: " + new java.util.Date());
                    System.out.println("==================================================");

                    // Affichage du contenu du message
                    System.out.println("CONTENU:");
                    System.out.println(content);
                    System.out.println();

                    // Affichage des propriétés JMS du message
                    System.out.println("PROPRIÉTÉS:");
                    try {
                        if (message.propertyExists("patientId")) {
                            System.out.println("   Patient ID: " + message.getStringProperty("patientId"));
                        }
                        if (message.propertyExists("testType")) {
                            System.out.println("   Type de test: " + message.getStringProperty("testType"));
                        }
                        if (message.propertyExists("value")) {
                            System.out.println("   Valeur: " + message.getDoubleProperty("value"));
                        }
                        if (message.propertyExists("unit")) {
                            System.out.println("   Unité: " + message.getStringProperty("unit"));
                        }
                    } catch (JMSException e) {
                        System.err.println("   Erreur lecture propriétés");
                    }

                    // Instructions d'action pour le personnel médical
                    System.out.println("==================================================");
                    System.out.println("ACTION REQUISE:");
                    System.out.println("   1. Contacter le médecin urgentiste");
                    System.out.println("   2. Vérifier les antécédents du patient");
                    System.out.println("   3. Préparer l'intervention si nécessaire");
                    System.out.println("==================================================");
                    for (int i = 0; i < 20; i++) {
                        System.out.print("*");
                    }
                    System.out.println();
                    System.out.println();

                } else {
                    // Affichage simple pour les messages non critiques
                    System.out.println("Message reçu (non critique): " +
                            message.getStringProperty("patientId") + " - " +
                            message.getStringProperty("testType"));
                }
            } else if (message instanceof ObjectMessage) {
                // Traitement pour les messages objets (peuvent contenir des alertes système)
                System.out.println("Message objet reçu (alerte système)");
            }
        } catch (Exception e) {
            // Gestion des erreurs lors du traitement du message
            System.err.println("Erreur traitement message: " + e.getMessage());
        }
    }

    // Méthode pour fermer proprement toutes les ressources JMS
    public void close() {
        System.out.println("Fermeture DoctorAlertConsumer...");

        try {
            // Fermer le consommateur en premier
            if (consumer != null) {
                consumer.close();
                System.out.println("   Consumer fermé");
            }
        } catch (JMSException e) {
            System.err.println("   Erreur fermeture consumer: " + e.getMessage());
        }

        try {
            // Fermer la session JMS
            if (session != null) {
                session.close();
                System.out.println("   Session fermée");
            }
        } catch (JMSException e) {
            System.err.println("   Erreur fermeture session: " + e.getMessage());
        }

        try {
            // Fermer la connexion au broker
            if (connection != null) {
                connection.close();
                System.out.println("   Connexion fermée");
            }
        } catch (JMSException e) {
            System.err.println("   Erreur fermeture connexion: " + e.getMessage());
        }

        System.out.println("DoctorAlertConsumer fermé proprement");
    }

    // Méthode principale pour tester le consommateur
    public static void main(String[] args) {
        DoctorAlertConsumer consumer = null;

        try {
            // Affichage de démarrage
            System.out.println("==================================================");
            System.out.println("DÉMARRAGE DOCTOR ALERT CONSUMER");
            System.out.println("==================================================");

            // Création du consommateur avec la file d'attente des alertes médicales
            consumer = new DoctorAlertConsumer(JMSConstants.MEDICAL_ALERTS_QUEUE);

            // Garder le programme actif pour recevoir des messages
            System.out.println();
            System.out.println("En attente d'alertes...");
            System.out.println("Appuyez sur Entrée pour arrêter");
            System.out.println("--------------------------------------------------");

            // Attendre une entrée utilisateur pour arrêter le programme
            System.in.read();

        } catch (Exception e) {
            // Gestion des erreurs de démarrage
            System.err.println();
            System.err.println("ERREUR: " + e.getMessage());
            System.err.println("Vérifiez que:");
            System.err.println("   1. ActiveMQ est démarré sur " + JMSConstants.BROKER_URL);
            System.err.println("   2. Le port 61616 est accessible");
            System.err.println("   3. La queue existe: " + JMSConstants.MEDICAL_ALERTS_QUEUE);
        } finally {
            // Section finally garantissant la fermeture des ressources
            if (consumer != null) {
                consumer.close(); // Fermeture propre du consommateur
            }

            // Message de fin
            System.out.println();
            System.out.println("==================================================");
            System.out.println("DOCTOR ALERT CONSUMER ARRÊTÉ");
            System.out.println("==================================================");
        }
    }
}