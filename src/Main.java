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
import java.util.Optional;
import javafx.scene.control.TableView;

import java.sql.*;

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

        // Ustvari stolpce za ime, priimek, razred, email, gumb za urejanje in gumb za brisanje
        TableColumn<Ucenec, String> imeStolpec = new TableColumn<>("Ime");
        imeStolpec.setCellValueFactory(new PropertyValueFactory<>("ime"));

        TableColumn<Ucenec, String> priimekStolpec = new TableColumn<>("Priimek");
        priimekStolpec.setCellValueFactory(new PropertyValueFactory<>("priimek"));

        TableColumn<Ucenec, String> razredStolpec = new TableColumn<>("Razred");
        razredStolpec.setCellValueFactory(new PropertyValueFactory<>("razred"));

        TableColumn<Ucenec, String> emailStolpec = new TableColumn<>("E-pošta");
        emailStolpec.setCellValueFactory(new PropertyValueFactory<>("email"));


// Set up table style
        tabelaUcenca.setStyle("-fx-font-size: 14px;");

// Set up column styles
        imeStolpec.setStyle("-fx-font-weight: bold;-fx-alignment: CENTER;"); // Example style for the 'Ime' column
        priimekStolpec.setStyle("-fx-font-weight: bold;-fx-alignment: CENTER;"); // Example style for the 'Priimek' column, aligning text to the center
        razredStolpec.setStyle("-fx-alignment: CENTER;"); // Example style for the 'Razred' column, setting text color to green
        emailStolpec.setStyle("-fx-alignment: CENTER;"); // Example style for the 'E-pošta' column, setting background color to light blue


        TableColumn<Ucenec, Void> urejanjeStolpec = new TableColumn<>("Uredi");

        urejanjeStolpec.setCellFactory(param -> new TableCell<Ucenec, Void>() {
            private final Button gumbUredi = new Button("Uredi");

            {
                gumbUredi.setOnAction(event -> {
                    Ucenec ucenec = getTableView().getItems().get(getIndex());
                    prikaziObrazecZaUrejanje(primaryStage, ucenec, tabelaUcenca);

                });
                gumbUredi.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(gumbUredi);
                }
            }
        });

        TableColumn<Ucenec, Void> deleteStolpec = new TableColumn<>("Izbriši");
        deleteStolpec.setCellFactory(param -> new TableCell<Ucenec, Void>() {
            private final Button deleteButton = new Button("Izbriši");

            {
                deleteButton.setOnAction(event -> {
                    Ucenec ucenec = getTableView().getItems().get(getIndex());
                    boolean confirmed = confirmDelete("Ali ste prepričani, da želite izbrisati učenca?");
                    if (confirmed) {
                        deleteUcenecFromDatabase(ucenec);
                        tabelaUcenca.getItems().remove(ucenec);
                    }

                });
                deleteButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        TableColumn<Ucenec, Void> pregledOcenStolpec = new TableColumn<>("Pregled ocen");
        pregledOcenStolpec.setCellFactory(param -> new TableCell<Ucenec, Void>() {
            private final Button pregledOcenButton = new Button("Preglej ocene");

            {
                pregledOcenButton.setOnAction(event -> {
                    Ucenec ucenec = getTableView().getItems().get(getIndex());
                    prikaziPregledOcen(primaryStage, ucenec);
                });
                pregledOcenButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pregledOcenButton);
                }
            }
        });

        // Dodaj stolpce v tabelo
        tabelaUcenca.getColumns().addAll(imeStolpec, priimekStolpec, razredStolpec, emailStolpec, urejanjeStolpec, deleteStolpec, pregledOcenStolpec);

        // Pridobi podatke o učencih iz baze podatkov
        ObservableList<Ucenec> ucenci = pridobiVseUcenca();
        tabelaUcenca.setItems(ucenci);

        // Dodaj tabelo v glavni vmesnik
        vBox.getChildren().addAll(tabelaUcenca);

        // Dodaj gumb za dodajanje novega dijaka
        Button dodajDijakaButton = new Button("DODAJ NOVEGA DIJAKA");
        dodajDijakaButton.setOnAction(event -> prikaziDodajanjeDijaka(primaryStage, tabelaUcenca));
        dodajDijakaButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");

        vBox.getChildren().add(dodajDijakaButton);

        root.setCenter(vBox);
        root.setStyle("-fx-background-color: #FFA500;");
        // Ustvari prizorišče in ga nastavi na odru
        Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        // Set title with bold and centered text
        Label titleLabel = new Label("SEZNAM UČENCEV");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        root.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, javafx.geometry.Pos.CENTER);
        primaryStage.setTitle("");
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

    private void prikaziObrazecZaUrejanje(Stage primaryStage, Ucenec ucenec, TableView<Ucenec> tabelaUcenca) {
        // Ustvari obrazec za urejanje učenca in prikaži ga
        UrejanjeDijaka urejanjeDijaka = new UrejanjeDijaka(ucenec, tabelaUcenca);
        Stage urejanjeDijakaStage = new Stage();
        urejanjeDijaka.start(urejanjeDijakaStage);

        // Dodaj poslušalca za dogodek, ko se obrazec za urejanje učenca zapre
        urejanjeDijakaStage.setOnHiding(event -> {
            // Posodobi tabelo učencev po urejanju učenca
            tabelaUcenca.setItems(pridobiVseUcenca());
        });
    }

    private void prikaziPregledOcen(Stage primaryStage, Ucenec ucenec) {
        // Prikaz pregleda ocen se zdaj nahaja tukaj
        Stage pregledOcenStage = new Stage();
        OceneDijaka.prikaziPregledOcen(pregledOcenStage, ucenec);
    }

    private ObservableList<Ucenec> pridobiVseUcenca() {
        ObservableList<Ucenec> ucenci = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM dijaki");
            while (resultSet.next()) {
                int id = resultSet.getInt("dijak_id");
                String ime = resultSet.getString("ime");
                String priimek = resultSet.getString("priimek");
                String razred = resultSet.getString("razred");
                String email = resultSet.getString("email");
                ucenci.add(new Ucenec(id, ime, priimek, razred, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ucenci;
    }

    private void deleteUcenecFromDatabase(Ucenec ucenec) {
        try (Connection connection = DriverManager.getConnection(URL, UPORABNIŠKO_IME, GESLO);
             CallableStatement statement = connection.prepareCall("{call izbrisi_dijaka(?)}")) {
            statement.setInt(1, ucenec.getId());
            statement.executeUpdate();
            System.out.println("Učenec uspešno izbrisan.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean confirmDelete(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potrditev brisanja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType yesButton = new ButtonType("Da", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Ne", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Razred za predstavitev učenca
    public static class Ucenec {
        private int id;
        private String ime;
        private String priimek;
        private String razred;
        private String email;

        public Ucenec(int id, String ime, String priimek, String razred, String email) {
            this.id = id;
            this.ime = ime;
            this.priimek = priimek;
            this.razred = razred;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getIme() {
            return ime;
        }

        public void setIme(String ime) {
            this.ime = ime;
        }

        public String getPriimek() {
            return priimek;
        }

        public void setPriimek(String priimek) {
            this.priimek = priimek;
        }

        public String getRazred() {
            return razred;
        }

        public void setRazred(String razred) {
            this.razred = razred;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

}