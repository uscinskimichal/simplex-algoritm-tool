package controllers;

import dialogs.Dialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import util.Navigate;

import java.net.URL;
import java.util.ResourceBundle;


public class MainWindowController extends Navigate implements Initializable {

    // ObservableList<Label> labels = FXCollections.observableArrayList();
    // ObservableList<TextField> fields = FXCollections.observableArrayList();

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Pane mainPane, constraintsPane, variablesPane;

    @FXML
    private ComboBox<Integer> numberOfVariablesComboBox, numberOfConstraintsComboBox;

    @FXML
    private ComboBox<String> comboBoxFunctionCriteria;


    private Label addLabel(String message, int layoutX, int layoutY, Pane pane, int i) {

        Label label = new Label(message);
        if (i + 1 == numberOfVariablesComboBox.getValue())
            label.setText("X" + (i + 1));
        label.setLayoutX(layoutX);
        label.setLayoutY(layoutY);
        pane.getChildren().add(label);
        return label;
    }

    private Label addLabel(String message, int layoutX, int layoutY, Pane pane) {

        Label label = new Label(message);
        label.setLayoutX(layoutX);
        label.setLayoutY(layoutY);
        pane.getChildren().add(label);
        return label;
    }

    private TextField addTextField(int layoutX, int layoutY, Pos position, Pane pane) {
        TextField textField = new TextField();
        textField.setLayoutX(layoutX);
        textField.setLayoutY(layoutY);
        textField.setPrefSize(50, 20);
        textField.setAlignment(position);
        pane.getChildren().add(textField);
        return textField;
    }

    private ComboBox<String> addComboBoxString(int layoutX, int layoutY, Pane pane) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setLayoutX(layoutX);
        comboBox.setLayoutY(layoutY);
        comboBox.setItems(FXCollections.observableArrayList("<=", ">=", "="));
        comboBox.getSelectionModel().select(0);
        pane.getChildren().add(comboBox);
        return comboBox;
    }


    private void generateVariables() {
        variablesPane.getChildren().clear();

        for (int variables = 0; variables < numberOfVariablesComboBox.getValue(); variables++) {
            addLabel("X" + (variables + 1) + " + ", 71 + 80 * variables, 44, variablesPane, variables);
            addTextField(20 + 80 * variables, 40, Pos.CENTER_RIGHT, variablesPane);
        }

        //changeScene(borderPane, "TestWindowView.fxml");
    }

    private void generateConstraints() {
        constraintsPane.getChildren().clear();

        for (int constraints = 0; constraints < numberOfConstraintsComboBox.getValue(); constraints++) {
            addLabel((constraints + 1) + " ) ", 21, 50 + 35 * constraints, constraintsPane);
            addComboBoxString(50 + numberOfVariablesComboBox.getValue() * 80, 46 + 35 * constraints, constraintsPane);
            addTextField(120 + numberOfVariablesComboBox.getValue() * 80, 46 + constraints * 35, Pos.CENTER, constraintsPane);

            for (int variables = 0; variables < numberOfVariablesComboBox.getValue(); variables++) {
                addLabel("X" + (variables + 1) + " + ", 101 + 80 * variables, 50 + constraints * 35, constraintsPane, variables);
                addTextField(50 + 80 * variables, 46 + constraints * 35, Pos.CENTER_RIGHT, constraintsPane);
            }
        }
    }


    /// TO DO SPOSOB ODNOSZENIA SIE DO POSZCEGOLNYCH POL, LISTA CZY COS

    @FXML
    void generate() {
        generateVariables();
        generateConstraints();
    }

    @FXML
    void close(ActionEvent event) {
        if (Dialog.popConfirmationDialog("Czy jesteś pewien?", "Czy na pewno chcesz wyjść?", "Wyjście")) {
            Platform.exit();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberOfVariablesComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        numberOfConstraintsComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        comboBoxFunctionCriteria.setItems(FXCollections.observableArrayList("Max", "Min"));
        numberOfVariablesComboBox.getSelectionModel().select(2);
        numberOfConstraintsComboBox.getSelectionModel().select(2);

        generate();
    }
}
