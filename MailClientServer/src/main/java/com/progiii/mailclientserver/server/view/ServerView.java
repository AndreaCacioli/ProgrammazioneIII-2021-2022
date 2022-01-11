package com.progiii.mailclientserver.server.view;

import com.progiii.mailclientserver.server.controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerView extends Application {
    /*
    * The main method that gets called.
    * The application is the server that sends the emails back and forth between the clients.
    * It has a GUI that shows what is happening in real time
    * and allows the user to stop/resume the service with a button.
    * */
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