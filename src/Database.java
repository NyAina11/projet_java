import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // Chemin vers votre fichier de base de données SQLite
    private static final String URL = "jdbc:sqlite:aeroport.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Connexion à la base de données SQLite établie.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}