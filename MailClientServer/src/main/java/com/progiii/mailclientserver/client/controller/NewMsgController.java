package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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

    /**
     * Binds the GUI input to the client.newEmail fields
     */
    public void bindEverything() {
        textFieldTo.textProperty().bindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().bindBidirectional(client.newEmail.subjectProperty());
        textAreaMsg.textProperty().bindBidirectional(client.newEmail.bodyProperty());
    }


    ///////////////////////////////////////
    //Methods that send ACTIONS to Server//
    @FXML
    public void onSendButtonClicked(Event event) {
        doNewMailOperation(event, new Action(client, client.newEmail.getReceiver(), Operation.SEND_EMAIL));
    }

    @FXML
    public void onSendToDraftsButtonClicked(Event event) {
        doNewMailOperation(event, new Action(client, null, Operation.NEW_DRAFT));
    }

    /**
     * First by using clientController.getNewSocket we create a Socket to
     * get the connection with Server, if we do it successfully we call setSocketSuccess which sets
     * on Client view the status connected, and finally we use sendActionToServer to send
     * the Action to the Server.
     *
     * If the Action is SEND_EMAIL we check if the receiver string
     * respect the Default Format
     *
     * After all, we send the Email(using SerializableEmail) to the Server, and we wait for his response
     * if everything is ok, everythingWentFine is set to true state.
     *
     * everythingWentFine allows us to unbind all (because the stage will close), create a new Email and
     * loadAllFromServer downloads the Emails
     *
     *
     * @param event  that cause the call of method
     * @param action which will be sent to Server
     */
    private void doNewMailOperation(Event event, Action action) {
        AtomicBoolean everythingWentFine = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> {
            synchronized (clientController.reentrantLock) {
                try {
                    clientController.getNewSocket();
                    clientController.setSocketSuccess();
                    clientController.sendActionToServer(action);

                    if (action.getOperation() == Operation.SEND_EMAIL) {
                        String[] receiversTmp = action.getReceiver().split(",");

                        for (String s : receiversTmp) {
                            if (!Email.validateEmailAddress(s.trim())) {
                                Platform.runLater(() -> {
                                    Alert a = new Alert(Alert.AlertType.ERROR, "Incorrect Format Email");
                                    a.show();
                                });
                                return;
                            }
                        }
                    }

                    clientController.sendEmailToServer(new SerializableEmail(client.newEmail));
                    ServerResponse response = clientController.waitForResponse();
                    if (response == ServerResponse.ACTION_COMPLETED) {
                        everythingWentFine.set(true);
                    }else if(response == ServerResponse.CLIENT_NOT_FOUND)
                    {
                        Alert a = new Alert(Alert.AlertType.ERROR, "One or more of the clients you tried to send an email to were not found!");
                        a.show();
                    }
                    else {
                        Platform.runLater(() -> {
                            String s = action.getOperation() == Operation.NEW_DRAFT ? "drafting " : "sending ";
                            Alert a = new Alert(Alert.AlertType.ERROR, "Something went wrong while " + s + "an email");
                            a.show();
                        });
                    }
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
            client.newEmail = new Email(client.getLargestID() + 1);
            textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
            textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
            textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

            clientController.loadAllFromServer();

            Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
            stage.close();
        }
    }

}
