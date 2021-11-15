package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;


public class ClientController
{
    @FXML
    private Button inboxButtonId;
    @FXML
    private Button sentButtonId;
    @FXML
    private Button draftsButtonId;
    @FXML
    private Button forwardButtonId;
    @FXML
    private Button deleteButtonId;

    @FXML
    private ListView<Email> emailListView;

    Client client;

    @FXML
    public void initialize()
    {
        client = new Client();
    }


    @FXML
    private void showInbox() {
        //TODO: fetch all mails and show them in the listView
        Bindings.bindContent(emailListView.getItems(), client.inbox);
    }

    @FXML
    private void showSent() {
        //TODO: fetch all sent emails and show them in the listView
        Bindings.bindContent(emailListView.getItems(), client.sent);
    }

    @FXML
    private void showDrafts() {
        //TODO: fetch all drafts mails and show them in the listView
        Bindings.bindContent(emailListView.getItems(), client.drafts);
    }
}