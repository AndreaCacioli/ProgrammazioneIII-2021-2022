package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class ClientController {

    @FXML
    private ListView<Email> emailListView;
    @FXML
    private TextArea selectedEmailView;
    @FXML
    private TextField fromTextField;
    @FXML
    private TextField toTextField;
    @FXML
    private TextField subjectTextField;

    Client client;

    @FXML
    public void initialize() {
        if (this.client != null)
            throw new IllegalStateException("Model can only be initialized once");
        client = new Client();
    }

    @FXML
    private void showInbox() {
        //TODO: fetch all mails and show them in the listView
        if(client.inboxProperty().size() > 0) client.selectedEmail = client.inboxProperty().get(0);
        emailListView.itemsProperty().bind(client.inboxProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showSent() {
        if(client.sentProperty().size() > 0) client.selectedEmail = client.sentProperty().get(0);
        //TODO: fetch all sent emails and show them in the listView
        emailListView.itemsProperty().bind(client.sentProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showDrafts() {
        if(client.draftsProperty().size() > 0) client.selectedEmail = client.draftsProperty().get(0);
        //TODO: fetch all drafts mails and show them in the listView
        emailListView.itemsProperty().bind(client.draftsProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showTrash() {
        if(client.trashProperty().size() > 0) client.selectedEmail = client.trashProperty().get(0);
        //TODO: fetch all trashed mails and show them in the listView
        emailListView.itemsProperty().bind(client.trashProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void onListViewClick() {
        Email email = emailListView.getSelectionModel().getSelectedItems().get(0);
        client.selectedEmail = email;
        bindMailToView(email);
    }

    private void bindMailToView(Email email) {
        fromTextField.textProperty().bind(email.senderProperty());
        toTextField.textProperty().bind(email.receiverProperty());
        subjectTextField.textProperty().bind(email.subjectProperty());
        selectedEmailView.textProperty().bind(email.bodyProperty());
    }


    @FXML
    private void deleteSelectedEmail() {
        if (client.selectedEmail != null) {
            switch (client.selectedEmail.state) {
                case DRAFTED -> {
                    int index = sendSelectedEmailToTrash(client.draftsProperty());
                    showNextEmail(index);
                }

                case SENT -> {
                    int index = sendSelectedEmailToTrash(client.sentProperty());
                    showNextEmail(index);
                }

                case RECEIVED -> {
                    int index = sendSelectedEmailToTrash(client.inboxProperty());
                    showNextEmail(index);
                }

                case TRASHED -> {
                    int index = sendSelectedEmailToTrash(client.trashProperty());
                    showNextEmail(index);
                }

            }

        }
    }

    private int sendSelectedEmailToTrash(ObservableList<Email> list)
    {
        int ret = list.indexOf(client.selectedEmail);

        if(client.selectedEmail.state != EmailState.TRASHED)
        {
            client.selectedEmail.state = EmailState.TRASHED;
            client.trashProperty().add(client.selectedEmail);
        }
        list.remove(client.selectedEmail);
        resetSelectedEmail();

        return ret;
    }

    private void showNextEmail(int indexOfEmailToBeShown)
    {
        if (indexOfEmailToBeShown > 0 && indexOfEmailToBeShown < emailListView.getItems().size())
        {
            emailListView.getSelectionModel().select(indexOfEmailToBeShown - 1);
            client.selectedEmail = emailListView.getItems().get(indexOfEmailToBeShown - 1);
            bindMailToView(client.selectedEmail);
        }
    }

    private void resetSelectedEmail() {
        fromTextField.textProperty().unbind();
        fromTextField.setText("");
        toTextField.textProperty().unbind();
        toTextField.setText("");
        subjectTextField.textProperty().unbind();
        subjectTextField.setText("");
        selectedEmailView.textProperty().unbind();
        selectedEmailView.setText("");

    }

}