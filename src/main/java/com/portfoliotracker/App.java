package com.portfoliotracker;

import com.portfoliotracker.controller.PassphraseController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws Exception {
        showPassphraseDialog();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        
        stage.show();
    }

    private void showPassphraseDialog() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/passphrase-dialog.fxml"));
        Parent dialogRoot = loader.load();
        
        Scene dialogScene = new Scene(dialogRoot);
        dialogScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setTitle("Encryption");
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
