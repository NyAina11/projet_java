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
        String fichierVols = "vols_a_scanner.txt";
        scannerFichierVols(fichierVols);
    }

    public static void scannerFichierVols(String cheminFichier) {
        // Try-with-resources pour s'assurer que les ressources sont bien fermées
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier));
             Connection conn = Database.connect()) {

            if (conn == null) {
                System.out.println("Impossible de se connecter à la base de données.");
                return;
            }

            String ligne;
            br.readLine(); // Ignorer la première ligne (l'en-tête)

            while ((ligne = br.readLine()) != null) {
                String[] champs = ligne.split(",");
                String numeroVol = champs[0];
                String aeroportDepart = champs[1];
                String aeroportArrivee = champs[2];
                String dateVol = champs[3];

                System.out.println("==============================================");
                System.out.printf("SCAN pour le vol %s (%s -> %s) le %s\n", numeroVol, aeroportDepart, aeroportArrivee, dateVol);

                // On cherche les NOTAM pertinents pour le départ et l'arrivée
                chercherNotamsPourVol(conn, aeroportDepart, dateVol);
                chercherNotamsPourVol(conn, aeroportArrivee, dateVol);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + cheminFichier);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur SQL.");
            e.printStackTrace();
        }
    }

    private static void chercherNotamsPourVol(Connection conn, String codeIcao, String dateVol) throws SQLException {
        // Requête SQL pour trouver les NOTAM actifs pour un aéroport donné à une date donnée
        String sql = "SELECT code_notam, message FROM notams WHERE code_icao = ? AND date_debut <= ? AND date_fin >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codeIcao); // Définit le code OACI de l'aéroport
            pstmt.setString(2, dateVol);  // Définit la date du vol
            pstmt.setString(3, dateVol);  // Définit la date du vol

            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                if (!found) {
                    System.out.printf("--- NOTAMs trouvés pour l'aéroport %s ---\n", codeIcao);
                    found = true;
                }
                System.out.printf("  -> [%s]: %s\n", rs.getString("code_notam"), rs.getString("message"));
            }

            if (!found) {
                System.out.printf("--- Aucun NOTAM actif trouvé pour %s à cette date ---\n", codeIcao);
            }
        }
    }
}