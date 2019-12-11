package util;

import dialogs.Dialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Navigate {


    public void changeScene(BorderPane borderPane, String fxmlName) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/" + fxmlName));
        borderPane.setCenter(root);
    }


    public void close(ActionEvent event) {
        if (Dialog.popConfirmationDialog("Czy jesteś pewien?", "Czy na pewno chcesz wyjść?", "Wyjście")) {
            Platform.exit();
        }
    }


    public Stage popNewWindow(String pathToFXML, String title, int width, int height) throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource(pathToFXML));
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        return stage;
    }

    public Object popNewWindowWithParameter(String pathToFXML, String title, int width, int height) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(pathToFXML));
        Parent root = (Parent) fxmlLoader.load();
        Object controller = fxmlLoader.<Object>getController();
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        return controller;
    }

}
