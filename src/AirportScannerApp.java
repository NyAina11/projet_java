import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AirportScannerApp {

    public static void main(String[] args) {
        // Le fichier à scanner est passé en argument, ou défini ici
        String fichierPassagers = "passagers_a_scanner.txt";
        scannerFichierPassagers(fichierPassagers);
    }

    public static void scannerFichierPassagers(String cheminFichier) {
        // La première ligne du fichier est l'en-tête
        String header = "";

        try (Connection conn = Database.connect();
             BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {

            if (conn == null) {
                System.err.println("Échec de la connexion à la base de données.");
                return;
            }

            // Lire et ignorer l'en-tête
            header = br.readLine();
            if (header == null) {
                System.err.println("Le fichier de passagers est vide.");
                return;
            }

            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] champs = ligne.split(",");
                if (champs.length < 4) {
                    System.err.println("Ligne mal formée, ignorée : " + ligne);
                    continue; // Passe à la ligne suivante
                }

                String nomPassager = champs[0];
                String aeroportDepart = champs[1];
                String aeroportArrivee = champs[2];
                String dateVol = champs[3];

                System.out.println("==============================================");
                System.out.printf("SCAN pour : %s | Vol : %s -> %s | Date : %s\n",
                        nomPassager, aeroportDepart, aeroportArrivee, dateVol);

                // Chercher les NOTAM pour le départ et l'arrivée
                chercherNotamsPourAeroport(conn, aeroportDepart, dateVol, "Départ");
                chercherNotamsPourAeroport(conn, aeroportArrivee, dateVol, "Arrivée");
            }

        } catch (IOException | SQLException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
        }
    }

    private static void chercherNotamsPourAeroport(Connection conn, String codeIcao, String dateVol, String type) {
        System.out.printf("--- NOTAMs pour l'aéroport de %s (%s) ---\n", type, codeIcao);
        String sql = "SELECT code_notam, message FROM notams WHERE code_icao = ? AND date_debut <= ? AND date_fin >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codeIcao);
            pstmt.setString(2, dateVol);
            pstmt.setString(3, dateVol);

            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                System.out.printf("  -> [%s]: %s\n", rs.getString("code_notam"), rs.getString("message"));
                found = true;
            }

            if (!found) {
                System.out.printf("  -> Aucun NOTAM actif trouvé pour %s à cette date.\n", codeIcao);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la recherche de NOTAMs pour " + codeIcao + ": " + e.getMessage());
        }
    }
}