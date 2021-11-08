package com.progiii.mailclientserver.client.controller;

import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;

public class ClientController {
    @FXML
    private RadioButton inboxRadio;
    @FXML
    private RadioButton sentRadio;
    @FXML
    private RadioButton draftsRadio;

    @FXML
    private ListView<String> emailListView;

    @FXML
    private void showInbox()
    {
      //TODO: fetch all mails and show them in the listView
        emailListView.getItems().add("inbox");
    }

    @FXML
    private void showSent()
    {
        //TODO: fetch all sent emails and show them in the listView
        emailListView.getItems().add("sent");
    }

    @FXML
    private void showDrafts()
    {
        //TODO: fetch all drafts mails and show them in the listView
        emailListView.getItems().add("drafts");
    }
}