package com.progiii.mailclientserver.server.model;

import com.progiii.mailclientserver.client.model.Client;

public class Action
{
    Client sender;
    Client receiver;
    Operation operation;

    public Action(Client sender, Client receiver, Operation operation)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.operation = operation;
    }

}
