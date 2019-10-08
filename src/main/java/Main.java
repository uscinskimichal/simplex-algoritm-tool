import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainWindowView.fxml"));
        primaryStage.setTitle("Simple simplex algorithm calculator");
        primaryStage.setScene(new Scene(root, 500, 375));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/image/ApplicationIcon.png"));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
