package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


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
    private Label statusLabel;

    @FXML
    private Button restartConnButton;

    private Client client;
    private Stage newMessageStage;
    private Socket socket;
    private NewMsgController newMsgController;
    private ScheduledExecutorService scheduledExServConn;
    private ScheduledExecutorService scheduledExEmailDownloader;
    protected final ReentrantLock reentrantLock = new ReentrantLock();
    ObjectOutputStream objectOutputStream;

    public void setNewMsgController(NewMsgController newMsgController) {
        this.newMsgController = newMsgController;
    }

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

    public void setStatusBiding() {
        this.getStatusLabel().textProperty().bind(client.statusProperty());
    }

    public ImageView getAvatarView() {
        return avatarView;
    }

    public Label getAccountLabel() {
        return accountLabel;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }


    ///////////////////////////////
    //Methods that change the GUI//
    @FXML
    public void initialize() {

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


    ///////////////////////////////////////
    //Methods that send ACTIONS to Server//

    //Common method to send an action
    //This should be in critical section
    public void sendActionToServer(Action action) {
        try {
            objectOutputStream.writeObject(action);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This should be in critical section
    public void sendEmailToServer(SerializableEmail serializableEmail) {
        try {
            objectOutputStream.writeObject(serializableEmail);
            objectOutputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //This should be in critical section
    public ServerResponse waitForResponse() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ServerResponse response = (ServerResponse) objectInputStream.readObject();
            System.out.println("Response: " + response);
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ServerResponse.UNKNOWN_ERROR;
    }

    @FXML
    private void deleteSelectedEmail() {
        new Thread(() -> {
            synchronized (reentrantLock) {
                ServerResponse response = null;
                if (client.selectedEmail != null) {
                    try {
                        getNewSocket();
                        sendActionToServer(new Action(client, null, Operation.DELETE_EMAIL));
                        SerializableEmail emailToBeDeleted = new SerializableEmail(client.selectedEmail);
                        sendEmailToServer(emailToBeDeleted);
                        response = waitForResponse();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (response == ServerResponse.ACTION_COMPLETED) {
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
                    } else {
                        //TODO fai sapere all'utente che é stato impossibile eseguire la richiesta con un popup
                    }
                    closeConnectionToServer();
                }
            }
        }).start();
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

    private void loadAllFromServer() {
        new Thread(() -> {
            synchronized (reentrantLock) {
                try {
                    getNewSocket();
                    Action request = new Action(client, null, Operation.GET_ALL_EMAILS);
                    sendActionToServer(request);

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
                            case RECEIVED -> {
                                if (!client.contains(client.inboxProperty(), email))
                                    client.inboxProperty().add(email);
                            }
                            case SENT -> {
                                if (!client.contains(client.sentProperty(), email))
                                    client.sentProperty().add(email);
                            }
                            case DRAFTED -> {
                                if (!client.contains(client.draftsProperty(), email))
                                    client.draftsProperty().add(email);
                            }
                            case TRASHED -> {
                                if (!client.contains(client.trashProperty(), email))
                                    client.trashProperty().add(email);
                            }
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
            SimpleListProperty<Email> allEmails = new SimpleListProperty<>();
            allEmails.addAll(client.inboxProperty());
            allEmails.addAll(client.sentProperty());
            allEmails.addAll(client.draftsProperty());
            allEmails.addAll(client.trashProperty());
            for (Email email : allEmails) {
                if (email.getID() > Email.serial) Email.serial = email.getID();
            }
        }).start();
    }

    ///////////////////////////////////////
    //Auto and manual server reconnection//
    private void startPeriodicReqConnection() {
        if (scheduledExServConn != null) return;
        scheduledExServConn = Executors.newScheduledThreadPool(1);
        scheduledExServConn.scheduleAtFixedRate(new ConnectionReqTask(), 1, 20, TimeUnit.SECONDS);
    }

    class ConnectionReqTask implements Runnable {
        public ConnectionReqTask() {
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

    protected void getNewSocket() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), 6969);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    @FXML
    public void openConnectionToServer() {
        try {
            //Test to see if it throws an exception
            getNewSocket();
            sendActionToServer(new Action(client, null, Operation.PING));
            ServerResponse response = waitForResponse();
            closeConnectionToServer();
            if (response == ServerResponse.ACTION_COMPLETED) {
                startPeriodicEmailDownloader();
                System.out.println("Connection To Server Successes");
                client.statusProperty().setValue("Connected");
            }
        } catch (ConnectException connectException) {
            System.out.println("Connection To Server Failure");
            client.statusProperty().setValue("Try to connect to Server...");
            startPeriodicReqConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            restartConnButton.setVisible(!client.statusProperty().getValue().equals("Connected"));
            if (socket != null && scheduledExServConn != null) {
                shutdownPeriodicReqConnection();
            }
        }
    }

    protected void closeConnectionToServer() {
        try {
            socket.close();
            objectOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startPeriodicEmailDownloader() {
        if (scheduledExEmailDownloader != null) return;
        scheduledExEmailDownloader = Executors.newScheduledThreadPool(1);
        scheduledExEmailDownloader.scheduleAtFixedRate(new PeriodicEmailDownloader(), 0, 5, TimeUnit.SECONDS);
    }

    class PeriodicEmailDownloader implements Runnable {
        public PeriodicEmailDownloader() {
        }

        @Override
        public void run() {
            loadAllFromServer();
        }
    }

    public void shutdownPeriodicEmailDownloader() {
        scheduledExEmailDownloader.shutdown();
    }

}