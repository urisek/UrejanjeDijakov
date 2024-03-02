import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Databaseconnection {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://ep-plain-sky-a2q2qj2y.eu-central-1.aws.neon.tech/ProjektDijaki?user=tim.urisek&password=fj27rezZVgtP&sslmode=require");
            System.out.println("Povezan na bazo.");
        } catch (SQLException e) {
            System.out.println("Napaka pri povezavi na bazo: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println("Napaka pri zapiranju povezave: " + ex.getMessage());
            }
        }
    }
}
