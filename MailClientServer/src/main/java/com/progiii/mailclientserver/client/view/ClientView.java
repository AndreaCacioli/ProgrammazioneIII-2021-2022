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

        System.out.println(getParameters().getRaw().get(0));
        Client client = new Client(getParameters().getRaw().get(0));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 550);
        ClientController controller = fxmlLoader.getController();
        controller.setClient(client);

        stage.setOnCloseRequest((event) -> {
            controller.shutdownThread();
        });

        FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("newMsgView.fxml"));
        Parent v = fxmlLoader1.load();
        NewMsgController controller1 = fxmlLoader1.getController();
        controller1.setClient(client);
        Scene scene1 = new Scene(v, 600, 400);

        Stage newStage = new Stage();
        newStage.setOnShown((event) -> {
            controller1.bindEverything();
        });
        newStage.setOnCloseRequest((event) -> {
            controller1.onSendToDraftsButtonClicked(event);
        });
        newStage.setScene(scene1);
        newStage.setTitle("New Email");


        controller.setStage(newStage);

        stage.setTitle("Mail Sender - 9000");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(ClientView.class.getResource("appStyle.css")).toExternalForm());
        stage.show();

        //Binding elemnts that do not change during the life of the app
        controller.getAvatarView().imageProperty().bind(client.imageProperty());
        controller.getAccountLabel().textProperty().bind(client.addressProperty());
    }

    public static void main(String[] args) {
        launch(args);
    }
}