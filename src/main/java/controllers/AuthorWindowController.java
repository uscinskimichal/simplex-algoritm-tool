package controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.Navigate;

public class AuthorWindowController extends Navigate {

    @FXML
    AnchorPane anchorPane;

    @FXML
    void goBack() {
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.close();
    }
}
