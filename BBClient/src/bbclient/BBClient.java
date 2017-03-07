package bbclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BBClient extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocBB.fxml"));
        
        Scene scene = new Scene(root,800,600);
        
        stage.setTitle("BeatBoxMaker");
        stage.setResizable(false);
        
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((event) -> {System.exit(0);
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
    
}
