package com.progiii.mailclientserver.client.controller;

import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;


public class ClientController {
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
    private ListView<String> emailListView;

    @FXML
    private void showInbox() {
        //TODO: fetch all mails and show them in the listView
        emailListView.getItems().add("inbox");
    }

    @FXML
    private void showSent() {
        //TODO: fetch all sent emails and show them in the listView
        emailListView.getItems().add("sent");
    }

    @FXML
    private void showDrafts() {
        //TODO: fetch all drafts mails and show them in the listView
        emailListView.getItems().add("drafts");
    }
}