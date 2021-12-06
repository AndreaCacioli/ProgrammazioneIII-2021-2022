package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class NewMsgController {

    @FXML
    private TextField textFieldTo;

    @FXML
    private TextField textFieldSubject;

    @FXML
    private TextArea textAreaMsg;


    Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void bindEverything() {
        textFieldTo.textProperty().bindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().bindBidirectional(client.newEmail.subjectProperty());
        textAreaMsg.textProperty().bindBidirectional(client.newEmail.bodyProperty());
    }

    @FXML
    public void onSendButtonClicked(ActionEvent event) {
        try
        {
            Socket socket = new Socket(InetAddress.getLocalHost(),6969);
            ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
            stream.writeObject(new Action(client , client.newEmail.getReceiver(), Operation.SEND_EMAIL));
            stream.writeObject(new String(client.newEmail.getSubject()));
            stream.writeObject(new String(client.newEmail.getBody()));
            stream.writeObject(client.newEmail.getDateTime());
            stream.flush();
        }catch (Exception ex) {ex.printStackTrace();}
    }

    @FXML
    public void onSendToDraftsButtonClicked(Event event) {
        if (!client.draftsProperty().contains(client.newEmail)) {
            client.draftsProperty().add(client.newEmail);
        }

        textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
        textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

        client.newEmail = new Email();

        if (event instanceof MouseEvent) {
            //close the window
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
        client.saveAll();
    }

}
