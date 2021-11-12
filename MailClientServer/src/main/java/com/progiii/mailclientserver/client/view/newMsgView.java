package com.progiii.mailclientserver.client.view;

import com.progiii.mailclientserver.client.controller.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class newMsgView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("newMsgView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("New Message");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(ClientView.class.getResource("buttonShadow.css")).toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}