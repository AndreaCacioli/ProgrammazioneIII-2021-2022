package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


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
    public void onSentButtonClicked(ActionEvent event) {

    }
}
