package controllers;

import dialogs.Dialog;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import services.SimplexCore;
import util.Navigate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class MainWindowController extends Navigate implements Initializable {

    List<TextField> variablesTextFieldList = new ArrayList<>();
    List<List<TextField>> listConstraintsTextFieldList = new ArrayList<>();
    List<ComboBox<String>> constraintMarkComboBoxList = new ArrayList<>();


    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button solveButton;

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
        constraintMarkComboBoxList.add(comboBox);
        return comboBox;
    }

    private void generateVariables() {
        for (int variables = 0; variables < numberOfVariablesComboBox.getValue(); variables++) {
            addLabel("X" + (variables + 1) + " + ", 71 + 80 * variables, 44, variablesPane, variables);
            variablesTextFieldList.add(addTextField(20 + 80 * variables, 40, Pos.CENTER_RIGHT, variablesPane));

        }
    }

    private void generateConstraints() {
        for (int constraints = 0; constraints < numberOfConstraintsComboBox.getValue(); constraints++) {
            List<TextField> singleRowConstraint = new ArrayList<>();
            listConstraintsTextFieldList.add(singleRowConstraint);

            for (int variables = 0; variables < numberOfVariablesComboBox.getValue(); variables++) {
                addLabel("X" + (variables + 1) + " + ", 101 + 80 * variables, 50 + constraints * 35, constraintsPane, variables);
                singleRowConstraint.add(addTextField(50 + 80 * variables, 46 + constraints * 35, Pos.CENTER_RIGHT, constraintsPane));
            }

            addLabel((constraints + 1) + " ) ", 21, 50 + 35 * constraints, constraintsPane);
            addComboBoxString(50 + numberOfVariablesComboBox.getValue() * 80, 46 + 35 * constraints, constraintsPane);
            singleRowConstraint.add(addTextField(120 + numberOfVariablesComboBox.getValue() * 80, 46 + constraints * 35, Pos.CENTER, constraintsPane));

        }
    }

    private void eraseData() {
        constraintMarkComboBoxList.clear();
        variablesPane.getChildren().clear();
        constraintsPane.getChildren().clear();
        variablesTextFieldList.clear();
        listConstraintsTextFieldList.clear();
    }

    @FXML
    void popAuthorWindow() throws IOException {
        popNewWindow("/view/AuthorWindowView.fxml", "Autor" , 300, 300);
    }

    @FXML
    void popAboutWindow() throws IOException {
        popNewWindow("/view/AboutWindowView.fxml", "O programie" , 300, 300);
    }

    @FXML
    void generate() {
        eraseData();
        generateVariables();
        generateConstraints();
    }

    @FXML
    void solve() {
        boolean maximization;
        if (comboBoxFunctionCriteria.getSelectionModel().getSelectedItem().equals("Max"))
            maximization = true;
        else
            maximization = false;

        try {
            new SimplexCore(
                    variablesTextFieldList
                            .stream()
                            .map(a -> {
                                if (a.getText().isEmpty())
                                    throw new NumberFormatException();
                                else
                                    return new BigDecimal(a.getText());
                            })
                            .collect(Collectors.toList())
                    ,
                    listConstraintsTextFieldList
                            .stream()
                            .map(a -> a.stream()
                                    .map(b -> {
                                        if (b.getText().isEmpty())
                                            throw new NumberFormatException();
                                        else
                                            return new BigDecimal(b.getText());
                                    }).collect(Collectors.toList())).collect(Collectors.toList())
                    ,

                    maximization
                    ,
                    constraintMarkComboBoxList
                            .stream()
                            .map(a -> a.getSelectionModel().getSelectedItem())
                            .collect(Collectors.toList()
                            ));

        } catch (NumberFormatException nfe) {
            Dialog.popErrorDialog("Błąd!", "Błędne dane", "Wprowadzono błędne dane, upewnij się, że separatorem jest \".\" Oraz czy wprowadzono wszystkie dane.");
        }
    }

    @FXML
    void firstExample(ActionEvent event) {
        numberOfVariablesComboBox.getSelectionModel().select(2);
        numberOfConstraintsComboBox.getSelectionModel().select(2);
        comboBoxFunctionCriteria.getSelectionModel().select(0);

        generate();

        variablesTextFieldList.get(0).setText("3");
        variablesTextFieldList.get(1).setText("5");
        variablesTextFieldList.get(2).setText("2");

        listConstraintsTextFieldList.get(0).get(0).setText("3");
        listConstraintsTextFieldList.get(0).get(1).setText("2");
        listConstraintsTextFieldList.get(0).get(2).setText("4");
        listConstraintsTextFieldList.get(0).get(3).setText("50");

        listConstraintsTextFieldList.get(1).get(0).setText("7");
        listConstraintsTextFieldList.get(1).get(1).setText("6");
        listConstraintsTextFieldList.get(1).get(2).setText("4");
        listConstraintsTextFieldList.get(1).get(3).setText("100");

        listConstraintsTextFieldList.get(2).get(0).setText("5");
        listConstraintsTextFieldList.get(2).get(1).setText("2");
        listConstraintsTextFieldList.get(2).get(2).setText("12");
        listConstraintsTextFieldList.get(2).get(3).setText("70");


        constraintMarkComboBoxList.get(0).getSelectionModel().select(0);
        constraintMarkComboBoxList.get(1).getSelectionModel().select(1);
        constraintMarkComboBoxList.get(2).getSelectionModel().select(2);
    }

    @FXML
    void secondExample(ActionEvent event) {
        numberOfVariablesComboBox.getSelectionModel().select(1);
        numberOfConstraintsComboBox.getSelectionModel().select(1);
        comboBoxFunctionCriteria.getSelectionModel().select(0);

        generate();

        variablesTextFieldList.get(0).setText("4");
        variablesTextFieldList.get(1).setText("5");

        listConstraintsTextFieldList.get(0).get(0).setText("20");
        listConstraintsTextFieldList.get(0).get(1).setText("30");
        listConstraintsTextFieldList.get(0).get(2).setText("100");

        listConstraintsTextFieldList.get(1).get(0).setText("6");
        listConstraintsTextFieldList.get(1).get(1).setText("0");
        listConstraintsTextFieldList.get(1).get(2).setText("5");

        constraintMarkComboBoxList.get(0).getSelectionModel().select(0);
        constraintMarkComboBoxList.get(1).getSelectionModel().select(0);
    }

    @FXML
    void thirdExample(ActionEvent event) {
        numberOfVariablesComboBox.getSelectionModel().select(3);
        numberOfConstraintsComboBox.getSelectionModel().select(2);
        comboBoxFunctionCriteria.getSelectionModel().select(0);

        generate();

        variablesTextFieldList.get(0).setText("2");
        variablesTextFieldList.get(1).setText("4");
        variablesTextFieldList.get(2).setText("6");
        variablesTextFieldList.get(3).setText("8");

        listConstraintsTextFieldList.get(0).get(0).setText("4");
        listConstraintsTextFieldList.get(0).get(1).setText("0");
        listConstraintsTextFieldList.get(0).get(2).setText("0");
        listConstraintsTextFieldList.get(0).get(3).setText("0");
        listConstraintsTextFieldList.get(0).get(4).setText("10");

        listConstraintsTextFieldList.get(1).get(0).setText("2");
        listConstraintsTextFieldList.get(1).get(1).setText("2");
        listConstraintsTextFieldList.get(1).get(2).setText("2");
        listConstraintsTextFieldList.get(1).get(3).setText("2");
        listConstraintsTextFieldList.get(1).get(4).setText("200");

        listConstraintsTextFieldList.get(2).get(0).setText("4");
        listConstraintsTextFieldList.get(2).get(1).setText("0");
        listConstraintsTextFieldList.get(2).get(2).setText("2");
        listConstraintsTextFieldList.get(2).get(3).setText("4");
        listConstraintsTextFieldList.get(2).get(4).setText("76");

        comboBoxFunctionCriteria.getSelectionModel().select(1);
        comboBoxFunctionCriteria.getSelectionModel().select(0);
        comboBoxFunctionCriteria.getSelectionModel().select(0);
    }

    @FXML
    void fourthExample(ActionEvent event) {
        numberOfVariablesComboBox.getSelectionModel().select(4);
        numberOfConstraintsComboBox.getSelectionModel().select(4);
        comboBoxFunctionCriteria.getSelectionModel().select(0);

        generate();

        variablesTextFieldList.get(0).setText("1.5");
        variablesTextFieldList.get(1).setText("3");
        variablesTextFieldList.get(2).setText("-4");
        variablesTextFieldList.get(3).setText("2.2");
        variablesTextFieldList.get(4).setText("6");

        listConstraintsTextFieldList.get(0).get(0).setText("-4");
        listConstraintsTextFieldList.get(0).get(1).setText("2.2");
        listConstraintsTextFieldList.get(0).get(2).setText("5");
        listConstraintsTextFieldList.get(0).get(3).setText("0");
        listConstraintsTextFieldList.get(0).get(4).setText("4");
        listConstraintsTextFieldList.get(0).get(5).setText("50");

        listConstraintsTextFieldList.get(1).get(0).setText("0");
        listConstraintsTextFieldList.get(1).get(1).setText("5.7");
        listConstraintsTextFieldList.get(1).get(2).setText("-2.1");
        listConstraintsTextFieldList.get(1).get(3).setText("4");
        listConstraintsTextFieldList.get(1).get(4).setText("2");
        listConstraintsTextFieldList.get(1).get(5).setText("41");

        listConstraintsTextFieldList.get(2).get(0).setText("4");
        listConstraintsTextFieldList.get(2).get(1).setText("3");
        listConstraintsTextFieldList.get(2).get(2).setText("5");
        listConstraintsTextFieldList.get(2).get(3).setText("2");
        listConstraintsTextFieldList.get(2).get(4).setText("1");
        listConstraintsTextFieldList.get(2).get(5).setText("100");

        listConstraintsTextFieldList.get(3).get(0).setText("4");
        listConstraintsTextFieldList.get(3).get(1).setText("0");
        listConstraintsTextFieldList.get(3).get(2).setText("-3.2");
        listConstraintsTextFieldList.get(3).get(3).setText("4.4");
        listConstraintsTextFieldList.get(3).get(4).setText("5");
        listConstraintsTextFieldList.get(3).get(5).setText("-6");

        listConstraintsTextFieldList.get(4).get(0).setText("4");
        listConstraintsTextFieldList.get(4).get(1).setText("2");
        listConstraintsTextFieldList.get(4).get(2).setText("5.5");
        listConstraintsTextFieldList.get(4).get(3).setText("-3.7");
        listConstraintsTextFieldList.get(4).get(4).setText("1");
        listConstraintsTextFieldList.get(4).get(5).setText("4");

        constraintMarkComboBoxList.get(0).getSelectionModel().select(0);
        constraintMarkComboBoxList.get(1).getSelectionModel().select(1);
        constraintMarkComboBoxList.get(2).getSelectionModel().select(2);
        constraintMarkComboBoxList.get(3).getSelectionModel().select(0);
        constraintMarkComboBoxList.get(4).getSelectionModel().select(1);



    }

    @FXML
    void fifthExample(ActionEvent event) {
        numberOfVariablesComboBox.getSelectionModel().select(3);
        numberOfConstraintsComboBox.getSelectionModel().select(1);
        comboBoxFunctionCriteria.getSelectionModel().select(0);

        generate();

        variablesTextFieldList.get(0).setText("4.2");
        variablesTextFieldList.get(1).setText("-6.6");
        variablesTextFieldList.get(2).setText("4");
        variablesTextFieldList.get(3).setText("32");

        listConstraintsTextFieldList.get(0).get(0).setText("-0.6");
        listConstraintsTextFieldList.get(0).get(1).setText("2");
        listConstraintsTextFieldList.get(0).get(2).setText("0.5");
        listConstraintsTextFieldList.get(0).get(3).setText("4");
        listConstraintsTextFieldList.get(0).get(4).setText("152");

        listConstraintsTextFieldList.get(1).get(0).setText("14");
        listConstraintsTextFieldList.get(1).get(1).setText("0");
        listConstraintsTextFieldList.get(1).get(2).setText("12.2");
        listConstraintsTextFieldList.get(1).get(3).setText("-3");
        listConstraintsTextFieldList.get(1).get(4).setText("-4");

        constraintMarkComboBoxList.get(0).getSelectionModel().select(0);
        constraintMarkComboBoxList.get(1).getSelectionModel().select(0);


    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberOfVariablesComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        numberOfConstraintsComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        comboBoxFunctionCriteria.setItems(FXCollections.observableArrayList("Max", "Min"));
        numberOfVariablesComboBox.getSelectionModel().select(2);
        numberOfConstraintsComboBox.getSelectionModel().select(2);

        comboBoxFunctionCriteria.getSelectionModel().select(0);
        generate();
    }
}
