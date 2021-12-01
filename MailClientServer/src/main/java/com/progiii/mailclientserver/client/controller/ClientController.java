package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;


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
    @FXML
    private ImageView avatarView;
    @FXML
    private Label accountLabel;



    Client client;
    Stage newMessageStage;

    public void setStage(Stage newMessageStage) {this.newMessageStage = newMessageStage;}
    @SuppressWarnings("all")
    public Client getClient(){return client;}
    @SuppressWarnings("all")
    public void setClient(Client client) {this.client = client;}
    public ImageView getAvatarView() {return avatarView;}
    public Label getAccountLabel() {return accountLabel;}


    @FXML
    public void initialize() {

        if (this.client != null)
            throw new IllegalStateException("Model can only be initialized once");
    }

    @FXML
    private void showInbox() {
        client.selectedEmail = new Email();
        //TODO: fetch all mails and show them in the listView
        if(client.inboxProperty().size() > 0) client.selectedEmail = client.inboxProperty().get(0);
        emailListView.itemsProperty().bind(client.inboxProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showSent() {
        client.selectedEmail = new Email();
        if(client.sentProperty().size() > 0) client.selectedEmail = client.sentProperty().get(0);
        //TODO: fetch all sent emails and show them in the listView
        emailListView.itemsProperty().bind(client.sentProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showDrafts() {
        client.selectedEmail = new Email();
        if(client.draftsProperty().size() > 0) client.selectedEmail = client.draftsProperty().get(0);
        //TODO: fetch all drafts mails and show them in the listView
        emailListView.itemsProperty().bind(client.draftsProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showTrash() {
        client.selectedEmail = new Email();
        if(client.trashProperty().size() > 0) client.selectedEmail = client.trashProperty().get(0);
        //TODO: fetch all trashed mails and show them in the listView
        emailListView.itemsProperty().bind(client.trashProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void onListViewClick(MouseEvent event) {

        Email email = emailListView.getSelectionModel().getSelectedItems().get(0);
        client.selectedEmail = email;
        bindMailToView(email);

        if (event.getClickCount() == 2)
        {
            if(client.selectedEmail.getState() == EmailState.DRAFTED)
            {
                client.newEmail = client.selectedEmail;
                try{
                    newMessageStage.show();
                }catch (Exception e) {e.printStackTrace();}
            }
        }

    }

    @FXML
    private void deleteSelectedEmail() {
        if (client.selectedEmail != null) {
            switch (client.selectedEmail.getState()) {
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

    @FXML
    private void onNewMailClicked()
    {
        try{
            client.newEmail = new Email();
            newMessageStage.show();
        }catch (Exception e) {e.printStackTrace();}

    }

    private void bindMailToView(Email email) {
        fromTextField.textProperty().bind(email.senderProperty());
        toTextField.textProperty().bind(email.receiverProperty());
        subjectTextField.textProperty().bind(email.subjectProperty());
        selectedEmailView.textProperty().bind(email.bodyProperty());
    }

    private int sendSelectedEmailToTrash(ObservableList<Email> list)
    {
        int ret = list.indexOf(client.selectedEmail);

        if(client.selectedEmail.getState() != EmailState.TRASHED)
        {
            client.selectedEmail.setState(EmailState.TRASHED);
            client.trashProperty().add(client.selectedEmail);
        }
        list.remove(client.selectedEmail);
        resetSelectedEmail();

        return ret;
    }

    private void showNextEmail(int indexOfEmailToBeShown)
    {
        if(indexOfEmailToBeShown == 0 &&  emailListView.getItems().size() == 0) return;

        if(indexOfEmailToBeShown == 0)
        {
            emailListView.getSelectionModel().select(indexOfEmailToBeShown);
            client.selectedEmail = emailListView.getItems().get(indexOfEmailToBeShown);
            bindMailToView(client.selectedEmail);
        }
        if (indexOfEmailToBeShown > 0 && indexOfEmailToBeShown - 1 < emailListView.getItems().size())
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

    public void saveAll()
    {
        int i = 0;
        String[] names = {"inbox", "sent", "drafts", "trashed"};
        SimpleListProperty<Email>[] lists = new SimpleListProperty[]{client.inboxProperty(),client.sentProperty(),client.draftsProperty(),client.trashProperty()};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (SimpleListProperty<Email> list : lists)
        {
            JSONArray array = new JSONArray();
            JSONObject emailDetails = new JSONObject();
            for(Email email : list)
            {
                emailDetails.put("sender", email.getSender());
                emailDetails.put("receiver", email.getReceiver());
                emailDetails.put("subject", email.getSubject());
                emailDetails.put("body", email.getBody());
                emailDetails.put("dateTime", email.getDateTime().format(formatter));
            }
            JSONObject emailList = new JSONObject();
            emailList.put("email", emailDetails);
            array.add(emailList);
            try {
                FileWriter fileWriter = new FileWriter("./MailClientServer/src/main/resources/com/progiii/mailclientserver/client/data/" + names[i]  + ".json");
                BufferedWriter out = new BufferedWriter(fileWriter);
                fileWriter.flush();
                out.write(array.toJSONString());
                out.flush();
                fileWriter.flush();
                out.close();
                fileWriter.close();
            }catch (Exception e){e.printStackTrace();}
            i++;
        }
    }



}