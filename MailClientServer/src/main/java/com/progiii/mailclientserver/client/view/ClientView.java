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


    /**
     * The main method called when the application starts
     * Initializes the model only once,
     * and then passes it to the two controllers
     * that are created by javaFX when the xml file is parsed.
     * This method wants the EMAIL ADDRESS of the client as a STRING ARGUMENT OF THE PROGRAM
     * Based on the email address passed, we use Gravatar's API
     * to get a computer generated image to use as a profile picture.
     *
     * Please note that this application on itself is of very little interest if not paired to the server.
     * (See ServerView.java)
     * */
    @Override
    public void start(Stage stage) throws IOException {

        Client client = new Client(getParameters().getRaw().get(0), true);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 550);
        ClientController controller = fxmlLoader.getController();
        controller.setClient(client);
        controller.setStatusBiding();
        controller.setGravatarBindings();

        FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("newMsgView.fxml"));
        Parent v = fxmlLoader1.load();
        NewMsgController controller1 = fxmlLoader1.getController();
        controller1.setClient(client);
        controller1.setClientController(controller);
        Scene scene1 = new Scene(v, 600, 400);
        Stage newStage = new Stage();


        newStage.setOnShown((event) -> controller1.bindEverything());
        newStage.setScene(scene1);
        scene1.getStylesheets().add(Objects.requireNonNull(ClientView.class.getResource("appStyle.css")).toExternalForm());
        newStage.setTitle("New Email");
        controller.setStage(newStage);

        stage.setOnShown((event) -> controller.startPeriodicEmailDownloader());
        stage.setOnCloseRequest((windowEvent -> controller.shutdownPeriodicEmailDownloader()));
        stage.setTitle("Mail Sender - 9000");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(ClientView.class.getResource("appStyle.css")).toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}