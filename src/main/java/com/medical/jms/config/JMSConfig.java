package com.medical.jms.config;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

public class JMSConfig {

    // Configuration du broker ActiveMQ
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    /**
     * Crée une fabrique de connexions ActiveMQ configurée
     * @return ConnectionFactory configurée pour se connecter au broker
     */
    public static ConnectionFactory createConnectionFactory() {
        // Création de la fabrique avec les paramètres d'authentification
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);

        // Configuration de sécurité pour permettre la sérialisation d'objets
        factory.setTrustAllPackages(true); // Nécessaire pour les messages avec objets sérialisés

        return factory;
    }

    /**
     * Établit une connexion au broker ActiveMQ
     * @return Connection active au broker
     * @throws JMSException en cas d'échec de connexion
     */
    public static Connection createConnection() throws JMSException {
        return createConnectionFactory().createConnection();
    }

    /**
     * Crée une session JMS pour l'envoi et la réception de messages
     * @param connection Connexion active au broker
     * @return Session JMS configurée
     * @throws JMSException en cas d'échec de création de session
     */
    public static Session createSession(Connection connection) throws JMSException {
        // Paramètres : non transactionnel, accusé de réception automatique
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Fournit l'URL du bro
     * ker pour référence
     * @return URL de connexion au broker
     */
    public static String getBrokerUrl() {
        return BROKER_URL;
    }
}