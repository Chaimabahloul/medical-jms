package com.medical.jms.service;

import com.medical.jms.model.MedicalResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final String DB_URL = "jdbc:h2:~/medicaldb";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public DatabaseService() {
        initializeDatabase();
        System.out.println(" DatabaseService initialisé");
    }

    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS medical_results (" +
                "id VARCHAR(50) PRIMARY KEY," +
                "patient_name VARCHAR(100)," +
                "patient_id VARCHAR(50)," +
                "test_type VARCHAR(50)," +
                "result_value DOUBLE," +
                "unit VARCHAR(20)," +
                "is_critical BOOLEAN," +
                "doctor_notes TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +  // AJOUTÉ: DEFAULT
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(" Base de données H2 initialisée");
            System.out.println(" Fichier: ~/medicaldb.mv.db");
        } catch (SQLException e) {
            System.err.println(" Erreur initialisation BD: " + e.getMessage());
            logSQLException(e);
        }
    }

    public void saveResult(MedicalResult result) {
        if (result == null) {
            System.err.println(" Impossible de sauvegarder: MedicalResult est null");
            return;
        }

        String sql = "INSERT INTO medical_results (id, patient_name, patient_id, " +
                "test_type, result_value, unit, is_critical, doctor_notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, result.getId());
            pstmt.setString(2, result.getPatientName());
            pstmt.setString(3, result.getPatientId());
            pstmt.setString(4, result.getTestType());
            pstmt.setDouble(5, result.getValue());
            pstmt.setString(6, result.getUnit());
            pstmt.setBoolean(7, result.isCritical());
            pstmt.setString(8, result.getDoctorNotes());
            pstmt.setTimestamp(9, new Timestamp(result.getTimestamp().getTime()));

            int rows = pstmt.executeUpdate();
            System.out.println("💾 Résultat sauvegardé: " + result.getId() +
                    " (" + rows + " ligne(s) affectée(s))");

        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde pour patient: " + result.getPatientName());
            logSQLException(e);
        }
    }

    public List<MedicalResult> getRecentResults(int limit) {
        // VALIDATION AJOUTÉE
        if (limit <= 0) {
            System.out.println("  Limite invalide: " + limit + ", retourne liste vide");
            return new ArrayList<>();
        }

        List<MedicalResult> results = new ArrayList<>();
        String sql = "SELECT * FROM medical_results ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalResult result = createMedicalResultFromResultSet(rs);
                results.add(result);
            }

            System.out.println("📋 " + results.size() + " résultats récents récupérés (limite: " + limit + ")");

        } catch (SQLException e) {
            System.err.println(" Erreur lecture résultats récents");
            logSQLException(e);
        }

        return results;
    }

    public List<MedicalResult> getPatientResults(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            System.err.println("ID patient invalide");
            return new ArrayList<>();
        }

        List<MedicalResult> results = new ArrayList<>();
        String sql = "SELECT * FROM medical_results WHERE patient_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId.trim());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MedicalResult result = createMedicalResultFromResultSet(rs);
                results.add(result);
            }

            System.out.println("👤 " + results.size() + " résultats trouvés pour patient: " + patientId);

        } catch (SQLException e) {
            System.err.println(" Erreur recherche patient: " + patientId);
            logSQLException(e);
        }

        return results;
    }

    public List<MedicalResult> getCriticalResults() {
        List<MedicalResult> results = new ArrayList<>();
        String sql = "SELECT * FROM medical_results WHERE is_critical = TRUE ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {  // CORRIGÉ: try-with-resources

            while (rs.next()) {
                MedicalResult result = createMedicalResultFromResultSet(rs);
                results.add(result);
            }

            System.out.println("🚨 " + results.size() + " alertes critiques trouvées");

        } catch (SQLException e) {
            System.err.println(" Erreur alertes critiques");
            logSQLException(e);
        }

        return results;
    }

    public int getTotalCount() {
        return getCount("SELECT COUNT(*) as total FROM medical_results", "total");
    }

    public int getCriticalCount() {
        return getCount("SELECT COUNT(*) as critical FROM medical_results WHERE is_critical = TRUE", "critical");
    }

    // MÉTHODE COMMUNE POUR ÉVITER LA DUPLICATION
    private int getCount(String sql, String columnName) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int count = rs.getInt(columnName);
                System.out.println("📊 " + columnName + ": " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println(" Erreur count " + columnName);
            logSQLException(e);
        }

        return 0;
    }

    public int clearOldData(int days) {
        if (days <= 0) {
            System.err.println(" Nombre de jours invalide: " + days);
            return 0;
        }

        // CORRIGÉ: Syntaxe H2 correcte
        String sql = "DELETE FROM medical_results WHERE created_at < DATEADD('DAY', -?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            int deleted = pstmt.executeUpdate();
            System.out.println("🧹 " + deleted + " anciens enregistrements supprimés (> " + days + " jours)");
            return deleted;

        } catch (SQLException e) {
            System.err.println(" Erreur nettoyage données (> " + days + " jours)");
            logSQLException(e);
            return 0;
        }
    }

    // Méthode utilitaire pour créer un MedicalResult à partir d'un ResultSet
    private MedicalResult createMedicalResultFromResultSet(ResultSet rs) throws SQLException {
        // Utiliser le constructeur principal
        MedicalResult result = new MedicalResult(
                rs.getString("patient_id"),
                rs.getString("patient_name"),
                rs.getString("test_type"),
                rs.getDouble("result_value"),
                rs.getString("unit"),
                rs.getBoolean("is_critical")
        );

        // Définir l'ID
        result.setId(rs.getString("id"));

        // Définir les notes du médecin
        String doctorNotes = rs.getString("doctor_notes");
        if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
            result.setDoctorNotes(doctorNotes);
        }

        // Définir le timestamp
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            result.setTimestamp(new Date(ts.getTime()));
        }

        return result;
    }

    // MÉTHODE DE LOGGING AMÉLIORÉE (remplace printStackTrace)
    private void logSQLException(SQLException e) {
        System.err.println("   Code erreur SQL: " + e.getErrorCode());
        System.err.println("   État SQL: " + e.getSQLState());
        System.err.println("   Message: " + e.getMessage());

        // Log seulement les premières lignes pertinentes
        StackTraceElement[] stack = e.getStackTrace();
        int count = 0;
        for (StackTraceElement element : stack) {
            if (element.getClassName().contains("medical")) {
                System.err.println("   -> " + element.getClassName() +
                        "." + element.getMethodName() +
                        " (ligne " + element.getLineNumber() + ")");
                count++;
                if (count >= 3) break; // Limiter le nombre de lignes
            }
        }
    }

    // Méthode utilitaire pour tester la connexion
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println(" Connexion BD OK");
            return true;
        } catch (SQLException e) {
            System.err.println(" Connexion BD échouée");
            logSQLException(e);
            return false;
        }
    }

    // Méthode de test améliorée
    public static void main(String[] args) {
        System.out.println(" TEST DATABASE SERVICE");
        System.out.println("========================");

        DatabaseService dbService = new DatabaseService();

        // Test connexion
        System.out.println("\n1. Test connexion...");
        boolean connected = dbService.testConnection();
        if (!connected) {
            System.err.println(" Test arrêté - Connexion BD échouée");
            return;
        }

        // Créer un résultat de test
        MedicalResult testResult = new MedicalResult(
                "PAT001",
                "Jean Test",
                "Glycémie",
                1.45,
                "g/L",
                true
        );

        // 1. Sauvegarder
        System.out.println("\n2. Sauvegarde résultat...");
        dbService.saveResult(testResult);

        // 2. Compter les résultats
        System.out.println("\n3. Statistiques...");
        int total = dbService.getTotalCount();
        int critical = dbService.getCriticalCount();
        System.out.println("   Total: " + total);
        System.out.println("   Critiques: " + critical);

        // 3. Récupérer les résultats récents (avec limites)
        System.out.println("\n4. Résultats récents (max 5)...");
        List<MedicalResult> recent = dbService.getRecentResults(5);
        for (MedicalResult result : recent) {
            System.out.println("   • " + result.getPatientName() + " - " +
                    result.getTestType() + ": " + result.getValue() + " " + result.getUnit());
        }

        // 4. Récupérer les alertes critiques
        System.out.println("\n5. Alertes critiques...");
        List<MedicalResult> criticals = dbService.getCriticalResults();
        System.out.println("   Nombre d'alertes: " + criticals.size());

        // 5. Test paramètres invalides
        System.out.println("\n6. Test paramètres invalides...");
        System.out.println("   Limite -1: " + dbService.getRecentResults(-1).size() + " résultats");
        System.out.println("   Limite 0: " + dbService.getRecentResults(0).size() + " résultats");
        System.out.println("   Patient null: " + dbService.getPatientResults(null).size() + " résultats");
        System.out.println("   Patient vide: " + dbService.getPatientResults("").size() + " résultats");

        System.out.println("\n TEST DATABASE SERVICE RÉUSSI!");
    }
}