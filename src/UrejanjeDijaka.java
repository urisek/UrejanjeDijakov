import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableView;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UrejanjeDijaka extends Application {
    private Main.Ucenec ucenec;
    private TableView<Main.Ucenec> tabelaUcenca;

    public UrejanjeDijaka(Main.Ucenec ucenec, TableView<Main.Ucenec> tabelaUcenca) {
        this.ucenec = ucenec;
        this.tabelaUcenca = tabelaUcenca;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();

        TextField imeField = new TextField(ucenec.getIme());
        TextField priimekField = new TextField(ucenec.getPriimek());
        TextField razredField = new TextField(ucenec.getRazred());
        TextField emailField = new TextField(ucenec.getEmail());

        Button saveButton = new Button("Shrani");
        saveButton.setOnAction(event -> {
            ucenec.setIme(imeField.getText());
            ucenec.setPriimek(priimekField.getText());
            ucenec.setRazred(razredField.getText());
            ucenec.setEmail(emailField.getText());

            // Update the database with the changes
            updateUcenecInDatabase(ucenec);

            // Display a success message
            showSuccessMessage();

            primaryStage.close();
        });

        root.getChildren().addAll(
                new Label("Ime:"), imeField,
                new Label("Priimek:"), priimekField,
                new Label("Razred:"), razredField,
                new Label("Email:"), emailField,
                saveButton
        );

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Urejanje učenca");
        primaryStage.show();
    }

    private void updateUcenecInDatabase(Main.Ucenec ucenec) {
        try (Connection connection = DriverManager.getConnection(Main.URL, Main.UPORABNIŠKO_IME, Main.GESLO);
             CallableStatement statement = connection.prepareCall("{call uredi_dijaka(?, ?, ?, ?, ?)}")) {
            statement.setInt(1, ucenec.getId());
            statement.setString(2, ucenec.getIme());
            statement.setString(3, ucenec.getPriimek());
            statement.setString(4, ucenec.getRazred());
            statement.setString(5, ucenec.getEmail());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showSuccessMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspeh");
        alert.setHeaderText(null);
        alert.setContentText("Učenec uspešno posodbljen");
        alert.showAndWait();
    }
}
