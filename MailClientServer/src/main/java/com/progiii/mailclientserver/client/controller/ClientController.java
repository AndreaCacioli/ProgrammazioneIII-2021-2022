package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.client.model.EmailState;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private Label timeLabel;

    @FXML
    private Button restartConnButton;

    private Client client;
    protected Stage newMessageStage;
    private Socket socket;
    private ScheduledExecutorService scheduledExEmailDownloader;
    protected final ReentrantLock reentrantLock = new ReentrantLock();
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    /**
     * method used to assign a Stage to this.newMessageStage
     *
     * @param newMessageStage newMSG window
     */
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
     * Binding Gravatar Image,
     * first of all check if there is internet connection
     * if true -> bind the image
     * otherwise set as Client's image a Local one
     * and finally bind the address
     */
    public void setGravatarBindings() {
        try {
            Socket socket = new Socket("www.google.com", 80);
            if (socket.isConnected()) {
                this.getAvatarView().imageProperty().bind(client.imageProperty());
                System.out.println("OK Internet Connection");
            } else System.out.println("NO Internet Connection");
            socket.close();
        } catch (Exception ex) {
            System.out.println("---NO Internet Connection---");
        } finally {
            this.getAccountLabel().textProperty().bind(client.addressProperty());
        }
    }

    /**
     * Binding of left-bottom Status label of GUI
     * which allow seeing if Client is connected to Server
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

    /**
     * called when Controller is in initialize phase
     * setting to the view a clock using to see the current time
     */
    @FXML
    public void initialize() {
        /*ask*/
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            timeLabel.setText(currentTime.getHour() + ":" + currentTime.getMinute() + "\t");
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * method call when we click on Inbox Section
     * if Inbox not empty we select the first Email
     * otherwise the selectedEmail is a new one(
     * used to bind an empty Email to the view).
     * We bind the ListView to the Client inboxProperty, and finally we bind the selected Email to the View
     */
    @FXML
    private void showInbox() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.inboxProperty().size() > 0) client.selectedEmail = client.inboxProperty().get(0);
        emailListView.itemsProperty().bind(client.inboxProperty());
        bindMailToView(client.selectedEmail);
    }

    /**
     * method call when we click on Sent Section
     * if Sent not empty we select the first Email
     * otherwise the selectedEmail is a new one(
     * used to bind an empty Email to the view).
     * We bind the ListView to the Client sentProperty, and finally we bind the selected Email to the View
     */
    @FXML
    private void showSent() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.sentProperty().size() > 0) client.selectedEmail = client.sentProperty().get(0);
        emailListView.itemsProperty().bind(client.sentProperty());
        bindMailToView(client.selectedEmail);
    }

    /**
     * method call when we click on Drafts Section
     * if Drafts not empty we select the first Email
     * otherwise the selectedEmail is a new one(
     * used to bind an empty Email to the view).
     * We bind the ListView to the Client draftsProperty, and finally we bind the selected Email to the View
     */
    @FXML
    private void showDrafts() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.draftsProperty().size() > 0) client.selectedEmail = client.draftsProperty().get(0);
        emailListView.itemsProperty().bind(client.draftsProperty());
        bindMailToView(client.selectedEmail);
    }

    /**
     * method call when we click on Trash Section
     * if Trash not empty we select the first Email
     * otherwise the selectedEmail is a new one(
     * used to bind an empty Email to the view).
     * We bind the ListView to the Client trashProperty, and finally we bind the selected Email to the View
     */
    @FXML
    private void showTrash() {
        client.selectedEmail = new Email(client.getLargestID() + 1);
        if (client.trashProperty().size() > 0) client.selectedEmail = client.trashProperty().get(0);
        emailListView.itemsProperty().bind(client.trashProperty());
        bindMailToView(client.selectedEmail);
    }

    /**
     * method called when we click on ListView,
     * if ListView is empty return
     * otherwise we select the first Email and bind this one to the view.
     * <p>
     * After, we check if Email is already been read, if the answer is false
     * we create a Thread that contact the Server, send Action, send the Email and
     * wait for Response to set true state of read variable.
     * <p>
     * Finally, if we click two times on an Email and this one is in Drafts Section
     * we one a newMessageStage to allow Client to complete the draft
     *
     * @param event that call onListViewClick
     */
    @FXML
    private void onListViewClick(MouseEvent event) {

        if (emailListView.getSelectionModel().getSelectedItems().size() <= 0) {
            return;
        }

        Email email = emailListView.getSelectionModel().getSelectedItems().get(0);
        client.selectedEmail = email;
        bindMailToView(email);


        if (!email.isRead()) {
            new Thread(() -> {
                try {
                    getNewSocket();
                    setSocketSuccess();
                    sendActionToServer(new Action(client, null, Operation.READ_EMAIL));
                    sendEmailToServer(new SerializableEmail(client.selectedEmail));
                    ServerResponse response = waitForResponse();
                    if (response == ServerResponse.ACTION_COMPLETED) {
                        email.setRead(true);
                    }
                } catch (IOException exception) {
                    setSocketFailure();
                } catch (ClassNotFoundException e) {
                    System.out.println("Could not read from stream");
                } finally {
                    closeConnectionToServer();
                }
            }).start();
        }


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

    /**
     * method called when Client click on new Email button,
     * newEmail is set to a new Email with getLargestID+1,
     * newEmail sender is set to the Client's address,
     * and finally we show the new message stage
     */
    @FXML
    private void onNewMailButtonClicked() {
        try {
            client.newEmail = new Email(client.getLargestID() + 1);
            client.newEmail.setSender(client.addressProperty().get());
            newMessageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fist of all we check that selectedEmail is unlike null
     * if true -> set newEmail to a new one and setting all others fields
     * finally we show the new message stage to allow Client to write the receiver
     */
    @FXML
    protected void onForwardButtonClicked() {
        if (client.selectedEmail != null) {
            client.newEmail = new Email(client.getLargestID() + 1);
            client.newEmail.setSender(client.addressProperty().get());
            client.newEmail.setSubject(client.selectedEmail.getSubject());
            client.newEmail.setBody(client.selectedEmail.getBody());
            newMessageStage.show();
        }
    }

    /**
     * fist of all we check that selectedEmail is unlike null
     * if true -> set newEmail to a new one and setting all others fields
     * finally we show the new message stage to allow Client to write the new message body
     */
    @FXML
    protected void onReplyButtonClicked() {
        if (client.selectedEmail != null) {
            client.newEmail = new Email(client.getLargestID() + 1);
            client.newEmail.setSender(client.addressProperty().get());
            client.newEmail.setReceiver(client.selectedEmail.getSender());
            client.newEmail.setSubject("Re: " + client.selectedEmail.getSubject());
            newMessageStage.show();
        }
    }

    /**
     * this method is like onReplyButtonClicked, but this one allow
     * Client to answer to an Email with multiple receiver
     */
    @FXML
    protected void onReplyAllButtonClicked() {
        if (client.selectedEmail != null) {
            client.newEmail = new Email(client.getLargestID() + 1);
            client.newEmail.setSender(client.addressProperty().get());
            String[] receivers = client.selectedEmail.getReceiver().split(",");
            String parameter = "";
            for (int i = 0; i < receivers.length; i++) {
                String receiver = receivers[i].strip();
                if (client.getAddress().equals(receiver))
                    parameter += client.selectedEmail.getSender();
                else
                    parameter += receiver;
                if (i != receivers.length - 1)
                    parameter += ",";
            }
            client.newEmail.setReceiver(parameter);
            client.newEmail.setSubject("Re: " + client.selectedEmail.getSubject());
            newMessageStage.show();
        }
    }

    /**
     * method used to bind the Email passed by param to view
     *
     * @param email will bind to the Client's view
     */
    private void bindMailToView(Email email) {
        fromTextField.textProperty().bind(email.senderProperty());
        toTextField.textProperty().bind(email.receiverProperty());
        subjectTextField.textProperty().bind(email.subjectProperty());
        selectedEmailView.textProperty().bind(email.bodyProperty());
    }

    /*ARRIVATO*/
    private void showNextEmail(int indexOfDeletedEmail) {
        if (emailListView.getItems().size() == 1) {
            resetSelectedEmail();
            return;
        }

        if (indexOfDeletedEmail == 0) {
            resetSelectedEmail();
            emailListView.getSelectionModel().select(1);
            client.selectedEmail = emailListView.getItems().get(1);
            bindMailToView(client.selectedEmail);
        }

        if (indexOfDeletedEmail > 0 && indexOfDeletedEmail - 1 < emailListView.getItems().size()) {
            resetSelectedEmail();
            emailListView.getSelectionModel().select(indexOfDeletedEmail - 1);
            client.selectedEmail = emailListView.getItems().get(indexOfDeletedEmail - 1);
            bindMailToView(client.selectedEmail);
        }
    }

    private void resetSelectedEmail() {
        client.selectedEmail = new Email(client.getLargestID() + 1);

        fromTextField.textProperty().unbind();
        toTextField.textProperty().unbind();
        subjectTextField.textProperty().unbind();
        selectedEmailView.textProperty().unbind();

        bindMailToView(client.selectedEmail);
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
        ServerResponse response;
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
        if (client.selectedEmail == null || client.selectedEmail.getSender().equals("")) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR, "Please select an email");
                a.show();
            });
            return;
        }
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
                    // TODO check where is when click 2 times trash button
                    if (response == ServerResponse.ACTION_COMPLETED) {
                        int indexOfEmail = client.whereIs(client.selectedEmail).indexOf(client.selectedEmail);
                        loadAllFromServer();
                        resetSelectedEmail();
                        showNextEmail(indexOfEmail);
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
                    ArrayList<Email> emailsFromServer = new ArrayList<>();
                    try {
                        SerializableEmail serializableEmail;
                        //Get all emails from socket
                        while ((serializableEmail = (SerializableEmail) objectInputStream.readObject()) != null) {
                            Email serverEmail = new Email(serializableEmail);
                            emailsFromServer.add(serverEmail);
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

                    //Cycle through all emails from server to add those that are new or modified
                    AtomicInteger newMails = new AtomicInteger();
                    for (Email serverEmail : emailsFromServer) {
                        switch (serverEmail.getState()) {
                            case RECEIVED -> {
                                if (!client.hasSameIDInCollection(client.inboxProperty(), serverEmail)) {
                                    if (!serverEmail.isRead()) {
                                        newMails.getAndIncrement();
                                    }
                                    Platform.runLater(() -> {
                                        client.inboxProperty().add(serverEmail);
                                    });
                                }
                            }
                            case SENT -> {
                                if (!client.hasSameIDInCollection(client.sentProperty(), serverEmail))
                                    Platform.runLater(() -> client.sentProperty().add(serverEmail));
                            }
                            case DRAFTED -> {
                                if (!client.hasSameIDInCollection(client.draftsProperty(), serverEmail)) {
                                    Platform.runLater(() -> client.draftsProperty().add(serverEmail));
                                } else {
                                    if (client.newEmail != null && serverEmail.getID() == client.newEmail.getID()) {
                                        break;
                                    }
                                    //Getting the email with the same id and checking if it has any changes
                                    Email clientEmail = client.findEmailById(client.draftsProperty(), serverEmail.getID());
                                    if (serverEmail.senderProperty().getValue().compareTo(clientEmail.getSender()) != 0)
                                        clientEmail.senderProperty().setValue(serverEmail.getSender());
                                    if (serverEmail.receiverProperty().getValue().compareTo(clientEmail.getReceiver()) != 0)
                                        clientEmail.receiverProperty().setValue(serverEmail.getReceiver());
                                    if (serverEmail.subjectProperty().getValue().compareTo(clientEmail.getSubject()) != 0)
                                        clientEmail.subjectProperty().setValue(serverEmail.getSubject());
                                    if (serverEmail.bodyProperty().getValue().compareTo(clientEmail.getBody()) != 0)
                                        clientEmail.bodyProperty().setValue(serverEmail.getBody());
                                }
                            }
                            case TRASHED -> {
                                if (!client.hasSameIDInCollection(client.trashProperty(), serverEmail))
                                    Platform.runLater(() -> client.trashProperty().add(serverEmail));
                            }
                        }
                    }

                    //now we cycle through all of our emails to check if we have some that are not in the server anymore
                    Platform.runLater(() -> {
                        client.inboxProperty().removeIf(inboxEmail -> !containsID(emailsFromServer, inboxEmail, EmailState.RECEIVED));
                        client.sentProperty().removeIf(sentEmail -> !containsID(emailsFromServer, sentEmail, EmailState.SENT));
                        client.draftsProperty().removeIf(draftsEmail -> !containsID(emailsFromServer, draftsEmail, EmailState.DRAFTED));
                        client.trashProperty().removeIf(trashEmail -> !containsID(emailsFromServer, trashEmail, EmailState.TRASHED));
                    });
                    if (newMails.get() > 0) {
                        Platform.runLater(() -> {
                            Alert a = new Alert(Alert.AlertType.INFORMATION, "You have got " + newMails + " new Emails!");
                            a.show();
                        });
                    }
                } else //Not Action completed
                {
                    System.out.println("Loading action caused a problem");
                }
            }
        }).start();
    }

    private boolean containsID(ArrayList<Email> emailsFromServer, Email inboxEmail, EmailState emailState) {
        for (Email email : emailsFromServer) {
            if (email.getState() == emailState && inboxEmail.getID() == email.getID()) return true;
        }
        return false;
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
        scheduledExEmailDownloader.scheduleAtFixedRate(new PeriodicEmailDownloader(), 0, 5, TimeUnit.SECONDS);
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