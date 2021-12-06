package com.progiii.mailclientserver.utils;

import com.progiii.mailclientserver.client.model.Client;

import java.io.Serializable;

public class Action implements Serializable
{
    String sender;
    String receiver;
    Operation operation;

    public Action(Client sender, String receiverAddress, Operation operation)
    {
        this.sender = sender.getAddress();
        this.receiver = receiverAddress;
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "Action{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", operation=" + operation +
                '}';
    }
}
