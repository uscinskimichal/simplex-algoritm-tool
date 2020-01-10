import dialogs.Dialog;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.Configuration;


public class Main extends Application {

    private Configuration configuration;

    public Main() {
        this.configuration = new Configuration();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainWindowView.fxml"));
        primaryStage.setTitle("Kalkulator - programowanie liniowe");
        primaryStage.setScene(new Scene(root, configuration.getSceneWidth(), configuration.getSceneHeight()));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/image/ApplicationIcon.png"));
        setOnCloseRequest(primaryStage);
        primaryStage.show();
    }


    private void setOnCloseRequest(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            if (Dialog.popConfirmationDialog("Czy jesteś pewien", "Czy na pewno chcesz wyjść?", "Wyjście"))
                primaryStage.close();
            event.consume();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
