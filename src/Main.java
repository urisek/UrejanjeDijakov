import java.sql.*;

public class Main {
    // Podatki za povezavo z bazo podatkov
    static final String URL = "jdbc:postgresql://ep-plain-sky-a2q2qj2y.eu-central-1.aws.neon.tech/ProjektDijaki";
    static final String UPORABNIŠKO_IME = "tim.urisek";
    static final String GESLO = "fj27rezZVgtP";

    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Poveži se z bazo podatkov
            connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);

            // Izvedi poizvedbo za pridobitev dijakov
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM dijaki");

            // Izpiši rezultate
            while (resultSet.next()) {
                String ime = resultSet.getString("ime");
                String priimek = resultSet.getString("priimek");
                System.out.println("Ime: " + ime + ", Priimek: " + priimek);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Zapri povezavo, izjave in rezultat
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
