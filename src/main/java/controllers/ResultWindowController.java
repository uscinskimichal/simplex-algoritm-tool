package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import services.SimplexCore;

public class ResultWindowController {

    private SimplexCore simplexCore;

    public void setSimplexCore(SimplexCore simplexCore) {
        this.simplexCore = simplexCore;
    }


    @FXML
    private TextArea resultArea;

    @FXML
    void goBack() {
        Stage stage = (Stage) resultArea.getScene().getWindow();
        stage.close();
    }


    public void initialize() {
        Platform.runLater( () -> resultArea.setText(simplexCore.returnStringSolution()));


    }


}
