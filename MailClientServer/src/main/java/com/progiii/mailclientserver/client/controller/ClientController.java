package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

    @FXML
    private Button restartConnButton;

    private Client client;
    private Stage newMessageStage;
    private Socket socket;
    private NewMsgController newMsgController;
    private ScheduledExecutorService scheduledExServBackup;
    private ScheduledExecutorService scheduledExServConn;


    public void setStage(Stage newMessageStage) {
        this.newMessageStage = newMessageStage;
    }

    @SuppressWarnings("all")
    public Client getClient() {
        return client;
    }

    @SuppressWarnings("all")
    public void setClient(Client client) {
        if (this.client != null)
            throw new IllegalStateException("Client can only be initialized once");
        this.client = client;
    }

    /**
     * Binding elements that do not change during the life of the app
     */
    public void setGravatarBindings() {
        try {
            Socket socket = new Socket("www.google.com", 80);
            if (socket.isConnected()) {
                this.getAvatarView().imageProperty().bind(client.imageProperty());
                this.getAccountLabel().textProperty().bind(client.addressProperty());
                System.out.println("OK Internet Connection");
            } else System.out.println("NO Internet Connection");
            socket.close();
        } catch (Exception ex) {
            System.out.println("---NO Internet Connection---");
        }

    }

    public ImageView getAvatarView() {
        return avatarView;
    }

    public Label getAccountLabel() {
        return accountLabel;
    }

    @FXML
    public void initialize() {
        startPeriodicBackup();
    }

    @FXML
    private void showInbox() {
        //TODO: fetch all mails and show them in the listView
        client.selectedEmail = new Email();
        if (client.inboxProperty().size() > 0) client.selectedEmail = client.inboxProperty().get(0);
        emailListView.itemsProperty().bind(client.inboxProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showSent() {
        //TODO: fetch all sent emails and show them in the listView
        client.selectedEmail = new Email();
        if (client.sentProperty().size() > 0) client.selectedEmail = client.sentProperty().get(0);
        emailListView.itemsProperty().bind(client.sentProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showDrafts() {
        //TODO: fetch all drafts mails and show them in the listView
        client.selectedEmail = new Email();
        if (client.draftsProperty().size() > 0) client.selectedEmail = client.draftsProperty().get(0);
        emailListView.itemsProperty().bind(client.draftsProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showTrash() {
        //TODO: fetch all trashed mails and show them in the listView
        client.selectedEmail = new Email();
        if (client.trashProperty().size() > 0) client.selectedEmail = client.trashProperty().get(0);
        emailListView.itemsProperty().bind(client.trashProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void onListViewClick(MouseEvent event) {

        if (emailListView.getSelectionModel().getSelectedItems().size() <= 0) {
            return;
        }

        Email email = emailListView.getSelectionModel().getSelectedItems().get(0);
        client.selectedEmail = email;
        bindMailToView(email);

        if (event.getClickCount() == 2) {
            if (client.selectedEmail.getState() == EmailState.DRAFTED) {
                client.newEmail = client.selectedEmail;
                try {
                    newMessageStage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @FXML
    private void deleteSelectedEmail() {
        if (client.selectedEmail != null) {
            try {
                newMsgController.sendActionToServer(Operation.DELETE_EMAIL);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
            client.saveAll();
        }
    }

    @FXML
    private void onNewMailClicked() {
        try {
            client.newEmail = new Email();
            client.newEmail.setSender(client.addressProperty().get());
            newMessageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void bindMailToView(Email email) {
        fromTextField.textProperty().bind(email.senderProperty());
        toTextField.textProperty().bind(email.receiverProperty());
        subjectTextField.textProperty().bind(email.subjectProperty());
        selectedEmailView.textProperty().bind(email.bodyProperty());
    }

    private int sendSelectedEmailToTrash(ObservableList<Email> list) {
        int ret = list.indexOf(client.selectedEmail);

        if (client.selectedEmail.getState() != EmailState.TRASHED) {
            client.selectedEmail.setState(EmailState.TRASHED);
            client.trashProperty().add(client.selectedEmail);
        }
        list.remove(client.selectedEmail);
        resetSelectedEmail();

        return ret;
    }

    private void showNextEmail(int indexOfEmailToBeShown) {
        if (indexOfEmailToBeShown == 0 && emailListView.getItems().size() == 0) return;

        if (indexOfEmailToBeShown == 0) {
            emailListView.getSelectionModel().select(indexOfEmailToBeShown);
            client.selectedEmail = emailListView.getItems().get(indexOfEmailToBeShown);
            bindMailToView(client.selectedEmail);
        }
        if (indexOfEmailToBeShown > 0 && indexOfEmailToBeShown - 1 < emailListView.getItems().size()) {
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

    public void openConnectionToServer() {
        try {
            restartConnButton.setDisable(true);
            socket = new Socket(InetAddress.getLocalHost(), 6969);
            System.out.println("Connection To Server Successes");
            loadAllFromServer();
        } catch (ConnectException connectException) {
            System.out.println("Connection To Server Failure");
            restartConnButton.setDisable(false);
            startPeriodicReqConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (socket != null && scheduledExServConn != null) {
                shutdownPeriodicReqConnection();
            }
        }
    }

    private void closeConnectionToServer() {
        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAllFromServer() {
        try {
            Action request = new Action(client, null, Operation.GET_ALL_EMAILS);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(request);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            SerializableEmail serializableEmail;
            Object object;
            while ((object = objectInputStream.readObject()) != null) {
                if (object instanceof ServerResponse) {
                    if (object != ServerResponse.ACTION_COMPLETED) {
                        if (object == ServerResponse.CLIENT_NOT_FOUND) {
                            System.out.println("I've been added to the server");
                        } else {
                            System.out.println("Something went wrong" + object);
                        }
                        return;
                    }
                    break;
                }
                serializableEmail = (SerializableEmail) object;
                Email email = new Email(serializableEmail);
                switch (email.getState()) {
                    case RECEIVED -> client.inboxProperty().add(email);
                    case SENT -> client.sentProperty().add(email);
                    case DRAFTED -> client.draftsProperty().add(email);
                    case TRASHED -> client.trashProperty().add(email);
                }
            }

        } catch (EOFException EOFException) {
            System.out.println("Finished getting emails");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnectionToServer();
        }
    }

    private void startPeriodicBackup() {
        if (scheduledExServBackup != null) return;
        scheduledExServBackup = Executors.newScheduledThreadPool(1);
        scheduledExServBackup.scheduleAtFixedRate(new backupTask(), 1, 5, TimeUnit.MINUTES);
    }

    class backupTask implements Runnable {
        public backupTask() {
        }

        @Override
        public void run() {
            client.saveAll();
            System.out.println("Backup!");
        }
    }

    public void shutdownPeriodicBackup() {
        scheduledExServBackup.shutdown();
    }

    private void startPeriodicReqConnection() {
        if (scheduledExServConn != null) return;
        scheduledExServConn = Executors.newScheduledThreadPool(1);
        scheduledExServConn.scheduleAtFixedRate(new connectionReqTask(), 1, 20, TimeUnit.SECONDS);
    }

    class connectionReqTask implements Runnable {
        public connectionReqTask() {
        }

        @Override
        public void run() {
            System.out.println("Try To Connect To Server...");
            openConnectionToServer();
        }
    }

    public void shutdownPeriodicReqConnection() {
        scheduledExServConn.shutdown();
    }

}