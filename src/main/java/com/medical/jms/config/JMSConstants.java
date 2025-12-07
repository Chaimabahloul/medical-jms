package com.medical.jms.config;

public class JMSConstants {

    // Files d'attente JMS pour les différents types de messages
    public static final String MEDICAL_RESULTS_QUEUE = "MedicalResultsQueue";
    public static final String MEDICAL_ALERTS_QUEUE = "MedicalAlertsQueue";

    // Configuration du broker (répétition pour séparation des préoccupations)
    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";

    // Propriétés standards pour les messages JMS
    public static final String PROPERTY_PATIENT_ID = "patientId";
    public static final String PROPERTY_TEST_TYPE = "testType";
    public static final String PROPERTY_CRITICAL = "critical";
    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final String PROPERTY_SOURCE = "source";

    // Constructeur privé pour empêcher l'instanciation
    private JMSConstants() {
        // Cette classe est utilitaire, elle ne doit pas être instanciée
    }
}