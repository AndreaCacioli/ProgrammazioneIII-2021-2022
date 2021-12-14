package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


public class NewMsgController {

    @FXML
    private TextField textFieldTo;

    @FXML
    private TextField textFieldSubject;

    @FXML
    private TextArea textAreaMsg;

    @FXML
    private Button sendNewMsgButton;

    @FXML
    private Button draftsNewMsgButton;

    private Client client;
    private ClientController clientController;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    //GUI
    public void bindEverything() {
        textFieldTo.textProperty().bindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().bindBidirectional(client.newEmail.subjectProperty());
        textAreaMsg.textProperty().bindBidirectional(client.newEmail.bodyProperty());
    }


    ///////////////////////////////////////
    //Methods that send ACTIONS to Server//
    @FXML
    public void onSendButtonClicked(ActionEvent event) {
        doNewMailOperation(event, new Action(client, client.newEmail.getReceiver().strip(), Operation.SEND_EMAIL));
    }

    @FXML
    public void onSendToDraftsButtonClicked(Event event) {
        doNewMailOperation(event, new Action(client, null, Operation.NEW_DRAFT));
    }

    private void doNewMailOperation(Event event, Action action) {
        AtomicBoolean everythingWentFine = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> {
            synchronized (clientController.reentrantLock) {
                try {
                    clientController.getNewSocket();
                    clientController.setSocketSuccess();
                    clientController.sendActionToServer(action);
                    clientController.sendEmailToServer(new SerializableEmail(client.newEmail));
                    ServerResponse response = clientController.waitForResponse();
                    if (response == ServerResponse.ACTION_COMPLETED) {

                        if (action.getOperation() == Operation.NEW_DRAFT) client.draftsProperty().add(client.newEmail);
                        else if (action.getOperation() == Operation.SEND_EMAIL)
                            client.sentProperty().add(client.newEmail);

                        everythingWentFine.set(true);
                    } else {
                        Platform.runLater(() -> {
                            String s = action.getOperation() == Operation.NEW_DRAFT ? "drafting " : "sending ";
                            Alert a = new Alert(Alert.AlertType.ERROR, "Something went wrong while " + s + "an email");
                            a.show();
                        });
                    }
                    client.newEmail = new Email();
                } catch (IOException socketException) {
                    clientController.setSocketFailure();
                } catch (ClassNotFoundException e) {
                    System.out.println("Could not read from stream");
                } finally {
                    clientController.closeConnectionToServer();
                }
            }
        });

        t1.start();
        try {
            t1.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (everythingWentFine.get()) {
            textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
            textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
            textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

}
