package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;


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
    @FXML
    private TextArea selectedEmailView;

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

    //TODO find out why this button causes the others to fail when deleting an email
    @FXML
    private void showTrash() {
        //TODO: fetch all trashed mails and show them in the listView
        Bindings.bindContent(emailListView.getItems(), client.trash);
    }

    @FXML
    private void showSelectedEmail()
    {
        Email email = emailListView.getSelectionModel().getSelectedItems().get(0);
        client.selectedEmail = email;
        System.out.println("showing new email! " + client.selectedEmail);
        selectedEmailView.textProperty().bind(email.bodyProperty());
    }

    @FXML
    private void deleteSelectedEmail()
    {
        if(client.selectedEmail != null)
        {
            switch (client.selectedEmail.state)
            {
                case DRAFTED -> {
                    client.drafts.remove(client.selectedEmail);
                    client.trash.add(client.selectedEmail);
                    selectedEmailView.textProperty().unbind();
                    selectedEmailView.setText("");
                    client.selectedEmail = null;
                }

                case SENT -> {
                    client.sent.remove(client.selectedEmail);
                    client.trash.add(client.selectedEmail);
                    selectedEmailView.textProperty().unbind();
                    selectedEmailView.setText("");
                    client.selectedEmail = null;
                }
                case RECEIVED -> {
                    client.inbox.remove(client.selectedEmail);
                    client.trash.add(client.selectedEmail);
                    selectedEmailView.textProperty().unbind();
                    selectedEmailView.setText("");
                    client.selectedEmail = null;
                }
                //TODO add trashed section
            }

        }
    }

}