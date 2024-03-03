import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class OceneDijaka extends Application {

    // Dodaj novo okno za pregled ocen
    public static void prikaziPregledOcen(Stage primaryStage, Main.Ucenec ucenec) {
        Stage pregledOcenStage = new Stage();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));

        Label naslovLabel = new Label("Ocene za učenca: " + ucenec.getIme() + " " + ucenec.getPriimek());
        naslovLabel.setStyle("-fx-font-weight: bold");

        TableView<Ocena> tabelaOcen = new TableView<>();

        TableColumn<Ocena, String> predmetStolpec = new TableColumn<>("Predmet");
        predmetStolpec.setCellValueFactory(new PropertyValueFactory<>("predmet"));

        TableColumn<Ocena, Integer> ocenaStolpec = new TableColumn<>("Ocena");
        ocenaStolpec.setCellValueFactory(new PropertyValueFactory<>("ocena"));

        TableColumn<Ocena, LocalDate> datumStolpec = new TableColumn<>("Datum ocenjevanja");
        datumStolpec.setCellValueFactory(new PropertyValueFactory<>("datumOcenjevanja"));

        tabelaOcen.getColumns().addAll(predmetStolpec, ocenaStolpec, datumStolpec);

        ObservableList<Ocena> ocene = pridobiOceneZaUcenca(ucenec.getId());
        tabelaOcen.setItems(ocene);

        vBox.getChildren().addAll(naslovLabel, tabelaOcen);

        Button dodajOcenoButton = new Button("Dodaj novo oceno");
        dodajOcenoButton.setOnAction(event -> dodajNovoOceno(pregledOcenStage, ucenec, tabelaOcen));
        vBox.getChildren().add(dodajOcenoButton);

        Scene scene = new Scene(vBox, 300, 200);
        pregledOcenStage.setScene(scene);
        pregledOcenStage.setTitle("Pregled ocen");
        pregledOcenStage.show();
    }

    // Pridobi ocene za določenega učenca iz baze podatkov
    private static ObservableList<Ocena> pridobiOceneZaUcenca(int dijakId) {
        ObservableList<Ocena> ocene = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             PreparedStatement statement = connection.prepareStatement("SELECT p.ime AS predmet, o.ocena, o.datum_ocenjevanja FROM ocene o JOIN predmeti p ON o.predmet_id = p.predmet_id WHERE o.dijak_id = ?")) {
            statement.setInt(1, dijakId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String predmet = resultSet.getString("predmet");
                int ocena = resultSet.getInt("ocena");
                LocalDate datumOcenjevanja = resultSet.getDate("datum_ocenjevanja").toLocalDate();
                ocene.add(new Ocena(predmet, ocena, datumOcenjevanja));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ocene;
    }

    // Razred za predstavitev ocene
    public static class Ocena {
        private String predmet;
        private int ocena;
        private LocalDate datumOcenjevanja;

        public Ocena(String predmet, int ocena, LocalDate datumOcenjevanja) {
            this.predmet = predmet;
            this.ocena = ocena;
            this.datumOcenjevanja = datumOcenjevanja;
        }

        public String getPredmet() {
            return predmet;
        }

        public void setPredmet(String predmet) {
            this.predmet = predmet;
        }

        public int getOcena() {
            return ocena;
        }

        public void setOcena(int ocena) {
            this.ocena = ocena;
        }

        public LocalDate getDatumOcenjevanja() {
            return datumOcenjevanja;
        }

        public void setDatumOcenjevanja(LocalDate datumOcenjevanja) {
            this.datumOcenjevanja = datumOcenjevanja;
        }

    }

    // Dodaj novo oceno
    private static void dodajNovoOceno(Stage primaryStage, Main.Ucenec ucenec, TableView<Ocena> tabelaOcen) {
        Stage dodajOcenoStage = new Stage();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));

        Label naslovLabel = new Label("Dodaj novo oceno za učenca: " + ucenec.getIme() + " " + ucenec.getPriimek());
        naslovLabel.setStyle("-fx-font-weight: bold");

        ComboBox<String> predmetComboBox = new ComboBox<>();
        predmetComboBox.setPromptText("Izberi predmet");

        refreshComboBox(predmetComboBox); // Refresh ComboBox

        TextField ocenaTextField = new TextField();
        ocenaTextField.setPromptText("Ocena");

        DatePicker datumOcenjevanjaPicker = new DatePicker();
        datumOcenjevanjaPicker.setPromptText("Datum ocenjevanja");

        Button dodajPredmetButton = new Button("DODAJ NOV PREDMET IN UČITELJA");
        dodajPredmetButton.setOnAction(event -> prikaziNovoOknoDodajanjaPredmetaUcitelja(dodajOcenoStage, predmetComboBox));

        Button potrdiButton = new Button("Potrdi");
        potrdiButton.setOnAction(event -> {
            String predmet = predmetComboBox.getValue();
            int ocena = Integer.parseInt(ocenaTextField.getText());
            LocalDate datumOcenjevanja = datumOcenjevanjaPicker.getValue();
            Timestamp datumOcenjevanjaTimestamp = Timestamp.valueOf(datumOcenjevanja.atStartOfDay());

            Ocena novaOcena = new Ocena(predmet, ocena, datumOcenjevanja);
            tabelaOcen.getItems().add(novaOcena);

            // Save the grade to the database
            dodajOcenoVBazo(ucenec.getId(), predmet, ocena, datumOcenjevanjaTimestamp);

            // Display a success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dodajanje ocene");
            alert.setHeaderText(null);
            alert.setContentText("Ocena je bila uspešno dodana!");
            alert.showAndWait();

            dodajOcenoStage.close();
        });

        vBox.getChildren().addAll(naslovLabel, predmetComboBox, ocenaTextField, datumOcenjevanjaPicker, dodajPredmetButton, potrdiButton);

        Scene scene = new Scene(vBox, 300, 250);
        dodajOcenoStage.setScene(scene);
        dodajOcenoStage.setTitle("Dodaj novo oceno");
        dodajOcenoStage.show();
    }


    // Metoda za prikaz okna za dodajanje novega predmeta in učitelja
    private static void prikaziNovoOknoDodajanjaPredmetaUcitelja(Stage dodajOcenoStage, ComboBox<String> predmetComboBox) {
        Stage dodajNovoOknoStage = new Stage();
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));

        Label naslovLabel = new Label("Dodaj nov predmet in učitelja");
        naslovLabel.setStyle("-fx-font-weight: bold");

        TextField imeUciteljaTextField = new TextField();
        imeUciteljaTextField.setPromptText("Ime učitelja");

        TextField priimekUciteljaTextField = new TextField();
        priimekUciteljaTextField.setPromptText("Priimek učitelja");

        TextField imePredmetaTextField = new TextField();
        imePredmetaTextField.setPromptText("Ime predmeta");

        TextField emailUciteljaTextField = new TextField();
        emailUciteljaTextField.setPromptText("E-pošta učitelja");

        Button dodajButton = new Button("Dodaj");
        dodajButton.setOnAction(event -> {
            dodajPredmetInUcitelja(imePredmetaTextField.getText(), imeUciteljaTextField.getText(), priimekUciteljaTextField.getText(), emailUciteljaTextField.getText());
            refreshComboBox(predmetComboBox); // Refresh ComboBox
            // Close the window for adding a new subject and teacher
            dodajNovoOknoStage.close();
            // Display a success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dodajanje učitelja");
            alert.setHeaderText(null);
            alert.setContentText("Učitelj je bil uspešno dodan!");
            alert.showAndWait();
        });

        vBox.getChildren().addAll(naslovLabel, imeUciteljaTextField, priimekUciteljaTextField, emailUciteljaTextField,  imePredmetaTextField, dodajButton);

        Scene scene = new Scene(vBox, 300, 200);
        dodajNovoOknoStage.setScene(scene);
        dodajNovoOknoStage.setTitle("Dodaj nov predmet in učitelja");
        dodajNovoOknoStage.show();
    }


    // Dodaj novo oceno v bazo podatkov
    private static void dodajOcenoVBazo(int dijakId, String predmet, int ocena, Timestamp datum_ocenjavanja) {
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO ocene (dijak_id, predmet_id, ocena, datum_ocenjevanja) VALUES (?, ?, ?, ?)")) {
            int predmetId = pridobiIdPredmeta(predmet);
            statement.setInt(1, dijakId);
            statement.setInt(2, predmetId);
            statement.setInt(3, ocena);
            statement.setTimestamp(4, datum_ocenjavanja);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Pridobi ID predmeta iz baze podatkov
    private static int pridobiIdPredmeta(String predmet) {
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             PreparedStatement statement = connection.prepareStatement("SELECT predmet_id FROM predmeti WHERE ime = ?")) {
            statement.setString(1, predmet);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("predmet_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Metoda za dodajanje novega predmeta in učitelja
    private static void dodajPredmetInUcitelja(String predmet, String uciteljIme, String uciteljPriimek, String uciteljEmail) {
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             CallableStatement statement = connection.prepareCall("{call dodaj_nov_predmet_in_ucitelja(?, ?, ?, ?)}")) {
            statement.setString(1, predmet);
            statement.setString(2, uciteljIme);
            statement.setString(3, uciteljPriimek);
            statement.setString(4, uciteljEmail);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metoda za osvežitev ComboBox-a s predmeti
    private static void refreshComboBox(ComboBox<String> predmetComboBox) {
        predmetComboBox.getItems().clear();
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             PreparedStatement statement = connection.prepareStatement("SELECT ime FROM predmeti")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String predmet = resultSet.getString("ime");
                predmetComboBox.getItems().add(predmet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Dummy test
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Dummy test
    }
}
