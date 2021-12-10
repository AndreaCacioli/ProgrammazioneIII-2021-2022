package com.progiii.mailclientserver.client.view;

import com.progiii.mailclientserver.client.controller.ClientController;
import com.progiii.mailclientserver.client.controller.NewMsgController;
import com.progiii.mailclientserver.client.model.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class ClientView extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Client client = new Client(getParameters().getRaw().get(0), true);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 550);
        ClientController controller = fxmlLoader.getController();
        controller.setClient(client);

        FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("newMsgView.fxml"));
        Parent v = fxmlLoader1.load();
        NewMsgController controller1 = fxmlLoader1.getController();
        controller1.setClient(client);
        controller1.setClientController(controller);
        Scene scene1 = new Scene(v, 600, 400);
        Stage newStage = new Stage();

        stage.setOnCloseRequest((windowEvent -> controller.shutdownPeriodicEmailDownloader()));
        stage.setOnShown((event) -> controller.openConnectionToServer());
        newStage.setOnShown((event) -> controller1.bindEverything());
        newStage.setOnCloseRequest((windowEvent) -> controller1.onSendToDraftsButtonClicked(windowEvent));
        newStage.setScene(scene1);
        newStage.setTitle("New Email");


        controller.setStage(newStage);
        controller.setNewMsgController(controller1);

        stage.setTitle("Mail Sender - 9000");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(ClientView.class.getResource("appStyle.css")).toExternalForm());
        stage.show();

        controller.setStatusBiding();
        controller.setGravatarBindings();

    }

    public static void main(String[] args) {
        launch(args);
    }
}