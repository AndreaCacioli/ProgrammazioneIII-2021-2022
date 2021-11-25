package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class NewMsgController {

    @FXML
    private TextField textFieldA;

    @FXML
    private TextField textFieldCc;

    @FXML
    private TextField textFieldOggetto;

    @FXML
    private TextArea textAreaMsg;


    Client client;

    public void setClient(Client client) {this.client = client;}
    public Client getClient() {return client;}

    //TODO mettere in inglese la grafica


    public void bindEverything()
    {
        //TODO rimuovere cc dalla grafica
        textAreaMsg.textProperty().bindBidirectional(client.newEmail.bodyProperty());
        textFieldOggetto.textProperty().bindBidirectional(client.newEmail.subjectProperty());
        textFieldA.textProperty().bindBidirectional(client.newEmail.receiverProperty());
    }

    @FXML
    public void onSentButtonClicked(ActionEvent event)
    {

    }
}
