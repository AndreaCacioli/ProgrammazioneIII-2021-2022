package com.progiii.mailclientserver.utils;

import com.progiii.mailclientserver.client.model.Client;

import java.io.Serializable;

/*
 * An action made by the server.
 * sender:    the user that sent the request to the server
 * receiver:  the user the action will be performed on (null for some actions)
 * operation: a value of the Operation.java file that represents what was done by the server
 * */
public class Action implements Serializable {
    String sender;
    String receiver;
    Operation operation;
    boolean successful;

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Action(Client sender, String receiverAddress, Operation operation) {
        this.sender = sender.getAddress();
        this.receiver = receiverAddress;
        this.operation = operation;
    }

    public String getSender() {
        return sender;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return successful ? "✅Action -> " +
                "Sender= '" + sender + '\'' +
                ", Receiver= '" + receiver + '\'' +
                ", Operation= " + operation
                :
                "❌Action -> " +
                        "Sender= '" + sender + '\'' +
                        ", Receiver= '" + receiver + '\'' +
                        ", Operation= " + operation
                ;
    }
}
