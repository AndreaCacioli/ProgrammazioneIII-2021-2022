package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    @FXML
    private Button sendNewMsgButton;

    @FXML
    private Button draftsNewMsgButton;


    private Client client;

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

    public void sendActionToServer(Operation operation) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), 6969);
            try {
                ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());

                stream.writeObject(new Action(client, client.newEmail.getReceiver(), operation));
                SerializableEmail serializableEmail = new SerializableEmail(client.newEmail.getSender(), client.newEmail.getReceiver(), client.newEmail.getSubject(), client.newEmail.getBody(), client.newEmail.getState(), client.newEmail.getDateTime());
                stream.writeObject(serializableEmail);
                stream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO verifica che funzioni perfettamente
    @FXML
    public void onSendButtonClicked(ActionEvent event) {

        textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
        textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();

        try {
            sendActionToServer(Operation.SEND_EMAIL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        client.saveAll();
        client.newEmail = new Email();
    }

    @FXML
    public void onSendToDraftsButtonClicked(Event event) {
        if (client.draftsProperty().contains(client.newEmail)) {
            return;
        }
        client.draftsProperty().add(client.newEmail);

        textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
        textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());
        if (event instanceof ActionEvent) {
            Stage stage = (Stage) draftsNewMsgButton.getScene().getWindow();
            stage.close();
        }

        try {
            sendActionToServer(Operation.NEW_DRAFT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        client.saveAll();
        client.newEmail = new Email();
    }
}
