package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public abstract class Navigate {

    @FXML
    public void changeScene(BorderPane borderPane, String fxmlName) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/"+fxmlName));
        borderPane.setCenter(root);
    }
}
