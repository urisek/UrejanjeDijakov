import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {
    // Podatki za povezavo z bazo podatkov
    static final String URL = "jdbc:postgresql://ep-plain-sky-a2q2qj2y.eu-central-1.aws.neon.tech/ProjektDijaki";
    static final String UPORABNIŠKO_IME = "tim.urisek";
    static final String GESLO = "fj27rezZVgtP";

    @Override
    public void start(Stage primaryStage) {
        // Ustvari glavni vmesnik za dodajanje podatkov
        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));

        // Ustvari tabelo za prikaz učencev
        TableView<Ucenec> tabelaUcenca = new TableView<>();

        // Ustvari stolpce za ime, priimek, razred in email
        TableColumn<Ucenec, String> imeStolpec = new TableColumn<>("Ime");
        imeStolpec.setCellValueFactory(new PropertyValueFactory<>("ime"));

        TableColumn<Ucenec, String> priimekStolpec = new TableColumn<>("Priimek");
        priimekStolpec.setCellValueFactory(new PropertyValueFactory<>("priimek"));

        TableColumn<Ucenec, String> razredStolpec = new TableColumn<>("Razred");
        razredStolpec.setCellValueFactory(new PropertyValueFactory<>("razred"));

        TableColumn<Ucenec, String> emailStolpec = new TableColumn<>("E-pošta");
        emailStolpec.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Dodaj stolpce v tabelo
        tabelaUcenca.getColumns().addAll(imeStolpec, priimekStolpec, razredStolpec, emailStolpec);

        // Pridobi podatke o učencih iz baze podatkov
        ObservableList<Ucenec> ucenci = pridobiVseUcenca();
        tabelaUcenca.setItems(ucenci);

        // Dodaj tabelo v glavni vmesnik
        vBox.getChildren().addAll(tabelaUcenca);

        // Dodaj gumb za dodajanje novega dijaka
        Button dodajDijakaButton = new Button("DODAJ NOVEGA DIJAKA");
        dodajDijakaButton.setOnAction(event -> prikaziDodajanjeDijaka(primaryStage, tabelaUcenca));
        vBox.getChildren().add(dodajDijakaButton);

        root.setCenter(vBox);

        // Ustvari prizorišče in ga nastavi na odru
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Seznam učencev");
        primaryStage.show();
    }

    private void prikaziDodajanjeDijaka(Stage primaryStage, TableView<Ucenec> tabelaUcenca) {
        // Ustvari novo okno za dodajanje dijaka
        DodajanjeDijakov dodajanjeDijaka = new DodajanjeDijakov();
        Stage dodajanjeDijakaStage = new Stage();
        dodajanjeDijaka.start(dodajanjeDijakaStage);

        // Dodaj poslušalca za dogodek, ko se okno za dodajanje dijaka zapre
        dodajanjeDijakaStage.setOnHiding(event -> {
            // Posodobi tabelo učencev po dodajanju novega dijaka
            tabelaUcenca.setItems(pridobiVseUcenca());
        });
    }

    private ObservableList<Ucenec> pridobiVseUcenca() {
        ObservableList<Ucenec> ucenci = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM dijaki");
            while (resultSet.next()) {
                String ime = resultSet.getString("ime");
                String priimek = resultSet.getString("priimek");
                String razred = resultSet.getString("razred");
                String email = resultSet.getString("email");
                ucenci.add(new Ucenec(ime, priimek, razred, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ucenci;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Razred za predstavitev učenca
    public static class Ucenec {
        private final String ime;
        private final String priimek;
        private final String razred;
        private final String email;

        public Ucenec(String ime, String priimek, String razred, String email) {
            this.ime = ime;
            this.priimek = priimek;
            this.razred = razred;
            this.email = email;
        }

        public String getIme() {
            return ime;
        }

        public String getPriimek() {
            return priimek;
        }

        public String getRazred() {
            return razred;
        }

        public String getEmail() {
            return email;
        }
    }
}
