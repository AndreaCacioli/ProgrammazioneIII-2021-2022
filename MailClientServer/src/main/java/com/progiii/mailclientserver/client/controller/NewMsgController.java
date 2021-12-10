package com.progiii.mailclientserver.client.controller;

import com.progiii.mailclientserver.client.model.Client;
import com.progiii.mailclientserver.client.model.Email;
import com.progiii.mailclientserver.utils.Action;
import com.progiii.mailclientserver.utils.Operation;
import com.progiii.mailclientserver.utils.SerializableEmail;
import com.progiii.mailclientserver.utils.ServerResponse;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    //Getters and Setters
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
        //TODO verifica che funzioni perfettamente

        AtomicBoolean everythingWentFine = new AtomicBoolean(false);


        Thread t1 = new Thread(() -> {
                synchronized (clientController.reentrantLock) {
                    try {
                    clientController.getNewSocket();
                    clientController.sendActionToServer(new Action(client, client.newEmail.getReceiver().strip(), Operation.SEND_EMAIL));
                    clientController.sendEmailToServer(new SerializableEmail(client.newEmail));
                    ServerResponse response = clientController.waitForResponse();
                    if (response != ServerResponse.ACTION_COMPLETED) {
                        System.out.println("Something went wrong while sending an email");
                        //TODO fai sapere che qualcosa é andato storto
                    }
                    else //ACTION_COMPLETED
                    {
                        client.sentProperty().add(client.newEmail);
                        everythingWentFine.set(true);
                    }
                    client.newEmail = new Email();
                } catch(Exception ex){
                    ex.printStackTrace();
                }finally{
                    clientController.closeConnectionToServer();
                }
            }
        });

        t1.start();
        try{t1.join();}catch(Exception ex) {ex.printStackTrace();}

        if(everythingWentFine.get())
        {
            textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
            textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
            textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }

    }

    @FXML
    public void onSendToDraftsButtonClicked(Event event) {
        if (client.draftsProperty().contains(client.newEmail)) {
            //TODO send action for a change
            return;
        }

        textAreaMsg.textProperty().unbindBidirectional(client.newEmail.bodyProperty());
        textFieldTo.textProperty().unbindBidirectional(client.newEmail.receiverProperty());
        textFieldSubject.textProperty().unbindBidirectional(client.newEmail.subjectProperty());

        if (event instanceof ActionEvent) {
            Stage stage = (Stage) draftsNewMsgButton.getScene().getWindow();
            stage.close();
        }

        new Thread(() -> {
            synchronized (clientController.reentrantLock)
            {
                try {
                    clientController.getNewSocket();
                    clientController.sendActionToServer(new Action(client, null, Operation.NEW_DRAFT));
                    clientController.sendEmailToServer(new SerializableEmail(client.newEmail));
                    ServerResponse response = clientController.waitForResponse();
                    if(response == ServerResponse.ACTION_COMPLETED)
                    {
                        client.draftsProperty().add(client.newEmail);
                    }
                    else
                    {
                        //TODO fai sapere che qualcosa é andato storto
                    }
                    client.newEmail = new Email();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }finally {
                    clientController.closeConnectionToServer();
                }
            }

        }).start();


    }
}
