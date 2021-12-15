package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
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
    protected Stage newMessageStage;
    private Socket socket;
    private ScheduledExecutorService scheduledExEmailDownloader;
    protected final ReentrantLock reentrantLock = new ReentrantLock();
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

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
     * Binding of Gravatar Image
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

    /**
     * Binding of left-bottom Status label of GUI
     */
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
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.inboxProperty().size() > 0) client.selectedEmail = client.inboxProperty().get(0);
        emailListView.itemsProperty().bind(client.inboxProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showSent() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.sentProperty().size() > 0) client.selectedEmail = client.sentProperty().get(0);
        emailListView.itemsProperty().bind(client.sentProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showDrafts() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.draftsProperty().size() > 0) client.selectedEmail = client.draftsProperty().get(0);
        emailListView.itemsProperty().bind(client.draftsProperty());
        bindMailToView(client.selectedEmail);
    }

    @FXML
    private void showTrash() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
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
            client.newEmail = new Email(client.getLargestID() + 1);
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
        client.selectedEmail = new Email(client.getLargestID() + 1);
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
        if (objectOutputStream == null) return;
        try {
            objectOutputStream.writeObject(action);
            objectOutputStream.flush();
        } catch (Exception e) {
            System.out.println("Socket is closed");
        }
    }

    //This should be in critical section
    public ServerResponse waitForResponse() throws IOException, ClassNotFoundException {
        ServerResponse response = ServerResponse.UNKNOWN_ERROR;
        response = (ServerResponse) objectInputStream.readObject();
        System.out.println("Response: " + response);
        return response;
    }

    //This should be in critical section
    public void sendEmailToServer(SerializableEmail serializableEmail) {
        if (objectOutputStream == null) return;
        try {
            objectOutputStream.writeObject(serializableEmail);
            objectOutputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedEmail() {
        new Thread(() -> {
            synchronized (reentrantLock) {
                ServerResponse response = null;
                if (client.selectedEmail != null) {
                    try {
                        getNewSocket();
                        setSocketSuccess();
                        sendActionToServer(new Action(client, null, Operation.DELETE_EMAIL));
                        SerializableEmail emailToBeDeleted = new SerializableEmail(client.selectedEmail);
                        sendEmailToServer(emailToBeDeleted);
                        response = waitForResponse();
                    } catch (IOException socketException) {
                        setSocketFailure();
                    } catch (ClassNotFoundException e) {
                        System.out.println("Could not read from stream");
                    } finally {
                        closeConnectionToServer();
                    }

                    if (response == ServerResponse.ACTION_COMPLETED) {
                        resetSelectedEmail();
                        loadAllFromServer();
                    } else {
                        Platform.runLater(() -> {
                            Alert a = new Alert(Alert.AlertType.ERROR, "Something went wrong while deleting an email");
                            a.show();
                        });
                    }
                }
            }
        }).start();
    }

    @FXML
    protected void loadAllFromServer() {
        new Thread(() -> {
            synchronized (reentrantLock) {
                try {
                    getNewSocket();
                    setSocketSuccess();
                } catch (IOException e) {
                    setSocketFailure();
                    return;
                }
                Action request = new Action(client, null, Operation.GET_ALL_EMAILS);
                sendActionToServer(request);
                ServerResponse response = null;
                try {
                    response = waitForResponse();
                } catch (Exception ex) {
                    System.out.println("no response in loadAllFromServer");
                }


                if (response == ServerResponse.ACTION_COMPLETED) {
                    //TODO only add the new ones and not empty all emails
                    client.emptySelf();
                } else return;

                try {
                    SerializableEmail serializableEmail = null;
                    while ((serializableEmail = (SerializableEmail) objectInputStream.readObject()) != null) {
                        Email email = new Email(serializableEmail);
                        switch (email.getState()) {
                            case RECEIVED -> {
                                if (!client.contains(client.inboxProperty(), email))
                                    Platform.runLater(() -> {
                                        client.inboxProperty().add(email);
                                    });
                            }
                            case SENT -> {
                                if (!client.contains(client.sentProperty(), email))
                                    Platform.runLater(() -> {
                                        client.sentProperty().add(email);
                                    });
                            }
                            case DRAFTED -> {
                                if (!client.contains(client.draftsProperty(), email))
                                    Platform.runLater(() -> {
                                        client.draftsProperty().add(email);
                                    });
                            }
                            case TRASHED -> {
                                if (!client.contains(client.trashProperty(), email))
                                    Platform.runLater(() -> {
                                        client.trashProperty().add(email);
                                    });
                            }
                        }
                    }
                } catch (EOFException EOFException) {
                    System.out.println("Finished getting emails");
                } catch (ClassNotFoundException e) {
                    System.out.println("Could not read from stream");
                } catch (IOException ioException) {
                    System.out.println("Error reading Emails in loadAllFromServer");
                } finally {
                    closeConnectionToServer();
                }
            }
        }).start();
    }

    ///////////////////////////////////////
    //Auto and manual server reconnection//
    protected void getNewSocket() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), 6969);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    protected void setSocketSuccess() {
        System.out.println("Connection To Server Successes");
        Platform.runLater(() -> {
            client.statusProperty().setValue("Connected");
            restartConnButton.setVisible(false);
        });
    }

    protected void setSocketFailure() {
        System.out.println("Connection To Server Failure");
        Platform.runLater(() -> {
            client.statusProperty().setValue("Trying to connect to Server...");
            restartConnButton.setVisible(true);
        });
    }

    protected void closeConnectionToServer() {
        if (socket != null && objectInputStream != null && objectOutputStream != null) {
            try {
                socket.close();
                objectOutputStream.close();
                objectInputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //////////////////////////////////////////
    //Auto download of every Email on server//
    public void startPeriodicEmailDownloader() {
        if (scheduledExEmailDownloader != null) return;
        scheduledExEmailDownloader = Executors.newScheduledThreadPool(1);
        scheduledExEmailDownloader.scheduleAtFixedRate(new PeriodicEmailDownloader(), 0, 30, TimeUnit.SECONDS);
    }

    public void shutdownPeriodicEmailDownloader() {
        if (scheduledExEmailDownloader != null)
            scheduledExEmailDownloader.shutdown();
    }

    class PeriodicEmailDownloader implements Runnable {
        public PeriodicEmailDownloader() {
        }

        @Override
        public void run() {
            loadAllFromServer();
        }
    }
}