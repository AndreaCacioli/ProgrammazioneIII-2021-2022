package com.progiii.mailclientserver.server.view;

import com.progiii.mailclientserver.server.controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerView extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ServerView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 480);
        ServerController controller = fxmlLoader.getController();
        stage.setOnCloseRequest((event) -> controller.stopServer());
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}