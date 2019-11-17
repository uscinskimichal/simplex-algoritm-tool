package util;

import dialogs.Dialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public abstract class Navigate {


    public void changeScene(BorderPane borderPane, String fxmlName) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/"+fxmlName));
        borderPane.setCenter(root);
    }


    public void close(ActionEvent event) {
        if (Dialog.popConfirmationDialog("Czy jesteś pewien?", "Czy na pewno chcesz wyjść?", "Wyjście")) {
            Platform.exit();
        }
    }

}
