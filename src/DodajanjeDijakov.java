import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.DatePicker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;

public class DodajanjeDijakov extends Application {
    // Podatki za povezavo z bazo podatkov
    static final String URL = "jdbc:postgresql://ep-plain-sky-a2q2qj2y.eu-central-1.aws.neon.tech/ProjektDijaki";
    static final String UPORABNIŠKO_IME = "tim.urisek";
    static final String GESLO = "fj27rezZVgtP";

    @Override
    public void start(Stage primaryStage) {
        // Ustvari komponente za uporabniški vmesnik
        Label imeLabel = new Label("Ime:");
        TextField imeField = new TextField();
        Label priimekLabel = new Label("Priimek:");
        TextField priimekField = new TextField();
        Label razredLabel = new Label("Razred:");
        TextField razredField = new TextField();
        Label rojstniDatumLabel = new Label("Rojstni datum:");
        DatePicker rojstniDatumPicker = new DatePicker();
        Label emailLabel = new Label("E-pošta:");
        TextField emailField = new TextField();
        Label krajLabel = new Label("Kraj:");
        ComboBox<String> krajComboBox = new ComboBox<>();
        Button dodajButton = new Button("Dodaj");

        // Napolni ComboBox z imeni krajev
        ObservableList<String> kraji = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT ime FROM kraji");
            while (resultSet.next()) {
                kraji.add(resultSet.getString("ime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        krajComboBox.setItems(kraji);

        // Nastavi akcijo za gumb za dodajanje
        dodajButton.setOnAction(event -> {
            String ime = imeField.getText();
            String priimek = priimekField.getText();
            String razred = razredField.getText(); // Spremenjeno v String
            String rojstniDatum = rojstniDatumPicker.getValue().toString();
            String email = emailField.getText();
            String izbranKraj = krajComboBox.getValue();
            int krajId = pridobiKrajId(izbranKraj); // Metoda za pridobitev kraj_id glede na ime kraja
            // Preveri, ali je e-poštni naslov že vnešen
            if (jeEmailZeDodan(email)) {
                prikaziSporocilo("Napaka", "Ta e-poštni naslov je že vnešen.");
                return;
            }
            // Pokliči metodo za dodajanje podatkov v bazo
            dodajPodatkeVBazo(ime, priimek, razred, rojstniDatum, email, krajId);
            // Po dodajanju izbrišemo vsebino polj
            imeField.clear();
            priimekField.clear();
            razredField.clear();
            rojstniDatumPicker.setValue(null);
            emailField.clear();
            krajComboBox.setValue(null);
            // Prikaži sporočilo o uspešnem dodajanju
            prikaziSporocilo("Dodajanje podatkov", "Podatki uspešno dodani.");
            // Zapri okno po uspešnem dodajanju
            primaryStage.close();
        });

        // Ustvari postavitev in dodaj komponente
        GridPane root = new GridPane();
        root.addRow(0, imeLabel, imeField);
        root.addRow(1, priimekLabel, priimekField);
        root.addRow(2, razredLabel, razredField);
        root.addRow(3, rojstniDatumLabel, rojstniDatumPicker);
        root.addRow(4, emailLabel, emailField);
        root.addRow(5, krajLabel, krajComboBox);
        root.add(dodajButton, 1, 6);

        // Ustvari prizorišče in ga nastavi na odru
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Vnos podatkov");
        primaryStage.show();
    }

    private int pridobiKrajId(String imeKraja) {
        int krajId = -1;
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT kraj_id FROM kraji WHERE ime = '" + imeKraja + "'");
            if (resultSet.next()) {
                krajId = resultSet.getInt("kraj_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return krajId;
    }

    private boolean jeEmailZeDodan(String email) {
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM dijaki WHERE email = '" + email + "'");
            resultSet.next();
            int count = resultSet.getInt("total");
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void dodajPodatkeVBazo(String ime, String priimek, String razred, String rojstniDatum, String email, int krajId) {
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             CallableStatement statement = connection.prepareCall("{call dodaj_dijaka(?, ?, ?, ?, ?, ?)}")) {
            statement.setString(1, ime);
            statement.setString(2, priimek);
            statement.setString(3, razred);
            statement.setDate(4, java.sql.Date.valueOf(rojstniDatum));
            statement.setString(5, email);
            statement.setInt(6, krajId);
            statement.execute();
            // Prikaži sporočilo o uspešnem dodajanju
            prikaziSporocilo("Dodajanje podatkov", "Podatki uspešno dodani.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Metoda za prikazovanje sporočil v obliki pop-up okna
    private void prikaziSporocilo(String naslov, String sporocilo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(sporocilo);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
