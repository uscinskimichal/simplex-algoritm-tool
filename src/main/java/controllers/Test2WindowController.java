package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import util.Navigate;

import java.io.IOException;

public class Test2WindowController extends Navigate {

    BorderPane borderPane;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    void goBack(ActionEvent event) throws IOException {
        this.borderPane = (BorderPane) anchorPane.getParent();
        changeScene(borderPane,"TestWindowView.fxml");

    }
}
